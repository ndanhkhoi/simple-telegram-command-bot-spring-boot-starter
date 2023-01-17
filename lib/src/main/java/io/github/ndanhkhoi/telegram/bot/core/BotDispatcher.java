package io.github.ndanhkhoi.telegram.bot.core;

import io.github.ndanhkhoi.telegram.bot.constant.ChatMemberStatus;
import io.github.ndanhkhoi.telegram.bot.constant.CommonConstant;
import io.github.ndanhkhoi.telegram.bot.core.registry.CommandRegistry;
import io.github.ndanhkhoi.telegram.bot.exception.BotAccessDeniedException;
import io.github.ndanhkhoi.telegram.bot.model.BotCommand;
import io.github.ndanhkhoi.telegram.bot.model.BotCommandParams;
import io.github.ndanhkhoi.telegram.bot.model.MessageParser;
import io.github.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import io.github.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public final class BotDispatcher {
    private final ApplicationContext applicationContext;
    private final BotProperties botProperties;
    private final AbsSender sender;

    private static BotDispatcher instance;

    private BotDispatcher(ApplicationContext applicationContext, BotProperties botProperties, AbsSender sender) {
        this.applicationContext = applicationContext;
        this.botProperties = botProperties;
        this.sender = sender;
    }

    public static BotDispatcher getInstance() {
        return instance;
    }

    public static void createInstance(ApplicationContext applicationContext, BotProperties botProperties, AbsSender sender) {
        synchronized (BotDispatcher.class) {
            if (instance == null) {
                instance = new BotDispatcher(applicationContext, botProperties, sender);
            }
            else {
                throw new java.lang.UnsupportedOperationException("This is a singleton class and cannot be instantiated more than once");
            }
        }
    }

    public AbsSender getSender() {
        return sender;
    }


    public UpdateSubscriber getUpdateSubscriber() {
        return applicationContext.getBean(UpdateSubscriber.class);
    }

    public CommandRegistry getCommandRegistry() {
        return applicationContext.getBean(CommandRegistry.class);
    }

    private boolean hasPermission(Update update, BotCommand botCommand) {
        try {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            Long userSendId = message.getFrom().getId();
            boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(message);
            if (isMessageInGroup && (botCommand.isOnlyForOwner()) || botCommand.isOnlyForPrivate()) {
                return false;
            }
            else if (isMessageInGroup && botCommand.isOnlyAdmin()) {
                GetChatMember getChatMember = new GetChatMember(chatId + "", userSendId);
                ChatMember chatMember = this.executeSneakyThrows(getChatMember);
                ChatMemberStatus status = ChatMemberStatus.fromStatusString(chatMember.getStatus());
                return status == ChatMemberStatus.ADMINISTRATOR || status == ChatMemberStatus.CREATOR;
            }
            else if (isMessageInGroup) {
                boolean isAcceptedGroup = botCommand.isAllowAllGroupAccess() || Arrays.stream(botCommand.getAccessGroupIds()).anyMatch(e -> e == chatId);
                boolean isAcceptedMember = botCommand.getAccessMemberIds().length == 0 || Arrays.stream(botCommand.getAccessMemberIds()).anyMatch(e -> e == userSendId);
                return botCommand.isAllowAllUserAccess() || (isAcceptedGroup && isAcceptedMember);
            }
            else if (botCommand.isOnlyForGroup()) {
                return false;
            }
            else if (botCommand.isOnlyForOwner()) {
                return botProperties.getBotOwnerChatId().contains(String.valueOf(userSendId));
            }
            else if (botCommand.isAllowAllUserAccess()) {
                return true;
            }
            return Arrays.stream(botCommand.getAccessUserIds())
                    .anyMatch(e -> e == userSendId);
        }
        catch (Exception ex) {
            log.error("Error !", ex);
        }
        return false;
    }

    public List<BotCommand> getAvailableBotCommands(Update update) {
        return getCommandRegistry().getAllCommands()
                .stream()
                .filter(botCommand -> this.hasPermission(update, botCommand))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private String truncatedBotUsername(String command) {
        String posfix = "@" + sender.getMe().getUserName();
        if (StringUtils.endsWith(command, posfix)) {
            return command.split(posfix)[0];
        }
        return command;
    }

    public Optional<BotCommand> getCommand(Update update) {
        CommandRegistry commandRegistry = getCommandRegistry();
        BotCommand botCommand = null;
        Message message = update.getMessage();
        MessageParser messageParser = new MessageParser(message.getText());
        String truncatedCmd = truncatedBotUsername(messageParser.getFirstWord());
        if (commandRegistry.hasCommand(truncatedCmd)) {
            if (hasPermission(update, commandRegistry.getCommand(truncatedCmd))) {
                botCommand = commandRegistry.getCommand(truncatedCmd);
            }
            else if (Boolean.TRUE.equals(botProperties.getShowCommandMenu())) {
                throw new BotAccessDeniedException(CommonConstant.ACCESS_DENIED_ERROR);
            }
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
        }
        else if (message.hasPhoto() || message.hasDocument()) {
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

    @SneakyThrows
    public void onRegisterBot() {
        SetMyCommands setMyCommands;
        if (Boolean.TRUE.equals(botProperties.getShowCommandMenu())) {
            List<org.telegram.telegrambots.meta.api.objects.commands.BotCommand> commandList = getCommandRegistry().getAllCommands()
                    .stream()
                    .sorted((e1, e2) -> StringUtils.compare(e1.getCmd(), e2.getCmd()))
                    .map(e -> {
                        StringBuilder description = new StringBuilder();
                        if (StringUtils.isNotBlank(e.getDescription())) {
                            description.append(e.getDescription());
                        }
                        if (StringUtils.isNotBlank(e.getBodyDescription())) {
                            description.append(" [").append(e.getBodyDescription()).append("]");
                        }
                        return new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(e.getCmd(), description.toString());
                    })
                    .collect(Collectors.toList());
            setMyCommands = new SetMyCommands(commandList, new BotCommandScopeDefault(), null);
        }
        else {
            setMyCommands = new SetMyCommands(Collections.singletonList(CommonConstant.HELP_BOT_COMMAND), new BotCommandScopeDefault(), null);
        }
        sender.execute(setMyCommands);
        log.info("Bot {} has started successfully", botProperties.getUsername());
    }

    @SneakyThrows
    public <T extends Serializable, M extends BotApiMethod<T>> T executeSneakyThrows(M method) {
        return sender.execute(method);
    }

}
