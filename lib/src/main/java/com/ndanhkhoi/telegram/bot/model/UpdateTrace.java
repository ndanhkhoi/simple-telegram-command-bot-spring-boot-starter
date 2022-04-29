package com.ndanhkhoi.telegram.bot.model;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Instant;

/**
 * @author ndanhkhoi
 * Created at 19:05:00 April 29, 2022
 */
@Getter
public class UpdateTrace {

    private final Instant timestamp;
    private final long startNanoTime;
    private final Update update;

    public UpdateTrace(Update update) {
        this.update = update;
        this.timestamp = Instant.now();
        this.startNanoTime = System.nanoTime();
    }

}
