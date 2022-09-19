package io.github.ndanhkhoi.telegram.bot.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.ParseMode;

/**
 * Created at 09:56:25 September 19, 2022,
 */
@RequiredArgsConstructor
public enum MessageParseMode {
    MARKDOWN(ParseMode.MARKDOWN),
    MARKDOWNV2(ParseMode.MARKDOWNV2),
    HTML(ParseMode.HTML),
    PLAIN("PLAIN")
    ;
    @Getter
    private final String value;
}
