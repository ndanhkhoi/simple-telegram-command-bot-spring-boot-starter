package io.github.ndanhkhoi.telegram.bot.core;

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

    private Boolean enableAutoConfig = true;
    private String username;
    private String token;
    private String loggingChatId;
    private List<String> botOwnerChatId = new ArrayList<>();
    private List<String> botRoutePackages = new ArrayList<>();
    private Boolean enableUpdateTrace = false;
    private Boolean disableDefaultCommands = false;
    private Executor executor = new Executor();
    private Integer registerDelay = 0;
    private Boolean showCommandMenu = true;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Executor {
        private int corePoolSize = 8;
        private int maxPoolSize = Integer.MAX_VALUE;
        private int queueCapacity = Integer.MAX_VALUE;
        private String threadNamePrefix = "bot-task-";
    }

}
