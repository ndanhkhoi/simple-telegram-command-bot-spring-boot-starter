package com.ndanhkhoi.telegram.bot.subscriber;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * An abstract class of handler for non command update
 */
public interface NonCommandUpdateSubscriber extends Consumer<Update> {
}
