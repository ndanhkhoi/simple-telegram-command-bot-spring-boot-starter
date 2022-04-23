package com.ndanhkhoi.telegram.bot.annotation;

import com.ndanhkhoi.telegram.bot.model.BotCommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created at 21:15:40 April 23, 2022
 * An annotation to mark a param in {@link BotCommand} method as a command name
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandName {
}
