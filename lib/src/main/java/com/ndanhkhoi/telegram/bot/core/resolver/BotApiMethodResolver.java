package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class BotApiMethodResolver implements TypeResolver<BotApiMethod> {

    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;

    public BotApiMethodResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    @Override
    public void resolve(BotApiMethod value, BotCommand botCommand, BotCommandParams params) {
        telegramLongPollingBot.executeSneakyThrows(value);
        log.debug("Excuted API method {}", value);
    }

    @Override
    public Class<BotApiMethod> getType() {
        return BotApiMethod.class;
    }

}
