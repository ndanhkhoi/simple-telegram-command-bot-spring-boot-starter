package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandArgs;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public class StringResolver extends TypeResolver<String> {

    private StringResolver(Class<String> type, Consumer4<Object, BotCommand, BotCommandArgs, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

    public final static StringResolver INSTANCE = new StringResolver (String.class,
            (value, botCommand, botCommandArgs, telegramLongPollingBot) -> {
                if (StringUtils.isBlank((String) value)) {
                    LOGGER.warn("Blank string returnd");
                    return;
                }
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, botCommandArgs.getUpdate().getMessage(), (String) value, botCommand.getUseHtml(), botCommand.getDisableWebPagePreview());
                LOGGER.info("Reply Message: {}", value);
            }
        );

}
