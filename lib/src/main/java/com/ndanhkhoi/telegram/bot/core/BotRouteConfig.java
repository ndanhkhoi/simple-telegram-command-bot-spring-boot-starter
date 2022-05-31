package com.ndanhkhoi.telegram.bot.core;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author khoinda
 * Created at 16:52:27 May 31, 2022
 */
public class BotRouteConfig implements ImportBeanDefinitionRegistrar {

    public static final String BOT_ROUTE_ANNOTATION_PROCESSOR_BEAN_NAME = "com.ndanhkhoi.telegram.bot.core.botRoutePostProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(BOT_ROUTE_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            registry.registerBeanDefinition(BOT_ROUTE_ANNOTATION_PROCESSOR_BEAN_NAME, new RootBeanDefinition(BotRoutePostProcessor.class));
        }

    }

}
