package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import reactor.function.Consumer4;

import java.time.ZoneId;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public class ByteArrayResolver extends TypeResolver<byte[]> {

    private ByteArrayResolver(Class<byte[]> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

    public final static ByteArrayResolver INSTANCE = new ByteArrayResolver(byte[].class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                MediaType sendFile = botCommand.getSendFile();
                SendMediaUtils.sendMedia(botCommandParams.getUpdate().getMessage(), FileUtils.getInputFile((byte[]) value, "temp_" + FileUtils.getPosfixFileInstantByTime(ZoneId.systemDefault())), botCommandParams.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
                LOGGER.info("Reply Media: [{}]", sendFile);
            }
        );

}
