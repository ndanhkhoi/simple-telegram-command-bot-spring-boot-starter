package io.github.ndanhkhoi.telegram.bot.core.resolver;

import io.github.ndanhkhoi.telegram.bot.constant.MediaType;
import io.github.ndanhkhoi.telegram.bot.core.BotDispatcher;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import io.github.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.InputFile;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@Slf4j
@NoArgsConstructor
public class InputFileResolver implements TypeResolver<InputFile> {

    @Override
    public void resolve(InputFile value, BotCommand botCommand, BotCommandParams params) {
        MediaType sendFile = botCommand.getSendFile();
        SendMediaUtils.sendMedia(params.getUpdate().getMessage(), value, params.getUpdate().getMessage().getChatId(), sendFile, BotDispatcher.getInstance().getSender());
        log.debug("Reply Media: [{}]", sendFile);
    }

    @Override
    public Class<InputFile> getType() {
        return InputFile.class;
    }

}
