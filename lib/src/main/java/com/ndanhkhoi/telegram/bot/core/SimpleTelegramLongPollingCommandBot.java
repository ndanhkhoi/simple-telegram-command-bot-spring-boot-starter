package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.constant.ChatMemberStatus;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.core.registry.CommandRegistry;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.model.MessageParser;
import com.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 11:33:32 October 08, 2021
 */
@Slf4j
@RequiredArgsConstructor
public class SimpleTelegramLongPollingCommandBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;
    private final UpdateSubscriber updateSubscriber;
    private final CommandRegistry commandRegistry;

    @SneakyThrows
    public <T extends Serializable, M extends BotApiMethod<T>> T executeSneakyThrows(M method) {
        return super.execute(method);
    }

    private boolean hasPermission(Update update, BotCommand botCommand) {
        try {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            Long userSendId = message.getFrom().getId();
            boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(message);
            if (isMessageInGroup && botCommand.isOnlyForOwner()) {
                return false;
            }
            else if (isMessageInGroup && botCommand.isOnlyAdmin()) {
                GetChatMember getChatMember = new GetChatMember(chatId + "", userSendId);
                ChatMember chatMember = this.executeSneakyThrows(getChatMember);
                ChatMemberStatus status = ChatMemberStatus.fromStatusString(chatMember.getStatus());
                return status == ChatMemberStatus.ADMINISTRATOR || status == ChatMemberStatus.CREATOR;
            }
            else if (isMessageInGroup) {
                boolean isAcceptedGroup = Arrays.stream(botCommand.getAccessGroupIds()).anyMatch(e -> e == chatId);
                boolean isAcceptedMember = botCommand.getAccessMemberIds().length == 0 || Arrays.stream(botCommand.getAccessMemberIds()).anyMatch(e -> e == userSendId);
                return botCommand.isAllowAllUserAccess() || (isAcceptedGroup && isAcceptedMember);
            }
            else if (botCommand.isOnlyForOwner()) {
                return botProperties.getBotOwnerChatId().contains(String.valueOf(userSendId));
            }
            else if (botCommand.isAllowAllUserAccess()) {
                return true;
            }
            return Arrays.stream(botCommand.getAccessUserIds())
                    .anyMatch(e -> e == userSendId);
        } catch (Exception ex) {
            log.error("Error !", ex);
        }
        return false;
    }

    public List<BotCommand> getAvailableBotCommands(Update update) {
        return commandRegistry.getAllCommands()
                .stream()
                .filter(botCommand -> this.hasPermission(update, botCommand))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String truncatedBotUsername(String command) {
        String posfix = "@" + this.getMe().getUserName();
        if (StringUtils.endsWith(command, posfix)) {
            return command.split(posfix)[0];
        }
        return command;
    }

    public Optional<BotCommand> getCommand(Update update) {
        BotCommand botCommand = null;
        Message message = update.getMessage();
        MessageParser messageParser = new MessageParser(message.getText());
        String truncatedCmd = truncatedBotUsername(messageParser.getFirstWord());
        if (commandRegistry.hasCommand(truncatedCmd) && hasPermission(update, commandRegistry.getCommand(truncatedCmd))) {
            botCommand = commandRegistry.getCommand(truncatedCmd);
        }
        return Optional.ofNullable(botCommand);
    }

    public BotCommandParams getCommandParams(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            Long chatId = message.getChat().getId();
            MessageParser messageParser = new MessageParser(message.getText());
            return BotCommandParams.builder()
                    .withUpdate(update)
                    .withMessage(update.getMessage())
                    .withCmdBody(messageParser.getRemainingText())
                    .withSendUserId(update.getMessage().getFrom().getId())
                    .withSendUsername(Objects.toString(update.getMessage().getFrom().getUserName(), ""))
                    .withChatId(chatId)
                    .build();
        } else if (message.hasPhoto() || message.hasDocument()) {
            return getCommandParamsWithMedia(update);
        }
        return null;
    }

    private BotCommandParams getCommandParamsWithMedia(Update update) {
        Message message = update.getMessage();
        MessageParser messageParser = new MessageParser(message.getCaption());
        Long chatId = message.getChat().getId();
        return BotCommandParams.builder()
                .withUpdate(update)
                .withMessage(update.getMessage())
                .withCmdBody(messageParser.getRemainingText())
                .withSendUserId(update.getMessage().getFrom().getId())
                .withSendUsername(Objects.toString(update.getMessage().getFrom().getUserName(), ""))
                .withChatId(chatId)
                .withPhotoSizes(message.getPhoto())
                .withDocument(message.getDocument())
                .build();
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onRegister() {
        super.onRegister();
        SetMyCommands setMyCommands = new SetMyCommands(Collections.singletonList(CommonConstant.HELP_BOT_COMMAND), new BotCommandScopeDefault(), null);
        executeSneakyThrows(setMyCommands);
        log.info("Bot {} has started successfully", botProperties.getUsername());
    }

    @Override
    public void onUpdateReceived(Update update) {
        updateSubscriber.consume(Mono.just(update));
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updateSubscriber.consume(Flux.fromIterable(updates));
    }

}
