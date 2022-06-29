package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.InputFile;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@Slf4j
@Component
public class InputFileResolver implements TypeResolver<InputFile> {

    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;

    public InputFileResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    @Override
    public void resolve(InputFile value, BotCommand botCommand, BotCommandParams params) {
        MediaType sendFile = botCommand.getSendFile();
        SendMediaUtils.sendMedia(params.getUpdate().getMessage(), value, params.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
        log.debug("Reply Media: [{}]", sendFile);
    }

    @Override
    public Class<InputFile> getType() {
        return InputFile.class;
    }

}
