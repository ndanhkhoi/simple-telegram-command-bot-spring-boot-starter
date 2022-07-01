package com.ndanhkhoi.telegram.bot.annotation;

import com.ndanhkhoi.telegram.bot.core.resolver.TypeResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author khoinda
 * Created at 09:30:06 July 01, 2022
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ConditionalOnMissingBean(parameterizedContainer = TypeResolver.class)
public @interface ConditionalOnMissingTypeResolverBean {

    @AliasFor(annotation = ConditionalOnMissingBean.class)
    Class<?>[] value() default {};

}
