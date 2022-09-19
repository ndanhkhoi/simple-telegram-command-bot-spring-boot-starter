package io.github.ndanhkhoi.telegram.bot.utils;

import io.github.ndanhkhoi.telegram.bot.constant.MessageParseMode;
import io.github.ndanhkhoi.telegram.bot.constant.TelegramTextStyled;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@UtilityClass
public final class TelegramMessageUtils {

    public static String wrapByTag(String raw, TelegramTextStyled styled) {
        return styled.getOpenTag() + raw + styled.getCloseTag();
    }

    @SneakyThrows
    public static void replyMessage(TelegramLongPollingBot bot, Message messageToReply, String replyContent, MessageParseMode parseMode, boolean disableWebPagePreview) {
        replyMessage(bot, messageToReply.getChatId() + "", messageToReply.getMessageId(), replyContent, parseMode, disableWebPagePreview);
    }

    @SneakyThrows
    public static void replyMessage(TelegramLongPollingBot bot, String chatId, @Nullable Integer messageId, String replyContent, MessageParseMode parseMode, boolean disableWebPagePreview) {
        SendMessage message = new SendMessage();
        if (parseMode != null && parseMode != MessageParseMode.PLAIN) {
            message.setParseMode(parseMode.getValue());
        }
        message.setText(replyContent);
        message.setChatId(chatId);
        if (messageId != null) {
            message.setReplyToMessageId(messageId);
        }
        if (disableWebPagePreview) {
            message.setDisableWebPagePreview(true);
        }
        bot.execute(message);
    }

    public static void replyMessage(TelegramLongPollingBot bot, Message messageToReply, String replyContent, MessageParseMode parseMode) {
        replyMessage(bot, messageToReply, replyContent, parseMode, false);
    }

    public static boolean isMessageInGroup(Message received) {
        return StringUtils.equalsAny(received.getChat().getType(), "group", "supergroup");
    }

    public static boolean isChannelPost(Update update) {
        return update.getChannelPost() != null;
    }

    public static List<String> lineWrap(String text, int width, boolean shiftNewLines) {
        String[] words = text.trim().split(" ");
        StringBuilder currentLine = new StringBuilder();
        List<String> newLines = new ArrayList<>();

        int currentLength = 0;
        for (int i = 0; i < words.length; i++) {
            currentLine.append(words[i]).append(" ");
            currentLength = currentLine.length();

            int nextWordLength = 0;
            if (i + 1 < words.length)
                nextWordLength = words[i + 1].length();
            if (currentLength + nextWordLength >= width - 2 || i + 1 >= words.length) {
                newLines.add(currentLine.toString());
                currentLine = new StringBuilder();
                if (shiftNewLines)
                    currentLine.append(" ");
            }
        }

        return newLines;
    }

}
