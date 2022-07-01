package com.ndanhkhoi.telegram.bot.core;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

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
    private String loggingChatId;
    private List<String> botOwnerChatId = new ArrayList<>();
    private List<String> botRoutePackages = new ArrayList<>();
    private Boolean enableUpdateTrace = false;
    private Boolean disableDefaultCommands = false;
}
