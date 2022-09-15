package io.github.ndanhkhoi.telegram.bot.model;

import io.github.ndanhkhoi.telegram.bot.annotation.CommandBody;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import io.github.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import io.github.ndanhkhoi.telegram.bot.constant.MediaType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;

/**
 * @author ndanhkhoi
 * Created at 17:00:46 October 05, 2021
 * A Bot command handler instance.
 * @see CommandMapping
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(setterPrefix = "with")
public class BotCommand {

    /**
     * A command
     * */
    private String cmd;

    /**
     * @see CommandMapping#useHtml()
     * */
    private boolean useHtml;

    /**
     * @see CommandMapping#disableWebPagePreview()
     * */
    private boolean disableWebPagePreview;

    /**
     * @see CommandMapping#accessUserIds()
     * */
    private long[] accessUserIds;

    /**
     * @see CommandMapping#accessMemberIds()
     * */
    private long[] accessMemberIds;

    /**
     * @see CommandMapping#accessGroupIds()
     * */
    private long[] accessGroupIds;

    /**
     * @see CommandMapping#allowAllUserAccess()
     * */
    private boolean allowAllUserAccess;

    /**
     * @see CommandMapping#onlyAdmin()
     * */
    private boolean onlyAdmin;

    /**
     * @see CommandMapping#sendFile()
     * */
    private MediaType sendFile;

    /**
     * @see CommandMapping#onlyForOwner()
     * */
    private boolean onlyForOwner;

    /**
     * A method that is marked by {@link CommandMapping} annotation
     * */
    private Method method;

    /**
     * {@link CommandDescription}
     * */
    private String description;

    /**
     * Description of command's body
     * @see CommandBody#description()
     * */
    private String bodyDescription;

}
