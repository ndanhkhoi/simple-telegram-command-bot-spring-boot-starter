package io.github.ndanhkhoi.telegram.bot.repository;

import io.github.ndanhkhoi.telegram.bot.model.UpdateTrace;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author ndanhkhoi
 * Created at 19:03:48 April 29, 2022
 */
public interface UpdateTraceRepository {

    /**
     * Adds a trace to the repository.
     * @param trace the trace to add
     */
    void add(Mono<UpdateTrace> trace);

    /**
     * Adds a trace to the repository.
     * @param traces trace list to add
     */
    void addAll(Flux<UpdateTrace> traces);

    /**
     * Find all {@link UpdateTrace} objects contained in the repository.
     * @return the results
     */
    Flux<UpdateTrace> fluxAll();

}
