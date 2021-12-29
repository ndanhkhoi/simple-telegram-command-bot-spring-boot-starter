package com.ndanhkhoi.telegram.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ndanhkhoi
 * Created at 10:24:51 April 09, 2021
 * @see com.ndanhkhoi.telegram.bot.core.BotCommandAgrs
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeArg {
}
