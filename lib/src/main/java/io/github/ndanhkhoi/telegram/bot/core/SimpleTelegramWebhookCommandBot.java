package io.github.ndanhkhoi.telegram.bot.core;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@Slf4j
public class SimpleTelegramWebhookCommandBot extends TelegramWebhookBot {

    private final BotProperties botProperties;

    public SimpleTelegramWebhookCommandBot(BotProperties botProperties) {
        super(botProperties.getToken());
        this.botProperties = botProperties;
    }

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @SneakyThrows
    @Override
    public void onRegister() {
        super.onRegister();
        BotDispatcher.getInstance().onRegisterBot();
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        BotDispatcher.getInstance().getUpdateSubscriber().consume(Mono.just(update));
        return null;
    }

    @Override
    public String getBotPath() {
        return getBotUsername();
    }

}

