package io.github.ndanhkhoi.telegram.bot.subscriber;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.BiConsumer;

/**
 * @author ndanhkhoi
 * Created at 20:18:05 March 15, 2022
 */
public interface CommandNotFoundUpdateSubscriber extends BiConsumer<Update, String> {
}
