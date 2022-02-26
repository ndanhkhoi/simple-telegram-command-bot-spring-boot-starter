package com.ndanhkhoi.telegram.bot.utils;

import com.ndanhkhoi.telegram.bot.constant.TelegramTextStyled;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@UtilityClass
public final class TelegramMessageUtils {

    public static String getCommandAgrs(String text, String cmd) {
        try {
            return text.split(cmd + " ")[1];
        }
        catch (Exception ex) {
            log.error("Error!", ex);
            return "";
        }
    }

    public static String wrapByTag(String raw, TelegramTextStyled styled) {
        return styled.getOpenTag() + raw + styled.getCloseTag();
    }

    @SneakyThrows
    public static void replyMessage(TelegramLongPollingBot bot, Message messageToReply, String replyContent, boolean useHtml, boolean disableWebPagePreview) {
        SendMessage message = new SendMessage();
        if (useHtml) {
            message.setParseMode(ParseMode.HTML);
        }
        message.setText(replyContent);
        message.setChatId(String.valueOf(messageToReply.getChatId()));
        message.setReplyToMessageId(messageToReply.getMessageId());
        if (disableWebPagePreview) {
            message.setDisableWebPagePreview(true);
        }
        bot.execute(message);
    }

    public static void replyMessage(TelegramLongPollingBot bot, Message messageToReply, String replyContent, boolean useHtml) {
        replyMessage(bot, messageToReply, replyContent, useHtml, false);
    }

    public static boolean isMessageInGroup(Message received) {
        return StringUtils.equalsAny(received.getChat().getType(), "group", "supergroup");
    }

    public static boolean isChannelPost(Update update) {
        return update.getChannelPost() != null;
    }

}
