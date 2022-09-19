package io.github.ndanhkhoi.telegram.bot.annotation;

import io.github.ndanhkhoi.telegram.bot.constant.MediaType;
import io.github.ndanhkhoi.telegram.bot.constant.MessageParseMode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author ndanhkhoi
 * Created at 10:20:44 April 09, 2021
 * An annotation to mark a method as a command handler.
 * The return value of method will be reply to a chat that send a command
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandMapping {

    /**
     * An array of commands to handle
     * */
    String[] value();

    /**
     * Send result as HTML or Markdown or Plain text
     * */
    MessageParseMode parseMode() default MessageParseMode.PLAIN;

    /**
     * A flag to disable webpage preview on message
     * */
    boolean disableWebPagePreview() default false;

    /**
     * An id array of users can call a command
     * */
    long[] accessUserIds() default {};

    /**
     * An id array of users in group can call a command
     * */
    long[] accessMemberIds() default {};

    /**
     * An id array of groups can call a command
     * */
    long[] accessGroupIds() default {};

    /**
     * A flag to mark a command can be called by everyone
     * */
    boolean allowAllUserAccess() default false;

    /**
     * A flag to mark a command can be called by any groups
     * */
    boolean allowAllGroupAccess() default false;

    /**
     * A flag to mark a command can be called by admin (of a group)
     * */
    boolean onlyAdmin() default false;

    /**
     * A flag to mark a command can be called in groups
     * */
    boolean onlyForGroup() default false;

    /**
     * A flag to mark a command can be called in private chat
     * */
    boolean onlyForPrivate() default false;

    /**
     * Reply media message.
     * @see MediaType
     * */
    MediaType sendFile() default MediaType.DOCUMENT;

    /**
     * A flag to mark a command can be called by owner
     * */
    boolean onlyForOwner() default false;

}
