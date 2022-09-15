package io.github.ndanhkhoi.telegram.bot.core.resolver;

import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import io.github.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@SuppressWarnings("rawtypes")
@Slf4j
public class CollectiomResolver implements TypeResolver<Collection>, ApplicationContextAware {
    private ApplicationContext applicationContext;


    public CollectiomResolver() {
        // Default constructor
    }

    @Override
    public void resolve(Collection value, BotCommand botCommand, BotCommandParams params) {
        UpdateSubscriber updateSubscriber = applicationContext.getBean(UpdateSubscriber.class);
        if (value.isEmpty()) {
            log.info("Nothing to reply. Cause return value(s) is empty collection/array");
        }
        else {
            value.forEach(e -> updateSubscriber.handleReturnedValue(() -> e, botCommand, params));
        }
    }

    @Override
    public Class<Collection> getType() {
        return Collection.class;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
