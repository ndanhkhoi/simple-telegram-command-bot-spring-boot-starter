package com.ndanhkhoi.telegram.bot.exception;

/**
 * @author ndanhkhoi
 * Created at 00:52:40 October 06, 2021
 */
public class BotException extends RuntimeException {

    public BotException() {
        super();
    }

    public BotException(Throwable throwable) {
        super(throwable);
    }

}
