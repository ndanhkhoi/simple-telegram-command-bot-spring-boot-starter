package com.ndanhkhoi.telegram.bot;

import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.core.processor.ProcessorConfig;
import com.ndanhkhoi.telegram.bot.core.registry.AdviceRegistry;
import com.ndanhkhoi.telegram.bot.core.registry.CommandRegistry;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.repository.impl.InMemoryUpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.subscriber.*;
import com.ndanhkhoi.telegram.bot.subscriber.impl.*;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
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
@Import(value = {ProcessorConfig.class})
public class BotAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final BotProperties botProperties;

    @ConditionalOnProperty(value = "khoinda.bot.enable-update-trace", havingValue = "true")
    @ConditionalOnMissingBean(UpdateTraceRepository.class)
    @Bean
    UpdateTraceRepository updateTraceRepository() {
        return new InMemoryUpdateTraceRepository();
    }

    @Bean(name = "updateObjectMapper")
    UpdateObjectMapper updateObjectMapper() {
        return new UpdateObjectMapper();
    }

    @Bean
    CommandRegistry commandRegistry() {
        return new CommandRegistry();
    }

    @Bean
    AdviceRegistry adviceRegistry() {
        return new AdviceRegistry();
    }

    @ConditionalOnMissingBean(NonCommandUpdateSubscriber.class)
    @Bean
    NonCommandUpdateSubscriber defaultNonCommandUpdateSubscriber() {
        return new DefaultNonCommandUpdateSubscriber();
    }

    @ConditionalOnMissingBean(CommandNotFoundUpdateSubscriber.class)
    @Bean
    CommandNotFoundUpdateSubscriber defaultCommandNotFoundUpdateSubscriber() {
        return new DefaultCommandNotFoundUpdateSubscriber();
    }

    @ConditionalOnMissingBean(CallbackQuerySubscriber.class)
    @Bean
    CallbackQuerySubscriber defaultCallbackQuerySubscriber() {
        return new DefaultCallbackQuerySubscriber();
    }

    @ConditionalOnMissingBean(PreSubscriber.class)
    @Bean
    PreSubscriber defaultPreSubscriber() {
        return new DefaultPreSubscriber();
    }

    @ConditionalOnMissingBean(PosSubscriber.class)
    @Bean
    PosSubscriber defaultPosSubscriber() {
        return new DefaultPosSubscriber();
    }

    @Bean
    UpdateSubscriber updateSubscriber() {
        return new UpdateSubscriber();
    }

    @Bean
    SimpleAsyncTaskExecutor botAsyncTaskExecutor() {
        log.info("Creating Default Bot Async Task Executor...");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);
        executor.setMaxPoolSize(Integer.MAX_VALUE);
        executor.setQueueCapacity(Integer.MAX_VALUE);
        executor.setThreadNamePrefix("bot-task-");
        return new SimpleAsyncTaskExecutor(executor);
    }

    @SneakyThrows
    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot(CommandRegistry commandRegistry, UpdateSubscriber updateSubscriber) {
        SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot = new SimpleTelegramLongPollingCommandBot(applicationContext, botProperties, updateSubscriber, commandRegistry);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(simpleTelegramLongPollingCommandBot);
        log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi");
        return simpleTelegramLongPollingCommandBot;
    }

}