package io.github.ndanhkhoi.telegram.bot.core.registry;

import io.github.ndanhkhoi.telegram.bot.core.resolver.TypeResolver;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ndanhkhoi
 * Created at 22:02:59 February 26, 2022
 */
@Slf4j
@NoArgsConstructor
public final class ResolverRegistry {

    private final Map<Class<Object>, TypeResolver<Object>> resolverMap = new ConcurrentHashMap<>();

    public TypeResolver<Object> getResolverByType(Class<Object> type) {
        return resolverMap.get(type);
    }

    public void register(TypeResolver<Object> resolver) {
        resolverMap.putIfAbsent(resolver.getType(), resolver);
    }

    public Set<Class<Object>> getSupportedTypes() {
        return resolverMap.keySet();
    }

}