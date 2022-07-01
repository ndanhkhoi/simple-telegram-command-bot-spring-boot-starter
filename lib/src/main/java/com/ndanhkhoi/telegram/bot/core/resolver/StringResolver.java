package com.ndanhkhoi.telegram.bot.core.resolver;

import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
@Slf4j
public class StringResolver implements TypeResolver<String> {

    private final SimpleTelegramLongPollingCommandBot telegramLongPollingBot;


    public StringResolver(SimpleTelegramLongPollingCommandBot telegramLongPollingBot) {
        this.telegramLongPollingBot = telegramLongPollingBot;
    }

    @Override
    public void resolve(String value, BotCommand botCommand, BotCommandParams params) {
        if (StringUtils.isBlank(value)) {
            log.warn("Blank string returnd");
            return;
        }
        Message message = params.getUpdate().getMessage();
        if (value.length() > CommonConstant.MAX_MESSAGE_CONTENT_LENGTH) {
            List<String> lineWrap = TelegramMessageUtils.lineWrap(value, CommonConstant.MAX_MESSAGE_CONTENT_LENGTH, false);
            String chatId = message.getChatId() + "";
            for (int i = 0; i < lineWrap.size(); i++) {
                Integer messageId = i == 0 ? message.getMessageId() : null;
                TelegramMessageUtils.replyMessage(telegramLongPollingBot, chatId, messageId, lineWrap.get(i), botCommand.isUseHtml(), botCommand.isDisableWebPagePreview());
            }
        }
        else {
            TelegramMessageUtils.replyMessage(telegramLongPollingBot, message, value, botCommand.isUseHtml(), botCommand.isDisableWebPagePreview());
        }
        log.debug("Reply Message: {}", value);
    }

    @Override
    public Class<String> getType() {
        return String.class;
    }

}
