package io.github.ndanhkhoi.telegram.bot.model;

import io.github.ndanhkhoi.telegram.bot.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
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
@AllArgsConstructor
public class BotCommandParams {

    /**
     * An update
     * */
    @TypeArg
    private Update update;

    /**
     * A message
     * */
    @TypeArg
    private Message message;

    /**
     * Command's body
     * */
    @AnnotaionArg(CommandBody.class)
    private String cmdBody;

    /**
     * Id of user sent a message
     * */
    @AnnotaionArg(SendUserId.class)
    private Long sendUserId;

    /**
     * Username of user sent a message
     * */
    @AnnotaionArg(SendUsername.class)
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

    /**
     * File received
     * */
    @TypeArg
    private Document document;

    @AnnotaionArg(CommandName.class)
    private String commandName;

}
