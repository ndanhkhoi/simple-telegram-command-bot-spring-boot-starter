package com.ndanhkhoi.telegram.bot.core.processor;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * @author khoinda
 * Created at 16:52:27 May 31, 2022
 */
public class ProcessorConfig implements ImportBeanDefinitionRegistrar {

    public static final String BOT_ROUTE_ANNOTATION_PROCESSOR_BEAN_NAME = "com.ndanhkhoi.telegram.bot.core.botRoutePostProcessor";
    public static final String BOT_ADVICE_ANNOTATION_PROCESSOR_BEAN_NAME = "com.ndanhkhoi.telegram.bot.core.botAdvicePostProcessor";
    public static final String TYPE_RESOLVER_PROCESSOR_BEAN_NAME = "com.ndanhkhoi.telegram.bot.core.typeResolvertProcessor";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBean(registry, BOT_ROUTE_ANNOTATION_PROCESSOR_BEAN_NAME, BotRoutePostProcessor.class);
        registerBean(registry, BOT_ADVICE_ANNOTATION_PROCESSOR_BEAN_NAME, BotAdvicePostProcessor.class);
        registerBean(registry, TYPE_RESOLVER_PROCESSOR_BEAN_NAME, TypeResolvertProcessor.class);
    }

    private <T> void registerBean(BeanDefinitionRegistry registry, String beanName, Class<T> clazz) {
        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, new RootBeanDefinition(clazz));
        }
    }

}
