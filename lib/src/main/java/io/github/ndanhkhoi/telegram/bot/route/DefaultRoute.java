package io.github.ndanhkhoi.telegram.bot.route;

import io.github.ndanhkhoi.telegram.bot.annotation.BotRoute;
import io.github.ndanhkhoi.telegram.bot.annotation.ChatId;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import io.github.ndanhkhoi.telegram.bot.constant.CommonConstant;
import io.github.ndanhkhoi.telegram.bot.constant.MediaType;
import io.github.ndanhkhoi.telegram.bot.constant.MessageParseMode;
import io.github.ndanhkhoi.telegram.bot.constant.TelegramTextStyled;
import io.github.ndanhkhoi.telegram.bot.core.BotDispatcher;
import io.github.ndanhkhoi.telegram.bot.core.BotProperties;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.utils.FileUtils;
import io.github.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 20:59:35 October 05, 2021
 */
@ConditionalOnProperty(value = "khoinda.bot.disable-default-commands", havingValue = "false", matchIfMissing = true)
@BotRoute
@Slf4j
@RequiredArgsConstructor
public class DefaultRoute {

    private final BotDispatcher botDispatcher;
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
        if (isMessageInGroup && botCommand.isOnlyAdmin()) {
            result.append(" (").append("only admin group has permission").append(")");
        }
        return result.toString();
    }

    @CommandDescription(CommonConstant.HELP_CMD_DESCRIPTION)
    @CommandMapping(value = CommonConstant.HELP_CMD, allowAllUserAccess = true, parseMode = MessageParseMode.HTML)
    public Mono<String> getCmdByChat(Update update, @ChatId Long chatId) {
        boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(update.getMessage());
        String title = TelegramMessageUtils.wrapByTag("List of available commands for this chat: ", TelegramTextStyled.BOLD);
        AtomicInteger index = new AtomicInteger(1);
        List<String> result =  botDispatcher.getAvailableBotCommands(update)
                .stream()
                .map(botCommand -> described(botCommand, isMessageInGroup))
                .sorted()
                .map(described -> index.getAndIncrement() + ". " + described)
                .collect(Collectors.toList());
        return Flux.merge(Flux.just(title), Flux.fromIterable(result))
            .collect(Collectors.joining(System.lineSeparator()));
    }

    @CommandDescription(CommonConstant.START_CMD_DESCRIPTION)
    @CommandMapping(value = CommonConstant.START_CMD, allowAllUserAccess = true)
    public String start(Update update) {
        return String.format("Hi, %s. Please use /help to know all I can do", update.getMessage().getFrom().getFirstName());
    }

    @CommandDescription(CommonConstant.GET_LOG_FILE_DESCRIPTION)
    @CommandMapping(value = CommonConstant.GET_LOG_FILE_CMD, sendFile = MediaType.DOCUMENT, onlyForOwner = true)
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
