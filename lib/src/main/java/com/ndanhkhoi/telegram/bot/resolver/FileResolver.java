package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

import java.io.File;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public class FileResolver extends TypeResolver<File> {

    public static final FileResolver INSTANCE = new FileResolver(File.class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                MediaType sendFile = botCommand.getSendFile();
                SendMediaUtils.sendMedia(botCommandParams.getUpdate().getMessage(), FileUtils.getInputFile((File) value), botCommandParams.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
                LOGGER.info("Reply Media: [{}]", sendFile);
            }
    );

    private FileResolver(Class<File> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

}
