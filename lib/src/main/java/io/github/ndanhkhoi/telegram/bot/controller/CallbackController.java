package io.github.ndanhkhoi.telegram.bot.controller;

import io.github.ndanhkhoi.telegram.bot.core.BotProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import org.telegram.telegrambots.updatesreceivers.ServerlessWebhook;

import java.util.NoSuchElementException;

/**
 * Created at 15:53:17 January 12, 2023,
 * <br>
 * Callback controller for webhook bot
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@ConditionalOnProperty(value = "khoinda.bot.webhook.use-webhook", havingValue = "true")
@RequestMapping("/callback")
public class CallbackController {

    private final ServerlessWebhook webhook;
    private final BotProperties botProperties;

    @PostMapping("/{botPath}")
    public ResponseEntity<CallbackResponse> updateReceived(@RequestHeader("X-Telegram-Bot-Api-Secret-Token") String secretToken, @PathVariable("botPath") String botPath, @RequestBody Update update) {
        CallbackResponse response;

        if (StringUtils.equals(botProperties.getWebhook().getSecretToken(), secretToken)) {
            try {
                webhook.updateReceived(botPath, update);
                response = CallbackResponse.builder()
                        .success(true)
                        .message(HttpStatus.OK.toString())
                        .build();
            }
            catch (NoSuchElementException e) {
                response = CallbackResponse.builder()
                        .success(false)
                        .message(HttpStatus.NOT_FOUND.toString())
                        .build();
            }
            catch (TelegramApiValidationException e) {
                response = CallbackResponse.builder()
                        .success(false)
                        .message(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                        .build();
            }
        }
        else {
            response = CallbackResponse.builder()
                    .success(false)
                    .message(HttpStatus.UNAUTHORIZED.toString())
                    .build();
        }
        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping(path = "/{botPath}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> testReceived(@PathVariable("botPath") String botPath) {
        if (StringUtils.equals(botProperties.getUsername(), botPath)) {
            return ResponseEntity.ok("Hi there " + botPath + "!");
        }
        else {
            return ResponseEntity.ok("Callback not found for " + botPath);
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    private static class CallbackResponse {
        private boolean success;
        private String message;
    }

}
