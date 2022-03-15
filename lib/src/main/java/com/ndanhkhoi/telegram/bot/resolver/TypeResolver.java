package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

/**
 * @author ndanhkhoi
 * Created at 21:59:44 February 26, 2022
 */
@Getter
public class TypeResolver<T> {

    protected static final Logger LOGGER = LoggerFactory.getLogger(TypeResolver.class);

    private final Class<T> type;
    private final Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver;

    TypeResolver(Class<T> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        this.type = type;
        this.resolver = resolver;
    }

}
