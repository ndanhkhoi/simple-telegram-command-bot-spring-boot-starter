package com.ndanhkhoi.telegram.bot;

import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.core.processor.ProcessorConfig;
import com.ndanhkhoi.telegram.bot.core.registry.CommandRegistry;
import com.ndanhkhoi.telegram.bot.core.registry.RegistryConfig;
import com.ndanhkhoi.telegram.bot.core.resolver.TypeResolverConfig;
import com.ndanhkhoi.telegram.bot.repository.RepositoryConfig;
import com.ndanhkhoi.telegram.bot.subscriber.SubscriberConfig;
import com.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan
@EnableConfigurationProperties({BotProperties.class})
@Import({
        ProcessorConfig.class,
        RegistryConfig.class,
        TypeResolverConfig.class,
        SubscriberConfig.class,
        RepositoryConfig.class
})
public class BotAutoConfiguration {
    private final BotProperties botProperties;

    @Bean(name = "updateObjectMapper")
    UpdateObjectMapper updateObjectMapper() {
        return new UpdateObjectMapper();
    }

    @Bean
    SimpleAsyncTaskExecutor botAsyncTaskExecutor() {
        log.info("Creating Default Bot Async Task Executor...");
        BotProperties.Executor executorProperties = botProperties.getExecutor();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(executorProperties.getCorePoolSize());
        executor.setMaxPoolSize(executorProperties.getMaxPoolSize());
        executor.setQueueCapacity(executorProperties.getQueueCapacity());
        executor.setThreadNamePrefix(executorProperties.getThreadNamePrefix());
        return new SimpleAsyncTaskExecutor(executor);
    }

    @SneakyThrows
    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot(CommandRegistry commandRegistry, UpdateSubscriber updateSubscriber) {
        SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot = new SimpleTelegramLongPollingCommandBot(botProperties, updateSubscriber, commandRegistry);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(simpleTelegramLongPollingCommandBot);
        log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi");
        return simpleTelegramLongPollingCommandBot;
    }

}