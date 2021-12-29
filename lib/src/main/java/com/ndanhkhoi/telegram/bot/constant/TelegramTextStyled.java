package com.ndanhkhoi.telegram.bot.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author ndanhkhoi
 * Created at 20:12:41 October 05, 2021
 * An enum for html tags of html message
 */
@Getter
@RequiredArgsConstructor
public enum TelegramTextStyled {

    BOLD("<b>", "</b>"),
    ITALIC("<i>", "</i>"),
    CODE("<code>", "</code>"),
    STRIKE("<s>", "</s>"),
    UNDERLINE("<u>", "</u>"),
    PRE("<pre>", "</pre>")
    ;
    private final String openTag;
    private final String closeTag;

}
