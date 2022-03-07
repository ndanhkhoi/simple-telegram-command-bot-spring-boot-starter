package com.ndanhkhoi.telegram.bot.core;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.ndanhkhoi.telegram.bot.model.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.*;

import java.util.*;

/**
 * @author ndanhkhoi
 * Created at 20:31:09 December 28, 2021
 */
public class CommandRegistry {

    private final Map<String, BotCommand> botCommandMap;
    private final BotProperties botProperties;

    private final Multimap<BotCommandScope, org.telegram.telegrambots.meta.api.objects.commands.BotCommand> commandMapByScope;

    public CommandRegistry(BotProperties botProperties) {
        this.botProperties = botProperties;
        this.botCommandMap = new LinkedHashMap<>();
        this.commandMapByScope = ArrayListMultimap.create();
    }

    public int getSize() {
        return this.botCommandMap.keySet().size();
    }

    public Set<String> getCommandNames() {
        return this.botCommandMap.keySet();
    }

    public void register(BotCommand botCommand) {
        this.botCommandMap.put(botCommand.getCmd(), botCommand);
        List<BotCommandScope> scopes = getScopes(botCommand);
        scopes.forEach(scope -> commandMapByScope.put(scope, new org.telegram.telegrambots.meta.api.objects.commands.BotCommand(botCommand.getCmd(), botCommand.getDescription())));
    }

    public Collection<BotCommand> getAllCommands() {
        return botCommandMap.values();
    }

    public boolean hasCommand(String name) {
        return this.botCommandMap.containsKey(name);
    }

    public BotCommand getCommand(String name) {
        return this.botCommandMap.get(name);
    }

    public Multimap<BotCommandScope, org.telegram.telegrambots.meta.api.objects.commands.BotCommand> getCommandMapByScope() {
        return this.commandMapByScope;
    }

    private List<BotCommandScope> getScopes(BotCommand botCommand) {
        List<BotCommandScope> scopes = new ArrayList<>();
        if (botCommand.getOnlyForOwner()) {
            botProperties.getBotOwnerChatId().forEach(chatId -> scopes.add(new BotCommandScopeChat(chatId)));
        }
        else if (botCommand.getAllowAllUserAccess()) {
            scopes.add(new BotCommandScopeAllPrivateChats());
            if (botCommand.getOnlyAdmin()) {
                scopes.add(new BotCommandScopeAllChatAdministrators());
            }
            else {
                scopes.add(new BotCommandScopeAllGroupChats());
            }
        }
        else if (botCommand.getAccessGroupIds().length > 0) {
            if (botCommand.getOnlyAdmin()) {
                Arrays.stream(botCommand.getAccessGroupIds())
                        .forEach(chatId -> scopes.add(new BotCommandScopeChatAdministrators(chatId + "")));
            }
            else {
                Arrays.stream(botCommand.getAccessGroupIds())
                        .forEach(chatId -> scopes.add(new BotCommandScopeChat(chatId + "")));
            }
        }
        else if (botCommand.getAccessUserIds().length > 0) {
            Arrays.stream(botCommand.getAccessUserIds())
                    .forEach(chatId -> scopes.add(new BotCommandScopeChat(chatId + "")));
        }

        return scopes;
    }

}
