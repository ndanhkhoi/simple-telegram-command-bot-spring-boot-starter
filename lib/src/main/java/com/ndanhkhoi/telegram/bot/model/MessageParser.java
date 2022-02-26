package com.ndanhkhoi.telegram.bot.model;

import lombok.Getter;

/**
 * @author ndanhkhoi
 * Created at 00:44:51 February 27, 2022
 */
@Getter
public class MessageParser {

    private final String firstWord;
    private final String remainingText;

    public MessageParser(String text) {
        String[] arr = text.split(" ");
        String command = arr[0];
        String body = "";
        if (arr.length > 1) {
            body = text.split(command + " ")[1];
        }
        this.firstWord = command;
        this.remainingText = body;
    }

}
