package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import reactor.core.CorePublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.Consumer4;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

/**
 * @author ndanhkhoi
 * Created at 17:00:46 October 05, 2021
 * A Bot command handler instance.
 * @see CommandMapping
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(setterPrefix = "with")
public class BotCommand implements Consumer4<Object, Object[], BotCommandAgrs, TelegramLongPollingBot> {

    /**
     * A command
     * */
    private String cmd;

    /**
     * @see CommandMapping#useHtml()
     * */
    private Boolean useHtml;

    /**
     * @see CommandMapping#disableWebPagePreview()
     * */
    private Boolean disableWebPagePreview;

    /**
     * @see CommandMapping#accessUserIds()
     * */
    private long[] accessUserIds;

    /**
     * @see CommandMapping#accessGroupIds()
     * */
    private long[] accessGroupIds;

    /**
     * @see CommandMapping#allowAllUserAccess()
     * */
    private Boolean allowAllUserAccess;

    /**
     * @see CommandMapping#onlyAdmin()
     * */
    private Boolean onlyAdmin;

    /**
     * @see CommandMapping#sendFile()
     * */
    private MediaType sendFile;

    /**
     * @see CommandMapping#onlyForOwner()
     * */
    private Boolean onlyForOwner;

    /**
     * A method that is marked by {@link CommandMapping} annotation
     * */
    private Method method;

    /**
     * {@link com.ndanhkhoi.telegram.bot.annotation.CommandDescription}
     * */
    private String description;

    /**
     * Description of command's body
     * @see CommandBody#description()
     * */
    private String bodyDescription;

    @SneakyThrows
    private Object invokeMethod(Object resource, Object[] agrs) {
        return method.invoke(resource, agrs);
    }

    private void reply(Object replyContent, BotCommandAgrs params, TelegramLongPollingBot telegramLongPollingBot) {
        if (replyContent instanceof String) {
            TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), Objects.toString(replyContent), useHtml, disableWebPagePreview);
            log.info("Reply Message: {}", replyContent);
        }
        else if (replyContent instanceof InputFile) {
            SendMediaUtils.sendMedia(params.getUpdate().getMessage(), (InputFile) replyContent, params.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
            log.info("Reply Media: [{}]", sendFile);
        }
        else if (replyContent instanceof List) {
            for (Object e : (List<?>) replyContent) {
                reply(e, params, telegramLongPollingBot);
            }
        }
    }

    @Override
    public void accept(Object resource, Object[] agrs, BotCommandAgrs params, TelegramLongPollingBot telegramLongPollingBot) {
        Object replyContent = invokeMethod(resource, agrs);
        if (replyContent instanceof CorePublisher) {
            if (replyContent instanceof Mono) {
                ((Mono<?>) replyContent)
                        .subscribe(e -> reply(e, params, telegramLongPollingBot));
            }
            else if (replyContent instanceof Flux) {
                ((Flux<?>) replyContent)
                        .subscribe(e -> reply(e, params, telegramLongPollingBot));
            }
        }
        else {
            reply(replyContent, params, telegramLongPollingBot);
        }
    }

}
