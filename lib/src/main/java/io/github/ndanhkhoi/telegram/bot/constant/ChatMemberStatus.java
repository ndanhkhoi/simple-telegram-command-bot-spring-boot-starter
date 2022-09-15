package io.github.ndanhkhoi.telegram.bot.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @author ndanhkhoi
 * Created at 20:12:41 October 05, 2021
 * An enum for status of user in a group
 */
@RequiredArgsConstructor
public enum ChatMemberStatus {

    CREATOR("creator"),
    ADMINISTRATOR("administrator"),
    MEMBER("member"),
    LEFT("left"),
    KICKED("kicked"),
    ;

    @Getter
    private final String value;

    public static ChatMemberStatus fromStatusString(String value) {
        return Arrays.stream(values())
                .filter(e -> StringUtils.equals(value, e.value))
                .findFirst()
                .orElse(null);
    }

}
