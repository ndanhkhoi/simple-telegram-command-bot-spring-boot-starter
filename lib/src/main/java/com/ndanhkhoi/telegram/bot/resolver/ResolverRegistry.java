package com.ndanhkhoi.telegram.bot.resolver;

import com.google.common.collect.ImmutableMap;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandArgs;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.Consumer4;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ndanhkhoi
 * Created at 22:02:59 February 26, 2022
 */
@Slf4j
public class ResolverRegistry {

    private final ImmutableMap<Class<?>, Consumer4<Object, BotCommand, BotCommandArgs, TelegramLongPollingBot>> resolverMap;

    public static final ResolverRegistry INSTANCE = new ResolverRegistry(ByteArrayResolver.INSTANCE, ByteArrayResourceResolver.INSTANCE, FileResolver.INSTANCE, InputFileResolver.INSTANCE, StringResolver.INSTANCE);

    private ResolverRegistry(TypeResolver<?> ... typeResolvers) {
        this.resolverMap = ImmutableMap.copyOf(Stream.of(typeResolvers)
                .collect(Collectors.toMap(TypeResolver::getType, TypeResolver::getResolver)));
    }

    private Consumer4<Object, BotCommand, BotCommandArgs, TelegramLongPollingBot> getResolverByType(Class<?> type) {
        return resolverMap.get(type);
    }

    public Set<String> getSimpleNameSupportedTypes() {
        return getSupportedTypes()
                .stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toSet());
    }

    public Set<Class<?>> getSupportedTypes() {
        return resolverMap.keySet();
    }

    public boolean isSupportedType(Class<?> type) {
        return resolverMap.containsKey(type);
    }

    private Consumer<? super Throwable> onErrorHandle(BotCommandArgs params, TelegramLongPollingBot telegramLongPollingBot) {
        return throwable -> {
            log.error("Error!", throwable);
            TelegramMessageUtils.replyMessage(telegramLongPollingBot, params.getUpdate().getMessage(), CommonConstant.ERROR_NOTIFY_MESSAGE,false);
        };
    }

    private <T> void resolveCollection(Collection<T> valueCollection, BotCommand botCommand, BotCommandArgs botCommandArgs, TelegramLongPollingBot bot) {
        if (valueCollection.isEmpty()) {
            log.info("Nothing to reply. Cause return value(s) is empty collection/array");
        }
        else {
            Class<?> typeOfElement = valueCollection.stream().findFirst().get().getClass();
            if (isSupportedType(typeOfElement)) {
                valueCollection.forEach(e -> resolve(e, botCommand, botCommandArgs, bot));
            }
            else {
                log.warn("Nothing to reply. Cause the return type is not supported ({}}). Supported types are: {}", typeOfElement.getSimpleName(), getSimpleNameSupportedTypes());
            }
        }
    }

    private void resolveByType(Object value, BotCommand botCommand, BotCommandArgs botCommandArgs, TelegramLongPollingBot bot) {
        if (value == null) {
            log.info("Nothing to reply. Cause return value is null or it's type is Void");
            return;
        }
        Class<?> type = value.getClass();
        if (type.isArray()) {
            Collection<Object> collection = Arrays.stream((Object[]) value)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            resolveCollection(collection, botCommand, botCommandArgs, bot);
            return;
        }
        if (value instanceof Collection) {
            resolveCollection((Collection<?>) value, botCommand, botCommandArgs, bot);
            return;
        }
        getSupportedTypes()
                .stream()
                .filter(e -> e.isAssignableFrom(type))
                .findFirst()
                .ifPresent(e -> getResolverByType(e).accept(value, botCommand, botCommandArgs, bot));
    }

    public void resolve(Object value, BotCommand botCommand, BotCommandArgs botCommandArgs, TelegramLongPollingBot bot) {
        if (value instanceof Mono) {
            ((Mono<?>) value)
                    .doOnError(onErrorHandle(botCommandArgs, bot))
                    .subscribe(e -> resolveByType(e, botCommand, botCommandArgs, bot));
        }
        else if (value instanceof Flux) {
            ((Flux<?>) value)
                    .subscribeOn(Schedulers.parallel())
                    .doOnError(onErrorHandle(botCommandArgs, bot))
                    .subscribe(e -> resolveByType(e, botCommand, botCommandArgs, bot));
        }
        else {
            resolveByType(value, botCommand, botCommandArgs, bot);
        }
    }

}