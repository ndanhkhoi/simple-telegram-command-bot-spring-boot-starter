package com.ndanhkhoi.telegram.bot.annotation;

import com.ndanhkhoi.telegram.bot.model.BotCommandParams;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ndanhkhoi
 * Created at 10:24:51 April 09, 2021
 * @see BotCommandParams
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeArg {
}
