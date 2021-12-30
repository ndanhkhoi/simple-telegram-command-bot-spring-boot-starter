package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.ChatMemberStatus;
import com.ndanhkhoi.telegram.bot.utils.TelegramMessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

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
        this.commandRegistry = new CommandRegistry();
        this.updateSubscriber = new UpdateSubscriber(botProperties, this, applicationContext);
        this.loadBotRoutes();
    }

    private void loadBotRoutes() {
        StringBuilder sb = new StringBuilder("com.ndanhkhoi.telegram.bot.resource");
        if (StringUtils.isNotBlank(botProperties.getBotRoutePackages())) {
            sb.append(",").append(botProperties.getBotRoutePackages());
        }
        String[] packagesToScan = sb.toString().split(",");

        log.info("Bot route's ackages: {}", Arrays.asList(packagesToScan));

        Flux.fromArray(packagesToScan)
                .map(packageToScan -> new Reflections(new ConfigurationBuilder().setUrls(ClasspathHelper.forPackage(packageToScan))))
                .flatMap(reflections -> Flux.fromIterable(reflections.get(Scanners.TypesAnnotated.with(BotRoute.class).asClass())))
                .flatMap(clazz -> Flux.fromArray(clazz.getDeclaredMethods()))
                .filter(method -> Modifier.isPublic(method.getModifiers()) && method.getDeclaredAnnotationsByType(CommandMapping.class).length > 0)
                .flatMap(this::extractBotCommands)
                .doAfterTerminate(() -> log.info("{} bot command(s) has bean loaded: {}", commandRegistry.getSize(), commandRegistry.getCommandNames()))
                .subscribeOn(Schedulers.parallel())
                .subscribe(commandRegistry::register);
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

    private String truncatedBotUsername(String command) throws TelegramApiException {
        String posfix = "@" + this.getMe().getUserName();
        if (command.endsWith(posfix)) {
            return command.split(posfix)[0];
        }
        return command;
    }

    public Flux<BotCommand> getAvailableBotCommands(Update update) {
        return Flux.fromIterable(commandRegistry.getAllCommands())
                .filter(botCommand -> this.hasPermission(update, botCommand));
    }

    public Mono<BotCommand> getBotCommandByAgrs(BotCommandAgrs botCommandAgrs) throws TelegramApiException {
        BotCommand botCommand = null;
        String truncatedCmd = truncatedBotUsername(botCommandAgrs.getCommand());
        if (commandRegistry.hasCommand(truncatedCmd) && hasPermission(botCommandAgrs.getUpdate(), commandRegistry.getCommand(truncatedCmd))) {
            botCommand = commandRegistry.getCommand(truncatedCmd);
        }
        else {
            log.warn("No resource match for command: {}", botCommandAgrs.getCommand());
        }
        return Mono.justOrEmpty(botCommand);
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
