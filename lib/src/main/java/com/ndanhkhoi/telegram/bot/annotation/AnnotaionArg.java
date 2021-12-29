package com.ndanhkhoi.telegram.bot.annotation;

import java.lang.annotation.*;

/**
 * @author ndanhkhoi
 * Created at 10:24:51 April 09, 2021
 * @see com.ndanhkhoi.telegram.bot.core.BotCommandAgrs
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotaionArg {

    Class<? extends Annotation> value();

}
