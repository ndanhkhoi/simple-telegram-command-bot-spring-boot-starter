package com.ndanhkhoi.telegram.bot.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * Default handler for non command update
 */
@Slf4j
public class DefaultCommandNotFoundUpdateSubscriber implements CommandNotFoundUpdateSubscriber {

    @Override
    public void accept(Update update, String cmd) {
        log.warn("No route match for command: {}", cmd);
    }

}
