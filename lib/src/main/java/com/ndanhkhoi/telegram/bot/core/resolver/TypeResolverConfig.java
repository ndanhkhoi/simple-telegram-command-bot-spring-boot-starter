package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.annotation.ConditionalOnMissingTypeResolverBean;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
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
public class TypeResolverConfig {

    @Bean
    @ConditionalOnMissingTypeResolverBean(BotApiMethod.class)
    TypeResolver<BotApiMethod> botApiMethodTypeResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new BotApiMethodResolver(telegramLongPollingCommandBot);
    }


    @Bean
    @ConditionalOnMissingTypeResolverBean(byte[].class)
    TypeResolver<byte[]> byteArrayResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new ByteArrayResolver(telegramLongPollingCommandBot);
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(ByteArrayResource.class)
    TypeResolver<ByteArrayResource> byteArrayResourceTypeResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new ByteArrayResourceResolver(telegramLongPollingCommandBot);
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(File.class)
    TypeResolver<File> fileTypeResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new FileResolver(telegramLongPollingCommandBot);
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(InputFile.class)
    TypeResolver<InputFile> inputFileResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new InputFileResolver(telegramLongPollingCommandBot);
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(String.class)
    TypeResolver<String> stringResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new StringResolver(telegramLongPollingCommandBot);
    }

    @Bean
    @ConditionalOnMissingTypeResolverBean(Collection.class)
    TypeResolver<Collection> collectionTypeResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingCommandBot) {
        return new CollectiomResolver(telegramLongPollingCommandBot);
    }

}
