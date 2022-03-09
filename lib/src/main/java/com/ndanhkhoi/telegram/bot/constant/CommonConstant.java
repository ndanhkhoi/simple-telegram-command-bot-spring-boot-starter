package com.ndanhkhoi.telegram.bot.constant;

import lombok.experimental.UtilityClass;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.regex.Pattern;

/**
 * @author khoinda
 * Created at 13:58:27 December 29, 2021
 */
@UtilityClass
public final class CommonConstant {

    public static final String CMD_PREFIX = "/";
    public static final int CMD_MAX_LENGTH = 32;
    public static final Pattern CMD_PATTERN = Pattern.compile("^[a-z0-9_]*$");
    public static final String ERROR_NOTIFY_MESSAGE = "There is an error. Please contact bot owner to check it !";
    public static final String HELP_CMD = "/help";
    public static final String HELP_CMD_DESCRIPTION = "List of available command(s) for this chat";
    public static final String START_CMD = "/start";
    public static final String START_CMD_DESCRIPTION = "Start chat with a bot";
    public static final String GET_LOG_FILE_CMD = "/get_log_file";
    public static final String GET_LOG_FILE_DESCRIPTION = "Get an application log file";
    public static final BotCommand HELP_BOT_COMMAND = new BotCommand(HELP_CMD, HELP_CMD_DESCRIPTION);

}
