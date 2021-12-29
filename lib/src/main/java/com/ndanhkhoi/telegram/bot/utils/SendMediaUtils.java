package com.ndanhkhoi.telegram.bot.utils;

import com.google.common.collect.ImmutableMap;
import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.exception.BotException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.function.Consumer4;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author ndanhkhoi
 * Created at 00:57:26 October 05, 2021
 */
@Slf4j
@UtilityClass
public class SendMediaUtils {

    private static final Map<MediaType, Consumer4<Message, InputFile, Long, TelegramLongPollingBot>> inputfileConsumerMap = ImmutableMap.<MediaType, Consumer4<Message, InputFile, Long, TelegramLongPollingBot>>builder()
            .put(MediaType.STICKER, SendMediaUtils::sendSticker)
            .put(MediaType.DOCUMENT, SendMediaUtils::sendDocument)
            .put(MediaType.PHOTO, SendMediaUtils::sendPhoto)
            .put(MediaType.VOICE, SendMediaUtils::sendVoice)
            .build();

    public static void sendDocument(@Nullable Message messageToReply, InputFile inputFile, Long chatId, TelegramLongPollingBot bot) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(inputFile);
        if (messageToReply != null) {
            sendDocument.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendDocument.setChatId(String.valueOf(chatId));
        try {
            bot.execute(sendDocument);
        } catch (Exception ex) {
            throw new BotException(ex);
        }
    }

    public static void sendPhoto(@Nullable Message messageToReply, InputFile inputFile, Long chatId, TelegramLongPollingBot bot) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        if (messageToReply != null) {
            sendPhoto.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendPhoto.setChatId(String.valueOf(chatId));
        try {
            bot.execute(sendPhoto);
        } catch (Exception ex) {
            throw new BotException(ex);
        }
    }

    public static void sendVoice(@Nullable Message messageToReply, InputFile inputFile, Long chatId, TelegramLongPollingBot bot) {
        SendVoice sendVoice = new SendVoice();
        sendVoice.setVoice(inputFile);
        if (messageToReply != null) {
            sendVoice.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendVoice.setChatId(String.valueOf(chatId));
        try {
            bot.execute(sendVoice);
        } catch (Exception ex) {
            throw new BotException(ex);
        }
    }

    public static void sendSticker(@Nullable Message messageToReply, InputFile sticker, Long chatId, TelegramLongPollingBot bot) {
        SendSticker sendSticker = new SendSticker();
        sendSticker.setSticker(sticker);
        sendSticker.setChatId(String.valueOf(chatId));
        if (messageToReply != null) {
            sendSticker.setReplyToMessageId(messageToReply.getMessageId());
        }
        try {
            bot.execute(sendSticker);
        } catch (Exception ex) {
            throw new BotException(ex);
        }
    }

    public static void sendMedia(@Nullable Message message, InputFile inputFile, Long chatId, MediaType mediaType, TelegramLongPollingBot bot) {
        if (inputfileConsumerMap.containsKey(mediaType)) {
            inputfileConsumerMap.get(mediaType).accept(message, inputFile, chatId, bot);
        }
    }

}
