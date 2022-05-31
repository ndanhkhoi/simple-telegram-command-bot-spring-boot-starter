package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.ChatMemberStatus;
import com.ndanhkhoi.telegram.bot.constant.CommonConstant;
import com.ndanhkhoi.telegram.bot.exception.BotException;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.model.MessageParser;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.subscriber.CommandNotFoundUpdateSubscriber;
import com.ndanhkhoi.telegram.bot.subscriber.DefaultCommandNotFoundUpdateSubscriber;
import com.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import com.ndanhkhoi.telegram.bot.utils.SpringHelper;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ndanhkhoi
 * Created at 11:33:32 October 08, 2021
 */
@Slf4j
@Singleton
public class SimpleTelegramLongPollingCommandBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final UpdateSubscriber updateSubscriber;
    private final SpringHelper springHelper;
    private final CommandRegistry commandRegistry = new CommandRegistry();
    private final DefaultCommandNotFoundUpdateSubscriber defaultNonCommandUpdateSubscriber = new DefaultCommandNotFoundUpdateSubscriber();

    public SimpleTelegramLongPollingCommandBot(BotProperties botProperties, SpringHelper springHelper, UpdateTraceRepository updateTraceRepository, UpdateObjectMapper updateObjectMapper) {
        this.botProperties = botProperties;
        this.springHelper = springHelper;
        this.updateSubscriber = new UpdateSubscriber(botProperties, this, springHelper, updateTraceRepository, updateObjectMapper);
        this.loadBotRoutes();
    }

    private void loadBotRoutes() {
        List<String> packagesToScan = new ArrayList<>();
        packagesToScan.add("com.ndanhkhoi.telegram.bot.route");
        packagesToScan.addAll(botProperties.getBotRoutePackages());

        log.info("Bot route's packages: {}", packagesToScan);

        Flux.fromIterable(packagesToScan)
                .map(packageToScan -> new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packageToScan))))
                .flatMap(reflections -> Flux.fromIterable(reflections.get(Scanners.TypesAnnotated.with(BotRoute.class).asClass())))
                .filter(clazz -> {
                    ConditionalOnProperty[] annotations = clazz.getDeclaredAnnotationsByType(ConditionalOnProperty.class);
                    if (annotations.length == 0) {
                        return true;
                    }
                    String[] properties = annotations[0].value();
                    String havingValue = annotations[0].havingValue();
                    return Arrays.stream(properties)
                            .map(property -> springHelper.getProperty(property, String.class))
                            .allMatch(propertyValue -> StringUtils.equals(propertyValue, havingValue));
                })
                .flatMap(clazz -> Flux.fromArray(clazz.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()) && method.getDeclaredAnnotationsByType(CommandMapping.class).length > 0)
                .flatMap(this::extractBotCommands)
                .doOnError(ex -> {
                    throw Exceptions.errorCallbackNotImplemented(ex);
                })
                .doAfterTerminate(() -> log.info("{} bot command(s) has bean loaded: {}", commandRegistry.getSize(), commandRegistry.getCommandNames()))
                .subscribe(commandRegistry::register);
    }

    @SneakyThrows
    public <T extends Serializable, M extends BotApiMethod<T>> T executeSneakyThrows(M method) {
        return super.execute(method);
    }

    private Flux<BotCommand> extractBotCommands(Method method) {
        String commandDescription = Arrays.stream(method.getDeclaredAnnotationsByType(CommandDescription.class))
                .findFirst()
                .map(CommandDescription::value)
                .orElse("");
        String bodyDescription = Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.getDeclaredAnnotationsByType(CommandBody.class).length > 0)
                .findFirst()
                .map(parameter -> {
                    String description = parameter.getDeclaredAnnotationsByType(CommandBody.class)[0].description();
                    return StringUtils.defaultIfBlank(description, parameter.getName());
                })
                .orElse("");
        CommandMapping mapping = method.getDeclaredAnnotationsByType(CommandMapping.class)[0];
        return Flux.fromArray(mapping.value())
                .map(cmd -> extractBotCommand(method, cmd, mapping, commandDescription, bodyDescription));
    }

    private BotCommand extractBotCommand(Method method, String cmd, CommandMapping mapping, String commandDescription, String bodyDescription) {
        this.validateCommand(cmd);
        return BotCommand.builder()
                .withCmd(cmd)
                .withUseHtml(mapping.useHtml())
                .withDisableWebPagePreview(mapping.disableWebPagePreview())
                .withAccessUserIds(mapping.accessUserIds())
                .withAccessMemberIds(mapping.accessMemberIds())
                .withAccessGroupIds(mapping.accessGroupIds())
                .withAllowAllUserAccess(mapping.allowAllUserAccess())
                .withOnlyAdmin(mapping.onlyAdmin())
                .withSendFile(mapping.sendFile())
                .withMethod(method)
                .withDescription(commandDescription)
                .withBodyDescription(bodyDescription)
                .withOnlyForOwner(mapping.onlyForOwner())
                .build();
    }

    private void validateCommand(String cmd) {
        if (StringUtils.isNotBlank(cmd)) {
            if (StringUtils.startsWith(cmd, CommonConstant.CMD_PREFIX)) {
                if (cmd.length() > CommonConstant.CMD_MAX_LENGTH) {
                    throw new BotException(String.format(CommonConstant.CMD_MAX_LENGTH_ERROR, CommonConstant.CMD_MAX_LENGTH, CommonConstant.CMD_PREFIX));
                }
                String cmdValue = cmd.substring(1);
                if (!CommonConstant.CMD_PATTERN.matcher(cmdValue).matches()) {
                    throw new BotException(CommonConstant.CMD_PATTERN_ERROR);
                }
            }
            else {
                throw new BotException(String.format(CommonConstant.CMD_PREFIX_ERROR, CommonConstant.CMD_PREFIX));
            }
        }
        else {
            throw new BotException(CommonConstant.CMD_BLANK_ERROR);
        }
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
        } else {
            if (springHelper.existBean(CommandNotFoundUpdateSubscriber.class)) {
                CommandNotFoundUpdateSubscriber nonCommandUpdateSubscriber = springHelper.getBean(CommandNotFoundUpdateSubscriber.class);
                nonCommandUpdateSubscriber.accept(update, messageParser.getFirstWord());
            } else {
                defaultNonCommandUpdateSubscriber.accept(update, messageParser.getFirstWord());
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
        Mono.just(update)
                .subscribeOn(Schedulers.parallel())
                .subscribe(updateSubscriber);
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        Flux.fromIterable(updates)
                .subscribeOn(Schedulers.parallel())
                .subscribe(updateSubscriber);
    }

}
