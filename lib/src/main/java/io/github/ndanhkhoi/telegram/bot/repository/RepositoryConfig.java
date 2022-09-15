package io.github.ndanhkhoi.telegram.bot.repository;

import io.github.ndanhkhoi.telegram.bot.repository.impl.InMemoryUpdateTraceRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author ndanhkhoi
 * Created at 10:17:09 July 02, 2022
 */
public class RepositoryConfig {

    @ConditionalOnProperty(value = "khoinda.bot.enable-update-trace", havingValue = "true")
    @ConditionalOnMissingBean(UpdateTraceRepository.class)
    @Bean
    UpdateTraceRepository updateTraceRepository() {
        return new InMemoryUpdateTraceRepository();
    }

}
