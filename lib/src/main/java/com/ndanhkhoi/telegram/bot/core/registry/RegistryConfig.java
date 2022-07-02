package com.ndanhkhoi.telegram.bot.core.registry;

import org.springframework.context.annotation.Bean;

/**
 * @author ndanhkhoi
 * Created at 10:07:48 July 02, 2022
 */
public class RegistryConfig {

    @Bean
    CommandRegistry commandRegistry() {
        return new CommandRegistry();
    }

    @Bean
    AdviceRegistry adviceRegistry() {
        return new AdviceRegistry();
    }

    @Bean
    ResolverRegistry resolverRegistry() {
        return new ResolverRegistry();
    }

}
