package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public final class StringResolver extends TypeResolver<String> {

    public static final StringResolver INSTANCE = new StringResolver (String.class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                if (StringUtils.isBlank((String) value)) {
                    LOGGER.warn("Blank string returnd");
                    return;
                }
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, botCommandParams.getUpdate().getMessage(), (String) value, botCommand.isUseHtml(), botCommand.isDisableWebPagePreview());
                LOGGER.info("Reply Message: {}", value);
            }
    );

    private StringResolver(Class<String> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

}
