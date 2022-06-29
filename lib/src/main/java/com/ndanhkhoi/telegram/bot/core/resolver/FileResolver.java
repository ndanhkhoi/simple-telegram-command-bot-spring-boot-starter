package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@Slf4j
@Component
public class FileResolver implements TypeResolver<File> {
    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;

    public FileResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    @Override
    public void resolve(File value, BotCommand botCommand, BotCommandParams params) {
        MediaType sendFile = botCommand.getSendFile();
        SendMediaUtils.sendMedia(params.getUpdate().getMessage(), FileUtils.getInputFile(value), params.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
        log.debug("Reply Media: [{}]", sendFile);
    }

    @Override
    public Class<File> getType() {
        return File.class;
    }

}
