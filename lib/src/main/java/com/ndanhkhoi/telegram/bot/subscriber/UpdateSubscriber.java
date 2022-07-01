package com.ndanhkhoi.telegram.bot.subscriber;

import com.ndanhkhoi.telegram.bot.annotation.AnnotaionArg;
import com.ndanhkhoi.telegram.bot.annotation.TypeArg;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.core.registry.AdviceRegistry;
import com.ndanhkhoi.telegram.bot.core.registry.ResolverRegistry;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.model.UpdateTrace;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author ndanhkhoi
 * Created at 12:06:17 April 09, 2021
 * A consumer that handle a command
 */
@Slf4j
public class UpdateSubscriber implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private SimpleTelegramLongPollingCommandBot getBotInstance() {
        return applicationContext.getBean(SimpleTelegramLongPollingCommandBot.class);
    }

    private BotProperties getBotProperties() {
        return applicationContext.getBean(BotProperties.class);
    }

    private UpdateObjectMapper getUpdateObjectMapper() {
        return applicationContext.getBean("updateObjectMapper", UpdateObjectMapper.class);
    }

    private UpdateTraceRepository getUpdateTraceRepository() {
        return applicationContext.getBean(UpdateTraceRepository.class);
    }

    private Executor getTaskExecutor() {
        return applicationContext.getBean("botAsyncTaskExecutor", SimpleAsyncTaskExecutor.class);
    }

    private ResolverRegistry getResolverRegistry() {
        return applicationContext.getBean(ResolverRegistry.class);
    }

    private boolean isUpdateTraceEnabled() {
        BotProperties botProperties = getBotProperties();
        return botProperties.getEnableUpdateTrace() != null && botProperties.getEnableUpdateTrace();
    }

    private <T> OptionalInt getIndexArgByType(Parameter[] parameters, Class<T> clazz) {
        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].getType() == clazz)
                .findFirst();
    }

    private <T extends Annotation> OptionalInt getIndexArgByAnnotation(Parameter[] parameters, Class<T> annotationType) {
        return IntStream.range(0, parameters.length)
                .filter(i -> AnnotationUtils.findAnnotation(parameters[i], annotationType) != null)
                .findFirst();
    }

    @SneakyThrows
    private <T> Object getProperty(T bean, String name) {
        return PropertyUtils.getProperty(bean, name);
    }

    @SneakyThrows
    private Object[] getBotCommandArgs(Method method, BotCommandParams botCommandParams) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        FieldUtils.getAllFieldsList(BotCommandParams.class)
                .forEach(field -> {
                    TypeArg typeArg = AnnotationUtils.findAnnotation(field, TypeArg.class);
                    if (typeArg != null) {
                        Class<?> fieldType = field.getType();
                        OptionalInt idx = getIndexArgByType(parameters, fieldType);
                        if (idx.isPresent()) {
                            args[idx.getAsInt()] = getProperty(botCommandParams, field.getName());
                        }
                    }
                    AnnotaionArg annotaionArg = AnnotationUtils.findAnnotation(field, AnnotaionArg.class);
                    if (annotaionArg != null) {
                        OptionalInt idx = getIndexArgByAnnotation(parameters, annotaionArg.value());
                        if (idx.isPresent()) {
                            args[idx.getAsInt()] = getProperty(botCommandParams, field.getName());
                        }
                    }
                });

        return args;
    }

    private void logUpdate(Update update) {
        UpdateObjectMapper updateObjectMapper = getUpdateObjectMapper();
        BotProperties botProperties = getBotProperties();
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = getBotInstance();
        log.debug("New update detected -> {}", getUpdateObjectMapper().writeValueAsString(update));
        if (StringUtils.isNotBlank(botProperties.getLoggingChatId())) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("New update detected -> \n" + updateObjectMapper.writeValueAsPrettyString(update));
            sendMessage.setChatId(botProperties.getLoggingChatId());
            telegramLongPollingBot.executeSneakyThrows(sendMessage);
        }
    }

    @SneakyThrows
    public void handleReturnedValue(Supplier<Object> returmedSupplier, BotCommand botCommand, BotCommandParams botCommandParams) {
        ResolverRegistry resolverRegistry = getResolverRegistry();
        Set<Class<Object>> supportedTypes = resolverRegistry.getSupportedTypes();
        Set<String> supportedTypesName = supportedTypes.stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
        Mono.fromSupplier(returmedSupplier)
                // Exception when invoke method. Ex: bot method throws exception manually
                .doOnError(InvocationTargetException.class, itex -> executeCommandAdvice(itex.getTargetException(), botCommandParams))
                .doOnError(Exception.class, ex -> executeCommandAdvice(ex, botCommandParams))
                .flatMapMany(rawReturnedValue -> {
                    if (Objects.isNull(rawReturnedValue)) {
                        log.info("Nothing to reply. Cause return value is null or it's type is Void");
                        return Flux.empty();
                    }
                    else if (rawReturnedValue instanceof Mono) {
                        return ((Mono<?>) rawReturnedValue).flux();
                    }
                    else if (rawReturnedValue instanceof Flux) {
                        return  (Flux<?>) rawReturnedValue;
                    }
                    return Flux.just(rawReturnedValue);
                })
                .subscribeOn(Schedulers.fromExecutor(getTaskExecutor()))
                .subscribe(returnValue -> {
                    Class<?> type = returnValue.getClass();
                    Optional<Class<Object>> supportedType = supportedTypes.stream()
                            .filter(e -> e.isAssignableFrom(type))
                            .findFirst();
                    if (supportedType.isPresent()) {
                        resolverRegistry.getResolverByType(supportedType.get())
                                .resolve(returnValue, botCommand, botCommandParams);
                    }
                    else {
                        log.warn("Nothing to reply. Cause the return type is not supported ({}}). Supported types are: {}", type.getName(), supportedTypesName);
                    }
                }, t -> executeCommandAdvice(t, botCommandParams));
    }

    @SneakyThrows
    private Object invokeMethod(Object bean, Method method, Object ...args) {
        return method.invoke(bean, args);
    }

    @SneakyThrows
    public void handleCmd(BotCommand botCommand, BotCommandParams botCommandParams) {
        Object[] args = getBotCommandArgs(botCommand.getMethod(), botCommandParams);
        Object route = applicationContext.getBean(botCommand.getMethod().getDeclaringClass());
        handleReturnedValue(() -> invokeMethod(route, botCommand.getMethod(), args), botCommand, botCommandParams);
    }

    private void sendUnknownErrorAlert(BotCommandParams params, Throwable t) {
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = applicationContext.getBean(SimpleTelegramLongPollingCommandBot.class);
        log.error("Error!", t);
        TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), CommonConstant.ERROR_NOTIFY_MESSAGE, false);
    }

    @SneakyThrows
    public void executeCommandAdvice(Throwable t, BotCommandParams params) {
        AdviceRegistry adviceRegistry = applicationContext.getBean(AdviceRegistry.class);
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = applicationContext.getBean(SimpleTelegramLongPollingCommandBot.class);
        if (adviceRegistry.hasAdvice(t.getClass())) {
            Method handleMethod = adviceRegistry.getAdvice(t.getClass()).getMethod();
            Object adviceBean = adviceRegistry.getAdvice(t.getClass()).getBean();
            Parameter[] parameters = handleMethod.getParameters();
            Object[] args = new Object[parameters.length];
            for (int idx = 0; idx < parameters.length; idx++) {
                if (parameters[idx].getType() == Update.class) {
                    args[idx] = params.getUpdate();
                }
                else if (Throwable.class.isAssignableFrom(parameters[idx].getType())) {
                    args[idx] = t;
                }
            }
            Object returnValue = handleMethod.invoke(adviceBean, args);
            if (returnValue == null) {
                log.warn("Returnd value of {}#{} is null, so default error handler will be called as a callback", adviceBean.getClass().getSimpleName(), handleMethod.getName());
                sendUnknownErrorAlert(params, t);
            }
            else if (returnValue instanceof String) {
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), (String) returnValue,false);
            }
            else if (returnValue instanceof BotApiMethod) {
                telegramLongPollingBot.execute((BotApiMethod<? extends Serializable>) returnValue);
            }
            else {
                log.warn("Returnd value of {}#{} is not supported ({}), so default error handler will be called as a callback", adviceBean.getClass().getSimpleName(), handleMethod.getName(), returnValue.getClass().getName());
                sendUnknownErrorAlert(params, t);
            }
        }
        else {
            sendUnknownErrorAlert(params, t);
        }
    }

    private void excuteCommand(Update update, BotCommandParams botCommandParams, SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        telegramLongPollingBot
                .getCommand(update)
                .ifPresentOrElse(botCommand -> {
                    botCommandParams.setCommandName(botCommand.getCmd());
                    handleCmd(botCommand, botCommandParams);
                }, () -> applicationContext.getBean(CommandNotFoundUpdateSubscriber.class).accept(update, botCommandParams.getCommandName()));
    }

    private void subscribe(Update update) {
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = getBotInstance();
        if (update.getCallbackQuery() != null) {
            applicationContext.getBean(CallbackQuerySubscriber.class).accept(update);
        }
        else {
            Message message = update.getMessage();
            boolean isNonCommand = message == null ||
                    (message.hasText() && !StringUtils.startsWith(message.getText(), CommonConstant.CMD_PREFIX)) ||
                    (message.hasPhoto() && !StringUtils.startsWith(message.getCaption(), CommonConstant.CMD_PREFIX)) ||
                    TelegramMessageUtils.isChannelPost(update);

            if (isNonCommand) {
                NonCommandUpdateSubscriber nonCommandUpdateSubscriber = applicationContext.getBean(NonCommandUpdateSubscriber.class);
                nonCommandUpdateSubscriber.accept(update);
            }
            else {
                BotCommandParams botCommandParams = telegramLongPollingBot.getCommandParams(update);
                if (botCommandParams != null) {
                    excuteCommand(update, botCommandParams, telegramLongPollingBot);
                }
            }
        }
    }

    private void consume(Update update) {
        this.logUpdate(update);
        if (isUpdateTraceEnabled()) {
            UpdateTraceRepository updateTraceRepository = getUpdateTraceRepository();
            updateTraceRepository.add(Mono.just(new UpdateTrace(update)));
        }
        // Pre-Processor
        applicationContext.getBean(PreSubscriber.class).accept(update);
        // Main Processor
        this.subscribe(update);
        // Pos-Processor
        applicationContext.getBean(PosSubscriber.class).accept(update);
    }

    public void consume(Mono<Update> update) {
        update.subscribeOn(Schedulers.fromExecutor(getTaskExecutor()))
                .subscribe(this::consume);
    }

    public void consume(Flux<Update> updates) {
        updates.subscribeOn(Schedulers.fromExecutor(getTaskExecutor()))
                .subscribe(this::consume);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
