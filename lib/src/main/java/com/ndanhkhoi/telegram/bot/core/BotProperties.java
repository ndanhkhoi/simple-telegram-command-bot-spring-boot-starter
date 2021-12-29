package com.ndanhkhoi.telegram.bot.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ndanhkhoi
 * Created at 22:09:23 October 05, 2021
 */
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "khoinda.bot", ignoreUnknownFields = false)
public class BotProperties {
    private String username;
    private String token;
    private String loggerChatId;
    private String botOwnerChatId;
    private String botRoutePackages;
    private String startTemplateFile;
}
