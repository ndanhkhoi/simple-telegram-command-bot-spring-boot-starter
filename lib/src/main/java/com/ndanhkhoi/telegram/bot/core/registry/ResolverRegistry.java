package com.ndanhkhoi.telegram.bot.core.registry;

import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.resolver.TypeResolver;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 22:02:59 February 26, 2022
 */
@Slf4j
@Component
public final class ResolverRegistry implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private final Map<Class<Object>, TypeResolver<Object>> resolverMap = new ConcurrentHashMap<>();

    private <T> TypeResolver<Object> getResolverByType(Class<T> type) {
        return resolverMap.get(type);
    }

    public void register(TypeResolver<Object> resolver) {
        Class<Object> key = resolver.getType();
        if (!resolverMap.containsKey(key)) {
            resolverMap.put(key, resolver);
        }
    }

    public Set<String> getSimpleNameSupportedTypes() {
        return getSupportedTypes()
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
    }

    public Set<Class<Object>> getSupportedTypes() {
        return resolverMap.keySet();
    }

    public boolean isSupportedType(Class<?> type) {
        return resolverMap.containsKey(type);
    }

    public void onErrorHandle(BotCommandParams params, Throwable throwable) {
        TelegramLongPollingBot telegramLongPollingBot = applicationContext.getBean(TelegramLongPollingBot.class);
        log.error("Error!", throwable);
        TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), CommonConstant.ERROR_NOTIFY_MESSAGE, false);
    }

    private <T> void resolveCollection(Collection<T> valueCollection, BotCommand botCommand, BotCommandParams botCommandParams) {
        if (valueCollection.isEmpty()) {
            log.info("Nothing to reply. Cause return value(s) is empty collection/array");
        } else {
            valueCollection.stream().findFirst()
                    .ifPresent(e -> {
                        Class<?> typeOfElement = e.getClass();
                        if (isSupportedType(typeOfElement)) {
                            valueCollection.forEach(value -> resolve(value, botCommand, botCommandParams));
                        } else {
                            log.warn("Nothing to reply. Cause the return type is not supported ({}}). Supported types are: {}", typeOfElement.getSimpleName(), getSimpleNameSupportedTypes());
                        }
                    });
        }
    }

    private void resolveByType(Object value, BotCommand botCommand, BotCommandParams botCommandParams) {
        TelegramLongPollingBot bot = applicationContext.getBean(TelegramLongPollingBot.class);
        if (value == null) {
            log.info("Nothing to reply. Cause return value is null or it's type is Void");
            return;
        }
        Class<?> type = value.getClass();
        if (type.isArray() && (type.getComponentType() != byte.class)) {
            Collection<Object> collection = Arrays.stream((Object[]) value)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            resolveCollection(collection, botCommand, botCommandParams);
            return;
        }
        if (value instanceof Collection) {
            resolveCollection((Collection<?>) value, botCommand, botCommandParams);
            return;
        }
        getSupportedTypes()
                .stream()
                .filter(e -> e.isAssignableFrom(type))
                .findFirst()
                .ifPresent(e -> getResolverByType(e).resolve(value, botCommand, botCommandParams));
    }

    public void resolve(Object value, BotCommand botCommand, BotCommandParams botCommandParams) {
        Executor executor = applicationContext.getBean("botAsyncTaskExecutor", SimpleAsyncTaskExecutor.class);
        if (value instanceof Mono) {
            ((Mono<?>) value)
                    .subscribeOn(Schedulers.fromExecutor(executor))
                    .doOnError(t -> onErrorHandle(botCommandParams, t))
                    .subscribe(e -> resolveByType(e, botCommand, botCommandParams));
        } else if (value instanceof Flux) {
            ((Flux<?>) value)
                    .subscribeOn(Schedulers.fromExecutor(executor))
                    .doOnError(t -> onErrorHandle(botCommandParams, t))
                    .subscribe(e -> resolveByType(e, botCommand, botCommandParams));
        } else {
            resolveByType(value, botCommand, botCommandParams);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}