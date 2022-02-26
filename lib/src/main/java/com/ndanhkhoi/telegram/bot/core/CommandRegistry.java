package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.model.BotCommand;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author ndanhkhoi
 * Created at 20:31:09 December 28, 2021
 */
public class CommandRegistry {

    private final Map<String, BotCommand> botCommandMap;

    public CommandRegistry() {
        this.botCommandMap = new LinkedHashMap<>();
    }

    public int getSize() {
        return botCommandMap.keySet().size();
    }

    public Set<String> getCommandNames() {
        return botCommandMap.keySet();
    }

    public void register(BotCommand botCommand) {
        botCommandMap.put(botCommand.getCmd(), botCommand);
    }

    public Collection<BotCommand> getAllCommands() {
        return botCommandMap.values();
    }

    public boolean hasCommand(String name) {
        return botCommandMap.containsKey(name);
    }

    public BotCommand getCommand(String name) {
        return botCommandMap.get(name);
    }


}
