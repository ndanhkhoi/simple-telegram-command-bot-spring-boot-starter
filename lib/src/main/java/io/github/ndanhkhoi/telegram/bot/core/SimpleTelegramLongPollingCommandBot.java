package io.github.ndanhkhoi.telegram.bot.core;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 11:33:32 October 08, 2021
 */
@Slf4j
@RequiredArgsConstructor
public class SimpleTelegramLongPollingCommandBot extends TelegramLongPollingBot {
    private final BotProperties botProperties;

    @Override
    public String getBotUsername() {
        return botProperties.getUsername();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @SneakyThrows
    @Override
    public void onRegister() {
        super.onRegister();
        BotDispatcher.getInstance().onRegisterBot();
    }

    @Override
    public void onUpdateReceived(Update update) {
        BotDispatcher.getInstance().getUpdateSubscriber().consume(Mono.just(update));
    }

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        BotDispatcher.getInstance().getUpdateSubscriber().consume(Flux.fromIterable(updates));
    }

}