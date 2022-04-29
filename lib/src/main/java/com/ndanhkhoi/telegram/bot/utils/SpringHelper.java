package com.ndanhkhoi.telegram.bot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author ndanhkhoi
 * Created at 20:22:55 March 15, 2022
 */
@RequiredArgsConstructor
public class SpringHelper {

    private final ApplicationContext applicationContext;
    private final Environment env;

    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    public <T extends Annotation> Map<String, Object> getBeansWithAnnotation(Class<T> annotation) {
        return applicationContext.getBeansWithAnnotation(annotation);
    }

    public <T> boolean existBean(Class<T> clazz) {
        try {
            getBean(clazz);
            return true;
        }
        catch (NoSuchBeanDefinitionException ex) {
            return false;
        }
    }

    public <T> T getProperty(String key, Class<T> clazz) {
        return env.getProperty(key, clazz);
    }

}
