package com.ndanhkhoi.telegram.bot.core.processor;

import com.ndanhkhoi.telegram.bot.annotation.BotExceptionHandler;
import com.ndanhkhoi.telegram.bot.annotation.BotRouteAdvice;
import com.ndanhkhoi.telegram.bot.core.registry.AdviceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ndanhkhoi
 * Created at 22:59:07 May 31, 2022
 */
@Slf4j
public class BotAdvicePostProcessor implements BeanPostProcessor, SmartInitializingSingleton, Ordered, BeanFactoryAware {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private BeanFactory beanFactory;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
            Class<?> targetClass = AopUtils.getTargetClass(bean);
            BotRouteAdvice botRouteAdvice = AnnotationUtils.findAnnotation(targetClass, BotRouteAdvice.class);
            if (botRouteAdvice != null) {
                Map<Method, BotExceptionHandler> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                        (MethodIntrospector.MetadataLookup<BotExceptionHandler>) method -> AnnotationUtils.findAnnotation(method, BotExceptionHandler.class));

                if (annotatedMethods.isEmpty()) {
                    this.nonAnnotatedClasses.add(bean.getClass());
                    log.trace("No @BotExceptionHandler annotations found on bean type: " + bean.getClass());
                }
                else {
                    Flux.fromIterable(annotatedMethods.entrySet())
                            .doOnError(ex -> {
                                throw Exceptions.errorCallbackNotImplemented(ex);
                            })
                            .doAfterTerminate(() -> log.debug( annotatedMethods.size() + " @BotExceptionHandler methods processed on bean '"
                                    + beanName + "': " + annotatedMethods))
                            .subscribe(entry -> {
                                Method method = entry.getKey();
                                BotExceptionHandler botExceptionHandler = entry.getValue();
                                beanFactory.getBean(AdviceRegistry.class).register(botExceptionHandler.value(), method, bean);
                            });
                }
            }
            else {
                nonAnnotatedClasses.add(bean.getClass());
            }
        }
        return bean;
    }


    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void afterSingletonsInstantiated() {
        nonAnnotatedClasses.clear();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

}
