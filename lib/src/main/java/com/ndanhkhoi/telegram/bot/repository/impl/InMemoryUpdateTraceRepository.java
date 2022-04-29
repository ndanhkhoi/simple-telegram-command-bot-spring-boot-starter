package com.ndanhkhoi.telegram.bot.repository.impl;

import com.ndanhkhoi.telegram.bot.model.UpdateTrace;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import reactor.core.publisher.Flux;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ndanhkhoi
 * Created at 19:06:28 April 29, 2022
 */
public class InMemoryUpdateTraceRepository implements UpdateTraceRepository {

    private int capacity = 100;

    private boolean reverse = true;

    private final List<UpdateTrace> traces = new LinkedList<>();

    /**
     * Flag to say that the repository lists traces in reverse order.
     * @param reverse flag value (default true)
     */
    public void setReverse(boolean reverse) {
        synchronized (this.traces) {
            this.reverse = reverse;
        }
    }

    /**
     * Set the capacity of the in-memory repository.
     * @param capacity the capacity
     */
    public void setCapacity(int capacity) {
        synchronized (this.traces) {
            this.capacity = capacity;
        }
    }

    @Override
    public List<UpdateTrace> findAll() {
        synchronized (this.traces) {
            return List.copyOf(this.traces);
        }
    }

    @Override
    public Flux<UpdateTrace> fluxAll() {
        synchronized (this.traces) {
            return Flux.fromStream(this.traces.stream());
        }
    }

    @Override
    public void add(UpdateTrace trace) {
        synchronized (this.traces) {
            while (this.traces.size() >= this.capacity) {
                this.traces.remove(this.reverse ? this.capacity - 1 : 0);
            }
            if (this.reverse) {
                this.traces.add(0, trace);
            }
            else {
                this.traces.add(trace);
            }
        }
    }

}
