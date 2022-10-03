package io.github.ndanhkhoi.telegram.bot;

import io.github.ndanhkhoi.telegram.bot.core.BotProperties;
import io.github.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import io.github.ndanhkhoi.telegram.bot.core.processor.ProcessorConfig;
import io.github.ndanhkhoi.telegram.bot.core.registry.RegistryConfig;
import io.github.ndanhkhoi.telegram.bot.core.resolver.TypeResolverConfig;
import io.github.ndanhkhoi.telegram.bot.exception.BotException;
import io.github.ndanhkhoi.telegram.bot.mapper.MapperConfig;
import io.github.ndanhkhoi.telegram.bot.repository.RepositoryConfig;
import io.github.ndanhkhoi.telegram.bot.subscriber.SubscriberConfig;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(value = "khoinda.bot.enable-auto-config", havingValue = "true", matchIfMissing = true)
@Configuration
@ComponentScan
@EnableConfigurationProperties({BotProperties.class})
@Import({
        ProcessorConfig.class,
        RegistryConfig.class,
        TypeResolverConfig.class,
        SubscriberConfig.class,
        RepositoryConfig.class,
        MapperConfig.class
})
public class BotAutoConfiguration {
    private final BotProperties botProperties;
    private final ApplicationContext applicationContext;

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

    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot() {
        return new SimpleTelegramLongPollingCommandBot(botProperties);
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void registerBot() {
        SimpleTelegramLongPollingCommandBot bot = applicationContext.getBean(SimpleTelegramLongPollingCommandBot.class);
        Mono.just(new TelegramBotsApi(DefaultBotSession.class))
                .delaySubscription(Duration.ofSeconds(botProperties.getRegisterDelay()))
                .doOnSuccess(api -> log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi"))
                .doOnError(ex -> {
                    throw Exceptions.errorCallbackNotImplemented(ex);
                })
                .subscribe(api -> {
                    try {
                        api.registerBot(bot);
                    }
                    catch (Exception ex) {
                        throw new BotException(ex);
                    }
                });
    }

}
