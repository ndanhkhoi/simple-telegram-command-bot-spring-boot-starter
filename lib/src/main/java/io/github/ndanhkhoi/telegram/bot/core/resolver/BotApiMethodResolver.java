package io.github.ndanhkhoi.telegram.bot.core.resolver;

import io.github.ndanhkhoi.telegram.bot.core.BotDispatcher;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@SuppressWarnings("rawtypes")
@Slf4j
@NoArgsConstructor
public class BotApiMethodResolver implements TypeResolver<BotApiMethod> {

    @Override
    public void resolve(BotApiMethod value, BotCommand botCommand, BotCommandParams params) {
        BotDispatcher.getInstance().executeSneakyThrows(value);
        log.debug("Excuted API method {}", value);
    }

    @Override
    public Class<BotApiMethod> getType() {
        return BotApiMethod.class;
    }

}
