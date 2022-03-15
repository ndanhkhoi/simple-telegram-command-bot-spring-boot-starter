package com.ndanhkhoi.telegram.bot.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

/**
 * @author ndanhkhoi
 * Created at 20:22:55 March 15, 2022
 */
@RequiredArgsConstructor
public class SpringBeanUtils {

    private final ApplicationContext applicationContext;

    public <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
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

}
