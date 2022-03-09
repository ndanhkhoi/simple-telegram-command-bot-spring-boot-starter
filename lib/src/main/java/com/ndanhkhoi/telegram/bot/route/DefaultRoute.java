package com.ndanhkhoi.telegram.bot.route;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.ChatId;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.constant.TelegramTextStyled;
import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.utils.FileUtils;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 20:59:35 October 05, 2021
 */
@BotRoute
@Slf4j
@RequiredArgsConstructor
public class DefaultRoute {

    private final SimpleTelegramLongPollingCommandBot simpleTelegramLongPollingCommandBot;
    private final BotProperties botProperties;

    @Value("${logging.file.name:#{null}}")
    private String logFile;

    private String described(BotCommand botCommand, boolean isMessageInGroup) {
        StringBuilder result = new StringBuilder();
        StringBuilder cmd = new StringBuilder(botCommand.getCmd());
        if (StringUtils.isNotBlank(botCommand.getBodyDescription())) {
            cmd.append(" [").append(botCommand.getBodyDescription()).append("]");
        }
        result.append(TelegramMessageUtils.wrapByTag(cmd.toString(), TelegramTextStyled.CODE));
        if (StringUtils.isNotBlank(botCommand.getDescription())) {
            result.append(": ").append(botCommand.getDescription());
        }
        if (isMessageInGroup) {
            if (botCommand.getOnlyAdmin()) {
                result.append(" (").append("only admin group has permission").append(")");
            }
        }
        return result.toString();
    }

    @CommandDescription(CommonConstant.HELP_CMD_DESCRIPTION)
    @CommandMapping(value = CommonConstant.HELP_CMD, allowAllUserAccess = true, useHtml = true)
    public Mono<String> getCmdByChat(Update update, @ChatId Long chatId) {
        boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(update.getMessage());
        String title = TelegramMessageUtils.wrapByTag("List of available commands for this chat: ", TelegramTextStyled.BOLD);
        AtomicInteger index = new AtomicInteger(1);
        Flux<String> result =  simpleTelegramLongPollingCommandBot.getAvailableBotCommands(update)
                .map(botCommand -> described(botCommand, isMessageInGroup))
                .sort()
                .map(described -> index.getAndIncrement() + ". " + described);
        return Flux.merge(Flux.just(title), result)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    @CommandDescription(CommonConstant.START_CMD_DESCRIPTION)
    @CommandMapping(value = CommonConstant.START_CMD, allowAllUserAccess = true)
    public String start(Update update) {
        return String.format("Hi, %s. Please use /cmd or /help to know all I can do", update.getMessage().getFrom().getFirstName());
    }

    @CommandDescription(CommonConstant.GET_LOG_FILE_CMD)
    @CommandMapping(value = CommonConstant.GET_LOG_FILE_DESCRIPTION, sendFile = MediaType.DOCUMENT, onlyForOwner = true)
    public Object getLog(Update update, @ChatId Long chatId) {
        if (botProperties.getBotOwnerChatId().contains(String.valueOf(chatId))) {
            if (StringUtils.isNotBlank(logFile)) {
                return FileUtils.getInputFile(new File(logFile));
            }
            else {
                return "Please config a property logging.file.name to get log file with this cmd !";
            }
        }
        return null;
    }

}
