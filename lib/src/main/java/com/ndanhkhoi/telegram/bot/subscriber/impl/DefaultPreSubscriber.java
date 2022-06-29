package com.ndanhkhoi.telegram.bot.subscriber.impl;

import com.ndanhkhoi.telegram.bot.subscriber.PreSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author ndanhkhoi
 * Created at 20:13:20 June 25, 2022
 */
@Slf4j
public class DefaultPreSubscriber implements PreSubscriber {

    @Override
    public void accept(Update update) {
        log.trace("DefaultPreProcessor...");
    }

}
