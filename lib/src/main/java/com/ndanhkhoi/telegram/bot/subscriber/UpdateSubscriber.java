package com.ndanhkhoi.telegram.bot.subscriber;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.ndanhkhoi.telegram.bot.annotation.AnnotaionArg;
import com.ndanhkhoi.telegram.bot.annotation.TypeArg;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.resolver.ResolverRegistry;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import reactor.core.scheduler.Schedulers;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Optional;
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
    private final ApplicationContext applicationContext;

    public UpdateSubscriber(BotProperties botProperties, SimpleTelegramLongPollingCommandBot telegramLongPollingBot, ApplicationContext applicationContext) {
        this.botProperties = botProperties;
        this.telegramLongPollingBot = telegramLongPollingBot;
        this.applicationContext = applicationContext;
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

    private Optional<String> stickerDetect(Message message) {
        String stickerDetect = null;
        if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            Map<String, Object> stickerInfo = ImmutableMap.<String, Object>builder()
                    .put("fileId", sticker.getFileId())
                    .put("setName", sticker.getSetName())
                    .build();
            stickerDetect = "[x] Sticker Detected: \n" + stickerInfo.toString();
            log.info(stickerDetect);
        }
        return Optional.ofNullable(stickerDetect);
    }

    private void logMessage(Update update) {
        try {
            Message message = update.getMessage();
            String sendUsername = message.getFrom().getUserName();
            Long sendUserId = message.getFrom().getId();
            String messageInfo;
            String messageText =  Strings.nullToEmpty(message.getText());
            if (TelegramMessageUtils.isMessageInGroup(message)) {
                Long groupId = message.getChatId();
                String groupName = message.getChat().getTitle();
                messageInfo = String.format("New message from: %s, id: %s (in group %s, id: %s)", sendUsername, sendUserId, groupName, groupId);
            }
            else {
                messageInfo = String.format("New message from: %s, id: %s", sendUsername, sendUserId);
            }
            log.info("\n{} \n\t {}", messageInfo, messageText);

            StringBuilder textToSend = new StringBuilder("<code>" + messageInfo + "</code>\n\n" + messageText);
            stickerDetect(message).ifPresent(textToSend::append);

            if (StringUtils.isNotBlank(botProperties.getLoggerChatId())) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setParseMode(ParseMode.HTML);
                sendMessage.setText(textToSend.toString());
                sendMessage.setChatId(botProperties.getLoggerChatId());
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
        Object route = applicationContext.getBean(botCommand.getMethod().getDeclaringClass());
        Object returnValue = botCommand.getMethod().invoke(route, args);
        ResolverRegistry.INSTANCE.resolve(returnValue, botCommand, botCommandParams, telegramLongPollingBot);
    }

    private void processNonCommandUpdate(Update update) {
        try {
            NonCommandUpdateSubscriber nonCommandUpdateSubscriber = applicationContext.getBean(NonCommandUpdateSubscriber.class);
            nonCommandUpdateSubscriber.accept(update);
        }
        catch (NoSuchBeanDefinitionException ex) {
            defaultNonCommandUpdateSubscriber.accept(update);
        }
    }

    @SneakyThrows
    @Override
    public void accept(Update update) {
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
                telegramLongPollingBot
                        .getCommand(update)
                        .doOnError(ResolverRegistry.onErrorHandle(botCommandParams, telegramLongPollingBot))
                        .subscribeOn(Schedulers.parallel())
                        .subscribe(botCommand -> handleCmd(botCommand, botCommandParams));
            }
        }
    }

}
