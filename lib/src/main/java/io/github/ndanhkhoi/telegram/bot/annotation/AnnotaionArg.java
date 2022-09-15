package io.github.ndanhkhoi.telegram.bot.annotation;

import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;

import java.lang.annotation.*;

/**
 * @author ndanhkhoi
 * Created at 10:24:51 April 09, 2021
 * @see BotCommandParams
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotaionArg {

    Class<? extends Annotation> value();

}
