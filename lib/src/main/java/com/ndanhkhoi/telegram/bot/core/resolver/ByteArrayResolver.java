package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.SendMediaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@Slf4j
@Component
public class ByteArrayResolver implements TypeResolver<byte[]> {

    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;


    public ByteArrayResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    @Override
    public void resolve(byte[] value, BotCommand botCommand, BotCommandParams params) {
        MediaType sendFile = botCommand.getSendFile();
        SendMediaUtils.sendMedia(params.getUpdate().getMessage(), FileUtils.getInputFile(value, "temp_" + FileUtils.getPosfixFileInstantByTime(ZoneId.systemDefault())), params.getUpdate().getMessage().getChatId(), sendFile, telegramLongPollingBot);
        log.debug("Reply Media: [{}]", sendFile);
    }

    @Override
    public Class<byte[]> getType() {
        return byte[].class;
    }

}
