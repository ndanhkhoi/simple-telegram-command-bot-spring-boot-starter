package com.ndanhkhoi.telegram.bot.annotation;

import java.lang.annotation.*;

/**
 * @author ndanhkhoi
 * Created at 18:37:41 April 29, 2022
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
//@ConditionalOnProperty
public @interface BotRouteConditionalOnProperty {

//    @AliasFor(annotation = ConditionalOnProperty.class)
    String[] value() default {};

//    @AliasFor(annotation = ConditionalOnProperty.class)
    String havingValue() default "";

}
