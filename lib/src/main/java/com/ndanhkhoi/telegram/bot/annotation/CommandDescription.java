package com.ndanhkhoi.telegram.bot.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ndanhkhoi
 * Created at 10:20:44 April 09, 2021
 * An annotation to descript a command.
 * @see CommandMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandDescription {

    String value();

}
