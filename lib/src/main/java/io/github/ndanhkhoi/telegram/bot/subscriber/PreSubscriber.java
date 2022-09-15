package io.github.ndanhkhoi.telegram.bot.subscriber;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

/**
 * @author ndanhkhoi
 * Created at 20:12:10 June 25, 2022
 */
public interface PreSubscriber extends Consumer<Update> {
}
