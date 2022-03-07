package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.ChatMemberStatus;
import com.ndanhkhoi.telegram.bot.exception.BotException;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import com.ndanhkhoi.telegram.bot.model.BotCommandParams;
import com.ndanhkhoi.telegram.bot.model.MessageParser;
import com.ndanhkhoi.telegram.bot.subscriber.UpdateSubscriber;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.inject.Singleton;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * @author ndanhkhoi
 * Created at 11:33:32 October 08, 2021
 */
@Slf4j
@Singleton
public class SimpleTelegramLongPollingCommandBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final UpdateSubscriber updateSubscriber;
    private final CommandRegistry commandRegistry;

    public SimpleTelegramLongPollingCommandBot(BotProperties botProperties, ApplicationContext applicationContext) {
        this.botProperties = botProperties;
        this.commandRegistry = new CommandRegistry(botProperties);
        this.updateSubscriber = new UpdateSubscriber(botProperties, this, applicationContext);
        this.loadBotRoutes();
    }

    private void loadBotRoutes() {
        List<String> packagesToScan = new ArrayList<>();
        packagesToScan.add("com.ndanhkhoi.telegram.bot.route");
        packagesToScan.addAll(botProperties.getBotRoutePackages());

        log.info("Bot route's ackages: {}", packagesToScan);

        Flux.fromIterable(packagesToScan)
                .map(packageToScan -> new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packageToScan))))
                .flatMap(reflections -> Flux.fromIterable(reflections.get(Scanners.TypesAnnotated.with(BotRoute.class).asClass())))
                .flatMap(clazz -> Flux.fromArray(clazz.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()) && method.getDeclaredAnnotationsByType(CommandMapping.class).length > 0)
                .flatMap(this::extractBotCommands)
                .doAfterTerminate(() -> {
                    commandRegistry.getCommandMapByScope()
                            .asMap()
                            .forEach((scope, commands) -> {
                                SetMyCommands setMyCommands = new SetMyCommands(new ArrayList<>(commands), scope, null);
                                this.executeSneakyThrows(setMyCommands);
                            });
                    log.info("{} bot command(s) has bean loaded: {}", commandRegistry.getSize(), commandRegistry.getCommandNames());
                })
                .subscribeOn(Schedulers.parallel())
                .subscribe(commandRegistry::register);
    }

    @SneakyThrows
    public <T extends Serializable, MethodType extends BotApiMethod<T>> T executeSneakyThrows(MethodType method) {
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
        if (StringUtils.isNotBlank(cmd)) {
            if (StringUtils.startsWith(cmd, "/")) {
                if (cmd.length() > 32) {
                    throw new BotException("Command cannot be longer than 32 (including /)");
                }
                String cmdValue = cmd.substring(1);
                Pattern pattern = Pattern.compile("^[a-zA-Z0-9_]*$");
                if (!pattern.matcher(cmdValue).matches()) {
                    throw new BotException("Command must contains only upper and lowercase letters, numbers, and underscores (_).");
                }
            }
            else {
                throw new BotException("Command must be start with /");
            }
        }
        else {
            throw new BotException("Command cannot be null or empty");
        }
        return BotCommand.builder()
                .withCmd(cmd)
                .withUseHtml(mapping.useHtml())
                .withDisableWebPagePreview(mapping.disableWebPagePreview())
                .withAccessUserIds(mapping.accessUserIds())
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

    private boolean hasPermission(Update update, BotCommand botCommand) {
        try {
            Message message = update.getMessage();
            Long chatId = message.getChatId();
            Long userSendId = message.getFrom().getId();
            boolean isMessageInGroup = TelegramMessageUtils.isMessageInGroup(message);
            if (isMessageInGroup) {
                if (botCommand.getOnlyForOwner()) {
                    return false;
                }
                boolean hasPermission = botCommand.getAllowAllUserAccess() ||
                        Arrays.stream(botCommand.getAccessGroupIds())
                                .anyMatch(e -> e == chatId);
                if (hasPermission) {
                    if (!botCommand.getOnlyAdmin()) {
                        return true;
                    }
                    else {
                        GetChatMember getChatMember = new GetChatMember(chatId + "", userSendId);
                        ChatMember chatMember = this.execute(getChatMember);
                        ChatMemberStatus status = ChatMemberStatus.fromStatusString(chatMember.getStatus());
                        return status == ChatMemberStatus.ADMINISTRATOR || status == ChatMemberStatus.CREATOR;
                    }
                }
            }
            else {
                if (botCommand.getOnlyForOwner()) {
                    return botProperties.getBotOwnerChatId().contains(String.valueOf(chatId));
                }
                if (botCommand.getAllowAllUserAccess()) {
                    return true;
                }
                return Arrays.stream(botCommand.getAccessUserIds())
                        .anyMatch(e -> e == chatId);
            }
        }
        catch (Exception ex) {
            log.error("Error !" ,ex);
        }
        return false;
    }

    public Flux<BotCommand> getAvailableBotCommands(Update update) {
        return Flux.fromIterable(commandRegistry.getAllCommands())
                .filter(botCommand -> this.hasPermission(update, botCommand));
    }

    @SneakyThrows
    private String truncatedBotUsername(String command) {
        String posfix = "@" + this.getMe().getUserName();
        if (StringUtils.endsWith(command, posfix)) {
            return command.split(posfix)[0];
        }
        return command;
    }

    public Mono<BotCommand> getCommand(Update update) {
        BotCommand botCommand = null;
        Message message = update.getMessage();
        MessageParser messageParser = new MessageParser(message.getText());
        String truncatedCmd = truncatedBotUsername(messageParser.getFirstWord());
        if (commandRegistry.hasCommand(truncatedCmd) && hasPermission(update, commandRegistry.getCommand(truncatedCmd))) {
            botCommand = commandRegistry.getCommand(truncatedCmd);
        }
        else {
            log.warn("No route match for command: {}", messageParser.getFirstWord());
        }
        return Mono.justOrEmpty(botCommand);
    }

    public BotCommandParams getCommandParams(Update update) {
        Message message = update.getMessage();
        if (message.hasText()) {
            Long chatId = message.getChat().getId();
            MessageParser messageParser = new MessageParser(message.getText());
            return BotCommandParams.builder()
                    .withUpdate(update)
                    .withCmdBody(messageParser.getRemainingText())
                    .withSendUserId(update.getMessage().getFrom().getId())
                    .withSendUsername(Objects.toString(update.getMessage().getFrom().getUserName(), ""))
                    .withChatId(chatId)
                    .build();
        }
        else if (message.hasPhoto()) {
            return getCommandParamsWithPhoto(update);
        }
        return null;
    }

    private BotCommandParams getCommandParamsWithPhoto(Update update) {
        Message message = update.getMessage();
        MessageParser messageParser = new MessageParser(message.getCaption());
        Long chatId = message.getChat().getId();
        return BotCommandParams.builder()
                .withUpdate(update)
                .withCmdBody(messageParser.getRemainingText())
                .withSendUserId(update.getMessage().getFrom().getId())
                .withSendUsername(Objects.toString(update.getMessage().getFrom().getUserName(), ""))
                .withChatId(chatId)
                .withPhotoSizes(message.getPhoto())
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
