package io.github.ndanhkhoi.telegram.bot.utils;

import io.github.ndanhkhoi.telegram.bot.constant.MediaType;
import jakarta.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import reactor.function.Consumer4;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ndanhkhoi
 * Created at 00:57:26 October 05, 2021
 */
@Slf4j
@UtilityClass
public class SendMediaUtils {

    private static final Map<MediaType, Consumer4<Message, InputFile, Long, AbsSender>> inputfileConsumerMap = new HashMap<>();

    static {
        inputfileConsumerMap.put(MediaType.STICKER, SendMediaUtils::sendSticker);
        inputfileConsumerMap.put(MediaType.DOCUMENT, SendMediaUtils::sendDocument);
        inputfileConsumerMap.put(MediaType.PHOTO, SendMediaUtils::sendPhoto);
        inputfileConsumerMap.put(MediaType.VOICE, SendMediaUtils::sendVoice);
    }

    @SneakyThrows
    public static void sendDocument(@Nullable Message messageToReply, InputFile inputFile, Long chatId, AbsSender bot) {
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(inputFile);
        if (messageToReply != null) {
            sendDocument.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendDocument.setChatId(String.valueOf(chatId));
        bot.execute(sendDocument);
    }

    @SneakyThrows
    public static void sendPhoto(@Nullable Message messageToReply, InputFile inputFile, Long chatId, AbsSender bot) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setPhoto(inputFile);
        if (messageToReply != null) {
            sendPhoto.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendPhoto.setChatId(String.valueOf(chatId));
        bot.execute(sendPhoto);
    }

    @SneakyThrows
    public static void sendVoice(@Nullable Message messageToReply, InputFile inputFile, Long chatId, AbsSender bot) {
        SendVoice sendVoice = new SendVoice();
        sendVoice.setVoice(inputFile);
        if (messageToReply != null) {
            sendVoice.setReplyToMessageId(messageToReply.getMessageId());
        }
        sendVoice.setChatId(String.valueOf(chatId));
        bot.execute(sendVoice);
    }

    @SneakyThrows
    public static void sendSticker(@Nullable Message messageToReply, InputFile sticker, Long chatId, AbsSender bot) {
        SendSticker sendSticker = new SendSticker();
        sendSticker.setSticker(sticker);
        sendSticker.setChatId(String.valueOf(chatId));
        if (messageToReply != null) {
            sendSticker.setReplyToMessageId(messageToReply.getMessageId());
        }
        bot.execute(sendSticker);
    }

    public static void sendMedia(@Nullable Message message, InputFile inputFile, Long chatId, MediaType mediaType, AbsSender bot) {
        if (inputfileConsumerMap.containsKey(mediaType)) {
            inputfileConsumerMap.get(mediaType).accept(message, inputFile, chatId, bot);
        }
    }

}
