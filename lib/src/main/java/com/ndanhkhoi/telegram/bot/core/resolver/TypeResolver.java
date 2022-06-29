package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;

/**
 * @author ndanhkhoi
 * Created at 21:59:44 February 26, 2022
 */
public interface TypeResolver<T> {
    void resolve(T value, BotCommand botCommand, BotCommandParams params);

    Class<T> getType();

}
