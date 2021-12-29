package com.ndanhkhoi.telegram.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ndanhkhoi
 * Created at 10:50:34 April 09, 2021
 * An annotation to mark a param in {@link com.ndanhkhoi.telegram.bot.core.BotCommand} method as a user id
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface SendUserId {
}
