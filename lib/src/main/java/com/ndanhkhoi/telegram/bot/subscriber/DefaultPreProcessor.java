package com.ndanhkhoi.telegram.bot.subscriber;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * @author ndanhkhoi
 * Created at 20:13:20 June 25, 2022
 */
@Slf4j
public class DefaultPreProcessor implements PreProcessor {

    @Override
    public void accept(Update update) {
        log.trace("DefaultPreProcessor...");
    }

}
