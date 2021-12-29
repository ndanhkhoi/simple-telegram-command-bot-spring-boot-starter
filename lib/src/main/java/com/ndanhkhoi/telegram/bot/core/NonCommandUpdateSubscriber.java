package com.ndanhkhoi.telegram.bot.core;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.function.Consumer;

/**
 * @author ndanhkhoi
 * Created at 20:57:14 December 28, 2021
 * An abstract class of handler for non command update
 */
public abstract class NonCommandUpdateSubscriber implements Consumer<Update> {
}
