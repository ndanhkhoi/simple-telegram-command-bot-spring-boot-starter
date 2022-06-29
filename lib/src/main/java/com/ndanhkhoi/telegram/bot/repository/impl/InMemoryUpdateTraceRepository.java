package com.ndanhkhoi.telegram.bot.repository.impl;

import com.ndanhkhoi.telegram.bot.model.UpdateTrace;
import com.ndanhkhoi.telegram.bot.repository.UpdateTraceRepository;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author ndanhkhoi
 * Created at 19:06:28 April 29, 2022
 */
public class InMemoryUpdateTraceRepository implements UpdateTraceRepository, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private int capacity = 100;

    private boolean reverse = true;

    private final List<UpdateTrace> traces = new CopyOnWriteArrayList<>();

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

    private void subscribe(UpdateTrace value) {
        while (this.traces.size() >= this.capacity) {
            this.traces.remove(this.reverse ? this.capacity - 1 : 0);
        }
        if (this.reverse) {
            this.traces.add(0, value);
        }
        else {
            this.traces.add(value);
        }
    }

    @Override
    public Flux<UpdateTrace> fluxAll() {
        return Flux.fromStream(this.traces.stream());
    }

    @Override
    public void add(Mono<UpdateTrace> trace) {
        trace.subscribeOn(Schedulers.fromExecutor(applicationContext.getBean("botAsyncTaskExecutor", SimpleAsyncTaskExecutor.class))).subscribe(this::subscribe);
    }

    @Override
    public void addAll(Flux<UpdateTrace> traces) {
        traces.subscribeOn(Schedulers.fromExecutor(applicationContext.getBean("botAsyncTaskExecutor", SimpleAsyncTaskExecutor.class))).subscribe(this::subscribe);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
