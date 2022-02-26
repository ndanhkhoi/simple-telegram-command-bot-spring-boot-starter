package com.ndanhkhoi.telegram.bot.resource;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.ChatId;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.MediaType;
import com.ndanhkhoi.telegram.bot.constant.TelegramTextStyled;
import com.ndanhkhoi.telegram.bot.core.BotCommand;
import com.ndanhkhoi.telegram.bot.core.BotProperties;
import com.ndanhkhoi.telegram.bot.core.SimpleTelegramLongPollingCommandBot;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 20:59:35 October 05, 2021
 */
@BotRoute
@Slf4j
@RequiredArgsConstructor
public class DefaultResource {

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

    @CommandDescription("List of available command(s) for this chat")
    @CommandMapping(value = {"/cmd", "/help"}, allowAllUserAccess = true, useHtml = true)
    public Mono<String> getCmdByChat(Update update, @ChatId Long chatId) {
        boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(update.getMessage());
        String title = TelegramMessageUtils.wrapByTag("List of available commands for this chat: ", TelegramTextStyled.BOLD);
        AtomicInteger index = new AtomicInteger(1);
        List<String>  botOwnerChatId = StringUtils.isNotBlank(botProperties.getBotOwnerChatId()) ? Arrays.asList(botProperties.getBotOwnerChatId().split(",")) :  new ArrayList<>();
        Flux<String> result =  simpleTelegramLongPollingCommandBot.getAvailableBotCommands(update)
                .filter(botCommand -> !botCommand.getOnlyForOwner() || botOwnerChatId.contains(String.valueOf(chatId)))
                .map(botCommand -> described(botCommand, isMessageInGroup))
                .sort()
                .map(described -> index.getAndIncrement() + ". " + described);
        return Flux.merge(Flux.just(title), result)
            .collect(Collectors.joining(System.lineSeparator()));
    }

    @CommandDescription("Start chat")
    @CommandMapping(value = "/start", allowAllUserAccess = true)
    public String start(Update update) {
        return String.format("Hi, %s. Please use /cmd or /help to know all I can do", update.getMessage().getFrom().getFirstName());
    }

    @CommandDescription("Get an application log file")
    @CommandMapping(value = "/getLogFile", sendFile = MediaType.DOCUMENT, allowAllUserAccess = true, onlyForOwner = true)
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
