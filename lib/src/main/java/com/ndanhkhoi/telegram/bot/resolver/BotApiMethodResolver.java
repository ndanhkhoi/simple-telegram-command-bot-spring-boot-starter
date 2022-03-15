package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import reactor.function.Consumer4;

import java.io.Serializable;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class BotApiMethodResolver extends TypeResolver<BotApiMethod> {

    private BotApiMethodResolver(Class<BotApiMethod> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

    public static final BotApiMethodResolver INSTANCE = new BotApiMethodResolver(BotApiMethod.class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                excute(telegramLongPollingBot, (BotApiMethod) value);
                LOGGER.info("Excuted API method {}", value);
            }
        );

    @SneakyThrows
    private static <T extends Serializable> void excute(TelegramLongPollingBot telegramLongPollingBot, BotApiMethod<T> apiMethod) {
        telegramLongPollingBot.execute(apiMethod);
    }

}
