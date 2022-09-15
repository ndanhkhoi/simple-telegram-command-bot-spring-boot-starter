package io.github.ndanhkhoi.telegram.bot.core.registry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ndanhkhoi
 * Created at 22:53:13 May 31, 2022
 */
@NoArgsConstructor
public class AdviceRegistry {

    private final Map<Class<? extends Throwable>, Advice> adviceMap = new ConcurrentHashMap<>();

    public void register(Class<? extends Throwable>[] classes, Method method, Object bean) {
        for (Class<? extends Throwable> clazz : classes) {
            adviceMap.put(clazz, new Advice(bean, method));
        }
    }

    public boolean hasAdvice(Class<? extends Throwable> clazz) {
        return adviceMap.containsKey(clazz);
    }

    public Advice getAdvice(Class<? extends Throwable> clazz) {
        return adviceMap.get(clazz);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Advice {
        private final Object bean;
        private final Method method;
    }

}
