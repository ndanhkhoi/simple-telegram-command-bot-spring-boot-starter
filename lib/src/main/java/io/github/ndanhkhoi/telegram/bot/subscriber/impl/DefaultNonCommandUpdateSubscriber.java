package io.github.ndanhkhoi.telegram.bot.subscriber.impl;

import io.github.ndanhkhoi.telegram.bot.subscriber.NonCommandUpdateSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * Default handler for non command update
 */
@Slf4j
public class DefaultNonCommandUpdateSubscriber implements NonCommandUpdateSubscriber {

    @Override
    public void accept(Update update) {
        log.warn("Not a bot's command !");
    }

}
