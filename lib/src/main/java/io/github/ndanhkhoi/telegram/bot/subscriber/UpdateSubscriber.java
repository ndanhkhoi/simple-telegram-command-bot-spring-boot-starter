package io.github.ndanhkhoi.telegram.bot.subscriber;

import io.github.ndanhkhoi.telegram.bot.annotation.AnnotaionArg;
import io.github.ndanhkhoi.telegram.bot.annotation.TypeArg;
import io.github.ndanhkhoi.telegram.bot.constant.CommonConstant;
import io.github.ndanhkhoi.telegram.bot.core.BotProperties;
import io.github.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import io.github.ndanhkhoi.telegram.bot.core.registry.AdviceRegistry;
import io.github.ndanhkhoi.telegram.bot.core.registry.ResolverRegistry;
import io.github.ndanhkhoi.telegram.bot.mapper.UpdateMapper;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import io.github.ndanhkhoi.telegram.bot.model.UpdateTrace;
import io.github.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import io.github.ndanhkhoi.telegram.bot.utils.ReflectUtils;
import io.github.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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

    private UpdateMapper getUpdateMapper() {
        return applicationContext.getBean("updateMapper", UpdateMapper.class);
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
                            args[idx.getAsInt()] = ReflectUtils.getProperty(botCommandParams, field.getName());
                        }
                    }
                    AnnotaionArg annotaionArg = AnnotationUtils.findAnnotation(field, AnnotaionArg.class);
                    if (annotaionArg != null) {
                        OptionalInt idx = getIndexArgByAnnotation(parameters, annotaionArg.value());
                        if (idx.isPresent()) {
                            args[idx.getAsInt()] = ReflectUtils.getProperty(botCommandParams, field.getName());
                        }
                    }
                });

        return args;
    }

    private void logUpdate(Update update) {
        UpdateMapper updateMapper = getUpdateMapper();
        BotProperties botProperties = getBotProperties();
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = getBotInstance();
        log.debug("New update detected -> {}", getUpdateMapper().writeValueAsString(update));
        if (StringUtils.isNotBlank(botProperties.getLoggingChatId())) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setText("New update detected -> \n" + updateMapper.writeValueAsPrettyString(update));
            sendMessage.setChatId(botProperties.getLoggingChatId());
            telegramLongPollingBot.executeSneakyThrows(sendMessage);
        }
    }

    private void handleConsumeError(Throwable t, BotCommandParams botCommandParams) {
        // Exception when invoke method. Ex: bot method throws exception manually
        if (t instanceof InvocationTargetException) {
            InvocationTargetException itex = (InvocationTargetException) t;
            executeCommandAdvice(itex.getTargetException(), botCommandParams);
        }
        else {
            executeCommandAdvice(t, botCommandParams);
        }
    }

    public void handleReturnedValue(Supplier<Object> returmedSupplier, BotCommand botCommand, BotCommandParams botCommandParams) {
        ResolverRegistry resolverRegistry = getResolverRegistry();
        Set<Class<Object>> supportedTypes = resolverRegistry.getSupportedTypes();
        Set<String> supportedTypesName = supportedTypes.stream()
                .map(Class::getName)
                .collect(Collectors.toSet());
        Mono.fromSupplier(returmedSupplier)
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
                }, t -> handleConsumeError(t, botCommandParams));
    }

    @SneakyThrows
    private Object invokeMethod(Object bean, Method method, Object ...args) {
        return method.invoke(bean, args);
    }

    public void handleCmd(BotCommand botCommand, BotCommandParams botCommandParams) {
        Object[] args = getBotCommandArgs(botCommand.getMethod(), botCommandParams);
        Object route = applicationContext.getBean(botCommand.getMethod().getDeclaringClass());
        handleReturnedValue(() -> invokeMethod(route, botCommand.getMethod(), args), botCommand, botCommandParams);
    }

    private void sendUnknownErrorAlert(BotCommandParams params, Throwable t) {
        SimpleTelegramLongPollingCommandBot telegramLongPollingBot = applicationContext.getBean(SimpleTelegramLongPollingCommandBot.class);
        log.error("Error!", t);
        TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), CommonConstant.ERROR_NOTIFY_MESSAGE, null);
    }

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
            Object returnValue = invokeMethod(adviceBean, handleMethod, args);
            if (returnValue == null) {
                log.warn("Returnd value of {}#{} is null, so default error handler will be called as a callback", adviceBean.getClass().getSimpleName(), handleMethod.getName());
                sendUnknownErrorAlert(params, t);
            }
            else if (returnValue instanceof String) {
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), (String) returnValue,null);
            }
            else if (returnValue instanceof BotApiMethod) {
                telegramLongPollingBot.executeSneakyThrows((BotApiMethod<? extends Serializable>) returnValue);
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

    private void process(Update update) {
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
        this.process(update);
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
