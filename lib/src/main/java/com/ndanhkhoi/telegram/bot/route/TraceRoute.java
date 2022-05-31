package com.ndanhkhoi.telegram.bot.route;

import com.ndanhkhoi.telegram.bot.annotation.BotRoute;
import com.ndanhkhoi.telegram.bot.annotation.CommandDescription;
import com.ndanhkhoi.telegram.bot.annotation.CommandMapping;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import com.ndanhkhoi.telegram.bot.utils.UpdateObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import reactor.core.publisher.Mono;

/**
 * @author ndanhkhoi
 * Created at 18:31:54 April 29, 2022
 */
@ConditionalOnProperty(value = "khoinda.bot.enable-update-trace", havingValue = "true")
@BotRoute
@Slf4j
public class TraceRoute {

    private final UpdateTraceRepository updateTraceRepository;
    private final UpdateObjectMapper updateObjectMapper;

    public TraceRoute(UpdateTraceRepository updateTraceRepository, @Qualifier("updateObjectMapper") UpdateObjectMapper updateObjectMapper) {
        this.updateTraceRepository = updateTraceRepository;
        this.updateObjectMapper = updateObjectMapper;
    }

    @CommandDescription("Trace last 100 update incoming")
    @CommandMapping(value = "/update_trace", onlyForOwner = true)
    public Mono<String> updateTrace() {
        return updateTraceRepository.fluxAll()
                .collectList()
                .map(updateObjectMapper::writeValueAsPrettyString);
    }

}
