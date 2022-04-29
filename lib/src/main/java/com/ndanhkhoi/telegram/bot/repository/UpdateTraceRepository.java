package com.ndanhkhoi.telegram.bot.repository;

import com.ndanhkhoi.telegram.bot.model.UpdateTrace;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 19:03:48 April 29, 2022
 */
public interface UpdateTraceRepository {

    /**
     * Find all {@link UpdateTrace} objects contained in the repository.
     * @return the results
     */
    List<UpdateTrace> findAll();

    /**
     * Adds a trace to the repository.
     * @param trace the trace to add
     */
    void add(UpdateTrace trace);

    Flux<UpdateTrace> fluxAll();

}
