package com.ndanhkhoi.telegram.bot;

import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.repository.impl.InMemoryUpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.utils.SpringHelper;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@RequiredArgsConstructor
@Configuration
@ComponentScan
@EnableConfigurationProperties({BotProperties.class})
public class BotAutoConfiguration {

    private final ApplicationContext applicationContext;
    private final Environment environment;
    private final BotProperties botProperties;

    @Bean
    SpringHelper springHelper() {
        return new SpringHelper(applicationContext, environment);
    }

    @ConditionalOnProperty(value = "khoinda.bot.enable-update-trace", havingValue = "true")
    @Bean
    UpdateTraceRepository updateTraceRepository() {
        return new InMemoryUpdateTraceRepository();
    }

    @Bean(name = "updateObjectMapper")
    UpdateObjectMapper updateObjectMapper() {
        return new UpdateObjectMapper();
    }

    @SneakyThrows
    @Bean
    SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot(SpringHelper springHelper, @Qualifier("updateObjectMapper") UpdateObjectMapper updateObjectMapper) {
        UpdateTraceRepository updateTraceRepository = springHelper.existBean(UpdateTraceRepository.class) ?
                springHelper.getBean(UpdateTraceRepository.class) : null;
        SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot = new SimpleTelegramLongPollingCommandBot(botProperties, springHelper, updateTraceRepository, updateObjectMapper);
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(simpleTelegramLongPollingCommandBot);
        log.info("Spring Boot Telegram Command Bot Auto Configuration by @ndanhkhoi");
        return simpleTelegramLongPollingCommandBot;
    }

}