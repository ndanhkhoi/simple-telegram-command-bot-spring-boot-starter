package com.ndanhkhoi.telegram.bot.annotation;

import com.ndanhkhoi.telegram.bot.model.BotCommandArgs;

import java.lang.annotation.*;

/**
 * @author ndanhkhoi
 * Created at 10:24:51 April 09, 2021
 * @see BotCommandArgs
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotaionArg {

    Class<? extends Annotation> value();

}
