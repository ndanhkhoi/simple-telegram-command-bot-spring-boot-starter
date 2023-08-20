package io.github.ndanhkhoi.telegram.bot.subscriber.impl;

import io.github.ndanhkhoi.telegram.bot.subscriber.AfterRegisterBotSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Slf4j
public class DefaultAfterRegisterBotSubscriber implements AfterRegisterBotSubscriber {

    @Override
    public void accept(AbsSender absSender) {
        log.trace("AfterRegisterBot detected !!");
    }

}
