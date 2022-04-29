package com.ndanhkhoi.telegram.bot.subscriber;

import com.ndanhkhoi.telegram.bot.annotation.AnnotaionArg;
import com.ndanhkhoi.telegram.bot.annotation.BotExceptionHandler;
import com.ndanhkhoi.telegram.bot.annotation.BotRouteAdvice;
import com.ndanhkhoi.telegram.bot.annotation.TypeArg;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.model.UpdateTrace;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.resolver.ResolverRegistry;
import com.ndanhkhoi.telegram.bot.utils.SpringHelper;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * @author ndanhkhoi
 * Created at 12:06:17 April 09, 2021
 * A consumer that handle a command
 */
@Slf4j
@Singleton
public class UpdateSubscriber implements Consumer<Update> {

    private final DefaultNonCommandUpdateSubscriber defaultNonCommandUpdateSubscriber = new DefaultNonCommandUpdateSubscriber();
    private final BotProperties botProperties;
    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;
    private final SpringHelper springHelper;
    private final UpdateTraceRepository updateTraceRepository;
    private final UpdateObjectMapper updateObjectMapper;

    public UpdateSubscriber(BotProperties botProperties, SimpleTelegramLongPollingCommandBot telegramLongPollingBot, SpringHelper springHelper, UpdateTraceRepository updateTraceRepository, UpdateObjectMapper updateObjectMapper) {
        this.botProperties = botProperties;
        this.telegramLongPollingBot = telegramLongPollingBot;
        this.springHelper = springHelper;
        this.updateTraceRepository = updateTraceRepository;
        this.updateObjectMapper = updateObjectMapper;
    }

    private <T> OptionalInt getIndexArgByType(Parameter[] parameters, Class<T> clazz) {
        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].getType() == clazz)
                .findFirst();
    }

    private <T extends Annotation> OptionalInt getIndexArgByAnnotation(Parameter[] parameters, Class<T> annotationType) {
        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].getDeclaredAnnotationsByType(annotationType).length > 0)
                .findFirst();
    }

    @SneakyThrows
    private <T> Object getProperty(T bean, String name) {
        return PropertyUtils.getProperty(bean, name);
    }

    @SneakyThrows
    private Object[] getBotCommandeArgs(Method method, BotCommandParams botCommandParams) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        FieldUtils.getAllFieldsList(BotCommandParams.class)
                .forEach(field -> {
                    TypeArg[] typeArgs = field.getDeclaredAnnotationsByType(TypeArg.class);
                    if (typeArgs.length > 0) {
                        Class<?> fieldType = field.getType();
                        OptionalInt idx = getIndexArgByType(parameters, fieldType);
                        if (idx.isPresent()) {
                            args[idx.getAsInt()] = getProperty(botCommandParams, field.getName());
                        }
                    }
                    AnnotaionArg[] annotaionArgs = field.getDeclaredAnnotationsByType(AnnotaionArg.class);
                    if (annotaionArgs.length > 0) {
                        AnnotaionArg annotaionArg = annotaionArgs[0];
                        OptionalInt idx = getIndexArgByAnnotation(parameters, annotaionArg.value());
                        if (idx.isPresent()) {
                            args[idx.getAsInt()] = getProperty(botCommandParams, field.getName());
                        }
                    }
                });

        return args;
    }

    private void logMessage(Update update) {
        try {
            log.info("New update detected -> {}", updateObjectMapper.writeValueAsString(update));
            if (StringUtils.isNotBlank(botProperties.getLoggingChatId())) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("New update detected -> \n" + updateObjectMapper.writeValueAsPrettyString(update));
                sendMessage.setChatId(botProperties.getLoggingChatId());
                telegramLongPollingBot.execute(sendMessage);
            }
        }
        catch (Exception ex) {
            log.error("Error!", ex);
        }
    }

    @SneakyThrows
    private void handleCmd(BotCommand botCommand, BotCommandParams botCommandParams) {
        Object[] args = getBotCommandeArgs(botCommand.getMethod(), botCommandParams);
        Object route = springHelper.getBean(botCommand.getMethod().getDeclaringClass());
        try {
            Object returnValue = botCommand.getMethod().invoke(route, args);
            ResolverRegistry.INSTANCE.resolve(returnValue, botCommand, botCommandParams, telegramLongPollingBot);
        }
        catch (InvocationTargetException ex) {
            // Exception when invoke method. Ex: bot method throws exception manually
            throw ex.getTargetException();
        }
    }

    private void processNonCommandUpdate(Update update) {
        if (springHelper.existBean(NonCommandUpdateSubscriber.class)) {
            NonCommandUpdateSubscriber nonCommandUpdateSubscriber = springHelper.getBean(NonCommandUpdateSubscriber.class);
            nonCommandUpdateSubscriber.accept(update);
        }
        else {
            defaultNonCommandUpdateSubscriber.accept(update);
        }
    }

    @Override
    public void accept(Update update) {
        if (BooleanUtils.isTrue(botProperties.getEnableUpdateTrace())) {
            updateTraceRepository.add(new UpdateTrace(update));
        }
        Message message = update.getMessage();
        if (message == null) {
            return;
        }
        logMessage(update);
        if (!TelegramMessageUtils.isChannelPost(update)) {
            boolean isNonCommand = (message.hasText() && !StringUtils.startsWith(message.getText(), CommonConstant.CMD_PREFIX)) ||
                    (message.hasPhoto() && !StringUtils.startsWith(message.getCaption(), CommonConstant.CMD_PREFIX));
            if (isNonCommand) {
                this.processNonCommandUpdate(update);
                return;
            }
            BotCommandParams botCommandParams = telegramLongPollingBot.getCommandParams(update);
            if (botCommandParams != null) {
                try {
                    telegramLongPollingBot
                            .getCommand(update)
                            .ifPresent(botCommand -> {
                                botCommandParams.setCommandName(botCommand.getCmd());
                                handleCmd(botCommand, botCommandParams);
                            });
                }
                catch (Throwable t) {
                    doOnError(t, botCommandParams);
                }
            }
        }
    }

    @SneakyThrows
    private void doOnError(Throwable t, BotCommandParams params) {
        Map<String, Object> adviceMap = springHelper.getBeansWithAnnotation(BotRouteAdvice.class);
        Method handleMethod = null;
        Object adviceBean = null;
        for (Map.Entry<String, Object> entry : adviceMap.entrySet()) {
            for (Method method : entry.getValue().getClass().getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())
                        && method.getDeclaredAnnotationsByType(BotExceptionHandler.class).length > 0
                        && method.getDeclaredAnnotationsByType(BotExceptionHandler.class)[0].value() == t.getClass()) {
                    handleMethod = method;
                    adviceBean = entry.getValue();
                    break;
                }
            }
        }

        if (handleMethod == null || adviceBean == null) {
            ResolverRegistry.onErrorHandle(params, telegramLongPollingBot).accept(t);
        }
        else {
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
                ResolverRegistry.onErrorHandle(params, telegramLongPollingBot).accept(t);
            }
            else if (returnValue instanceof String) {
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), (String) returnValue,false);
            }
            else if (returnValue instanceof BotApiMethod) {
                telegramLongPollingBot.execute((BotApiMethod<? extends Serializable>) returnValue);
            }
            else {
                log.warn("Returnd value of {}#{} is not supported ({}), so default error handler will be called as a callback", adviceBean.getClass().getSimpleName(), handleMethod.getName(), returnValue.getClass().getName());
                ResolverRegistry.onErrorHandle(params, telegramLongPollingBot).accept(t);
            }
        }
    }

}
