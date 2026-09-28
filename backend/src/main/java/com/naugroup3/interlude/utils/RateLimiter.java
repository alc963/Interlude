package com.naugroup3.interlude.utils;

import java.net.http.HttpResponse;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class RateLimiter<Data, Result, Err> {
    public sealed interface PrecheckResult<Result> {
        record Proceed<Result>() implements PrecheckResult<Result> {}
        record Resolved<Result>(Result value) implements PrecheckResult<Result> {}
        default boolean proceed() { return this instanceof Proceed<Result>; }
    }

    @FunctionalInterface
    public interface Precheck<Data, Result> {
        PrecheckResult<Result> check(Data data);
    }

    public static final class PendingItem<Data, Result, Err> {
        public final Data data;
        public final CompletableFuture<Expected<Result, Err>> future;
        public PendingItem(Data data, CompletableFuture<Expected<Result, Err>> future) {
            this.data = data;
            this.future = future;
        }
    }

    @FunctionalInterface
    public interface Executor<Data, Result, Err> {
        CompletableFuture<Expected<Result, Err>> execute(Data data);
    }

    private final SharedRateLimitBudget budget;

    private final Object lock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final ArrayDeque<PendingItem<Data, Result, Err>> queue = new ArrayDeque<>();
    private final Precheck<Data, Result> precheck;
    private final Executor<Data, Result, Err> executor;

    public RateLimiter(SharedRateLimitBudget budget,
        Precheck<Data, Result> precheck, 
        Executor<Data, Result, Err> executor
    ){
        this.budget = budget;
        
        this.precheck = precheck;
        this.executor = executor;
        
        this.budget.add_on_token_refill_callback(this::try_dispatch);
    }

    private void try_dispatch(){
        PendingItem<Data, Result, Err> selected_item = null;

        synchronized(lock) {
            if (queue.isEmpty()) return;

            // use iterator to remove resolved items in loop
            Iterator<PendingItem<Data, Result, Err>> it = queue.iterator();
            while(it.hasNext()) {
                PendingItem<Data, Result, Err> item = it.next();

                if (precheck == null) {
                    selected_item = item;
                    break;
                }
                PrecheckResult<Result> precheck_result = precheck.check(item.data);
                if(precheck_result instanceof PrecheckResult.Proceed<Result>) {
                    selected_item = item;
                    break;
                } else if(precheck_result instanceof PrecheckResult.Resolved<Result> pr) {
                    it.remove();
                    item.future.complete(new Expected.Success<>(pr.value()));
                }
            }
            if(selected_item == null) return;

            if (!budget.reserve()) return;

            queue.remove(selected_item);
        }

        run_once(selected_item);
    }

    private void run_once(PendingItem<Data, Result, Err> item) {
        this.executor.execute(item.data).whenComplete((expected, err) -> {
            if(err != null) {
                this.budget.release();
                item.future.completeExceptionally(err);
                return;
            }

            if(expected.has_value()) {
                budget.release();
                item.future.complete(expected);
            }
            // likely externally rate-limited?
            else {
                if(HttpResponse.class.isInstance(expected.error())) {
                    synchronized(lock) {
                        final HttpResponse<String> response = (HttpResponse<String>)expected.error();
                        budget.set_backoff(response);
                        queue.addFirst(item);
                    }
                    budget.release();
                }
                else {
                    budget.release();
                    item.future.complete(expected);
                }
            }
        });
    }

    public PendingItem<Data, Result, Err> add(Data data){
        CompletableFuture<Expected<Result, Err>> future = new CompletableFuture<>();
        PendingItem<Data, Result, Err> item = new PendingItem<>(data, future);
        synchronized(lock) {
            this.queue.addLast(item);
        }
        try_dispatch();
        return item;
    }

    public void shutdown(){
        this.scheduler.shutdown();
    }
}
