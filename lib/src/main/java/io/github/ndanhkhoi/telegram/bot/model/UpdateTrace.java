package io.github.ndanhkhoi.telegram.bot.model;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Calendar;
import java.util.Date;

/**
 * @author ndanhkhoi
 * Created at 19:05:00 April 29, 2022
 */
@Getter
public class UpdateTrace {

    private final Date timestamp;
    private final long startNanoTime;
    private final Update update;

    public UpdateTrace(Update update) {
        this.update = update;
        this.timestamp = Calendar.getInstance().getTime();
        this.startNanoTime = System.nanoTime();
    }

}
