package com.ndanhkhoi.telegram.bot;

import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan
@EnableConfigurationProperties({BotProperties.class})
public class BotAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final BotProperties botProperties;

    @SneakyThrows
    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot() {
        SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot = new SimpleTelegramLongPollingCommandBot(botProperties, applicationContext);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(simpleTelegramLongPollingCommandBot);
        log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi %28%u256F%B0%u25A1%B0%uFF09%u256F%uFE35%20%u253B%u2501%u253B");
        return simpleTelegramLongPollingCommandBot;
    }

}