package com.ndanhkhoi.telegram.bot.resolver;

import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import reactor.function.Consumer4;

import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 22:21:06 February 26, 2022
 */
public final class StringResolver extends TypeResolver<String> {

    public static final StringResolver INSTANCE = new StringResolver (String.class,
            (value, botCommand, botCommandParams, telegramLongPollingBot) -> {
                String replyContent = (String) value;
                if (StringUtils.isBlank(replyContent)) {
                    LOGGER.warn("Blank string returnd");
                    return;
                }
                Message message = botCommandParams.getUpdate().getMessage();
                if (replyContent.length() > CommonConstant.MAX_MESSAGE_CONTENT_LENGTH) {
                    List<String> lineWrap = TelegramMessageUtils.lineWrap(replyContent, CommonConstant.MAX_MESSAGE_CONTENT_LENGTH, false);
                    String chatId = message.getChatId() + "";
                    for (int i = 0; i < lineWrap.size(); i++) {
                        Integer messageId = i == 0 ? message.getMessageId() : null;
                        TelegramMessageUtils.replyMessage(telegramLongPollingBot, chatId, messageId, lineWrap.get(i), botCommand.isUseHtml(), botCommand.isDisableWebPagePreview());
                    }
                }
                else {
                    TelegramMessageUtils.replyMessage(telegramLongPollingBot, message, replyContent, botCommand.isUseHtml(), botCommand.isDisableWebPagePreview());
                }
                LOGGER.info("Reply Message: {}", replyContent);
            }
    );

    private StringResolver(Class<String> type, Consumer4<Object, BotCommand, BotCommandParams, TelegramLongPollingBot> resolver) {
        super(type, resolver);
    }

}
