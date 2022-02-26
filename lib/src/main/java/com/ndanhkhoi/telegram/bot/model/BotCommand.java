package com.ndanhkhoi.telegram.bot.model;

import com.ndanhkhoi.telegram.bot.annotation.CommandBody;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.constant.MediaType;
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
    private Boolean useHtml;

    /**
     * @see CommandMapping#disableWebPagePreview()
     * */
    private Boolean disableWebPagePreview;

    /**
     * @see CommandMapping#accessUserIds()
     * */
    private long[] accessUserIds;

    /**
     * @see CommandMapping#accessGroupIds()
     * */
    private long[] accessGroupIds;

    /**
     * @see CommandMapping#allowAllUserAccess()
     * */
    private Boolean allowAllUserAccess;

    /**
     * @see CommandMapping#onlyAdmin()
     * */
    private Boolean onlyAdmin;

    /**
     * @see CommandMapping#sendFile()
     * */
    private MediaType sendFile;

    /**
     * @see CommandMapping#onlyForOwner()
     * */
    private Boolean onlyForOwner;

    /**
     * A method that is marked by {@link CommandMapping} annotation
     * */
    private Method method;

    /**
     * {@link com.ndanhkhoi.telegram.bot.annotation.CommandDescription}
     * */
    private String description;

    /**
     * Description of command's body
     * @see CommandBody#description()
     * */
    private String bodyDescription;

}
