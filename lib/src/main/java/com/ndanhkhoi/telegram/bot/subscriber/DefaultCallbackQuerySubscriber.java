package com.ndanhkhoi.telegram.bot.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * Default handler for non command update
 */
@Slf4j
public class DefaultCallbackQuerySubscriber implements CallbackQuerySubscriber {

    @Override
    public void accept(Update update) {
        log.info("Callback detected !!");
    }

}
