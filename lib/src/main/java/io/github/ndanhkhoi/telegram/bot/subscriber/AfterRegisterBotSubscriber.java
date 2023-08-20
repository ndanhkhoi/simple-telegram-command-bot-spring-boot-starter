package io.github.ndanhkhoi.telegram.bot.subscriber;

import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.function.Consumer;

public interface AfterRegisterBotSubscriber extends Consumer<AbsSender> {
}
