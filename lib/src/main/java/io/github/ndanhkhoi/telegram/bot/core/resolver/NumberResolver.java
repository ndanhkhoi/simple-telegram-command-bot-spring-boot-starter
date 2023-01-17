package io.github.ndanhkhoi.telegram.bot.core.resolver;

import io.github.ndanhkhoi.telegram.bot.constant.MessageParseMode;
import io.github.ndanhkhoi.telegram.bot.core.BotDispatcher;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import io.github.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.Objects;

/**
 * Created at 15:26:04 January 17, 2023,
 */
@Slf4j
@NoArgsConstructor
public class NumberResolver implements TypeResolver<Number> {

    @Override
    public void resolve(Number value, BotCommand botCommand, BotCommandParams params) {
        Message message = params.getUpdate().getMessage();
        MessageParseMode parseMode = botCommand.getParseMode();
        TelegramMessageUtils.replyMessage(BotDispatcher.getInstance().getSender(), message, Objects.toString(value), parseMode, botCommand.isDisableWebPagePreview());
        log.debug("Reply Message: {}", value);
    }

    @Override
    public Class<Number> getType() {
        return Number.class;
    }

}
