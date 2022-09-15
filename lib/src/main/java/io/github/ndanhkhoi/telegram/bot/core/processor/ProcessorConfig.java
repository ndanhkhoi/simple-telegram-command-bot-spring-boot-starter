package io.github.ndanhkhoi.telegram.bot.core.processor;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Arrays;
import java.util.List;

/**
 * @author khoinda
 * Created at 16:52:27 May 31, 2022
 */
public class ProcessorConfig implements ImportBeanDefinitionRegistrar {

    private static final List<Class<? extends BeanPostProcessor>> BEANPOST_PROCESSOR_LIST = Arrays.asList(
            BotRoutePostProcessor.class,
            BotAdvicePostProcessor.class,
            TypeResolverPostProcessor.class
    );

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        BEANPOST_PROCESSOR_LIST.forEach(bean -> registerBean(registry, bean.getName(), bean));
    }

    private <T> void registerBean(BeanDefinitionRegistry registry, String beanName, Class<T> clazz) {
        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, new RootBeanDefinition(clazz));
        }
    }

}
