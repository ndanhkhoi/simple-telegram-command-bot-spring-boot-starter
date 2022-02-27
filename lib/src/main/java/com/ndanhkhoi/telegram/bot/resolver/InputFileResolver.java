package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import reactor.function.Consumer4;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public class InputFileResolver extends TypeResolver<InputFile> {

    private InputFileResolver(Class<InputFile> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

    public final static InputFileResolver INSTANCE = new InputFileResolver(InputFile.class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                MediaType sendFile = botCommand.getSendFile();
                SendMediaUtils.sendMedia(botCommandParams.getUpdate().getMessage(), (InputFile) value, botCommandParams.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
                LOGGER.info("Reply Media: [{}]", sendFile);
            }
        );

}
