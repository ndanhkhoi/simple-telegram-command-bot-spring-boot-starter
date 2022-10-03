package io.github.ndanhkhoi.telegram.bot.exception;

/**
 * Created at 16:44:08 October 03, 2022,
 */
public class BotAccessDeniedException extends RuntimeException {

    public BotAccessDeniedException() {
        super();
    }

    public BotAccessDeniedException(Throwable throwable) {
        super(throwable);
    }

    public BotAccessDeniedException(String msg) {
        super(msg);
    }

}
