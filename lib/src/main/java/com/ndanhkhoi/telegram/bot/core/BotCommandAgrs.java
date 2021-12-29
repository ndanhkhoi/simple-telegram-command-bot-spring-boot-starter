package com.ndanhkhoi.telegram.bot.core;

import com.ndanhkhoi.telegram.bot.annotation.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 17:40:38 October 05, 2021
 *  Pojo to stored args of method that marked by {@link CommandMapping} annotation
 */
@Getter
@Setter
@Builder(setterPrefix = "with")
public class BotCommandAgrs {

    /**
     * A command
     * */
    private String command;

    /**
     * An update
     * */
    @TypeArg
    private Update update;

    /**
     * Command's body
     * */
    @AnnotaionArg(CommandBody.class)
    private String cmdBody;

    /**
     * Id of user sent a message
     * */
    @AnnotaionArg(SendUsername.class)
    private Long sendUserId;

    /**
     * Username of user sent a message
     * */
    @AnnotaionArg(SendUserId.class)
    private String sendUsername;

    /**
     * Id of a chat receive message
     * */
    @AnnotaionArg(ChatId.class)
    private Long chatId;

    /**
     * List of photos
     * */
    @TypeArg
    private List<PhotoSize> photoSizes;

}
