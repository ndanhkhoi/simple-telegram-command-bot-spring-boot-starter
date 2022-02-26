package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandArgs;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import org.springframework.core.io.ByteArrayResource;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public class ByteArrayResourceResolver extends TypeResolver<ByteArrayResource> {

    private ByteArrayResourceResolver(Class<ByteArrayResource> type, Consumer4<Object, BotCommand, BotCommandArgs, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

    public final static ByteArrayResourceResolver INSTANCE = new ByteArrayResourceResolver(ByteArrayResource.class,
            (value, botCommand, botCommandArgs, telegramLongPollingBot) -> {
                MediaType sendFile = botCommand.getSendFile();
                SendMediaUtils.sendMedia(botCommandArgs.getUpdate().getMessage(), FileUtils.getInputFile((ByteArrayResource) value), botCommandArgs.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
                LOGGER.info("Reply Media: [{}]", sendFile);
            }
        );

}
