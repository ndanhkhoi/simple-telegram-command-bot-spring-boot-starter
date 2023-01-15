package io.github.ndanhkhoi.telegram.bot.core.resolver;

import io.github.ndanhkhoi.telegram.bot.annotation.ConditionalOnMissingTypeResolverBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.Collection;

/**
 * @author khoinda
 * Created at 09:33:31 July 01, 2022
 */
@SuppressWarnings("rawtypes")
public class TypeResolverConfig {

    @Bean
    @ConditionalOnMissingTypeResolverBean(BotApiMethod.class)
    TypeResolver<BotApiMethod> botApiMethodTypeResolver() {
        return new BotApiMethodResolver();
    }


    @Bean
    @ConditionalOnMissingTypeResolverBean(byte[].class)
    TypeResolver<byte[]> byteArrayResolver() {
        return new ByteArrayResolver();
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(ByteArrayResource.class)
    TypeResolver<ByteArrayResource> byteArrayResourceTypeResolver() {
        return new ByteArrayResourceResolver();
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(File.class)
    TypeResolver<File> fileTypeResolver() {
        return new FileResolver();
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(InputFile.class)
    TypeResolver<InputFile> inputFileResolver() {
        return new InputFileResolver();
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(String.class)
    TypeResolver<String> stringResolver() {
        return new StringResolver();
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(Collection.class)
    TypeResolver<Collection> collectionTypeResolver() {
        return new CollectiomResolver();
    }

}
