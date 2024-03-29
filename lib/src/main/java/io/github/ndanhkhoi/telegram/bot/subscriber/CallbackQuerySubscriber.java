package io.github.ndanhkhoi.telegram.bot.subscriber;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * An abstract class of handler for callback query
 */
public interface CallbackQuerySubscriber extends Consumer<Update> {
}
