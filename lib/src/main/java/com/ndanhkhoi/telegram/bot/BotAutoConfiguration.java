package com.ndanhkhoi.telegram.bot;

import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.BotRouteConfig;
import com.ndanhkhoi.telegram.bot.core.CommandRegistry;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.repository.impl.InMemoryUpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.subscriber.*;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan
@EnableConfigurationProperties({BotProperties.class})
@Import(value = {BotRouteConfig.class})
public class BotAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final BotProperties botProperties;

    @ConditionalOnProperty(value = "khoinda.bot.enable-update-trace", havingValue = "true")
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

    @Bean
    UpdateSubscriber updateSubscriber(UpdateObjectMapper updateObjectMapper, @Autowired(required = false) UpdateTraceRepository updateTraceRepository) {
        return new UpdateSubscriber(applicationContext, botProperties, updateObjectMapper,updateTraceRepository);
    }

    @SneakyThrows
    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot(UpdateSubscriber updateSubscriber, CommandRegistry commandRegistry) {
        SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot = new SimpleTelegramLongPollingCommandBot(applicationContext, botProperties, updateSubscriber, commandRegistry);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(simpleTelegramLongPollingCommandBot);
        log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi");
        return simpleTelegramLongPollingCommandBot;
    }

}