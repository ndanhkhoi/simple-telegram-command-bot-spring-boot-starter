package io.github.ndanhkhoi.telegram.bot.subscriber;

import io.github.ndanhkhoi.telegram.bot.subscriber.impl.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * @author ndanhkhoi
 * Created at 10:15:47 July 02, 2022
 */
public class SubscriberConfig {

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

    @ConditionalOnMissingBean(AfterRegisterBotSubscriber.class)
    @Bean
    AfterRegisterBotSubscriber afterRegisterBotSubscriber() {
        return new DefaultAfterRegisterBotSubscriber();
    }

    @Bean
    UpdateSubscriber updateSubscriber() {
        return new UpdateSubscriber();
    }

}
