package com.naugroup3.interlude.utils;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.OptionalLong;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SharedRateLimitBudget {
    private final int refill_interval_seconds;
    private int capacity;
    private int tokens;
    private long backoff_seconds;

    private boolean in_flight = false;

    private final List<Runnable> on_token_refill_callbacks = new CopyOnWriteArrayList<>();

    private final Object lock = new Object();

    public SharedRateLimitBudget(int requests_per_minute, ScheduledExecutorService scheduler) {
        this.capacity = requests_per_minute;
        this.tokens = requests_per_minute;
        this.refill_interval_seconds = 60 / requests_per_minute;

        scheduler.scheduleAtFixedRate(this::tick, refill_interval_seconds, refill_interval_seconds, TimeUnit.SECONDS);
    }

    private void tick() {
        synchronized(lock) {
            if (this.backoff_seconds > 0) this.backoff_seconds--;
            else if (this.tokens < this.capacity) this.tokens++;
        }
        run_on_token_refill_callbacks();
    }

    public synchronized boolean reserve() {
        if(this.tokens <= 0) return false;
        if(this.backoff_seconds > 0) return false;
        if(this.in_flight) return false;
        this.tokens--;
        this.in_flight = true;
        return true;
    }
    public synchronized void release() {
        this.in_flight = false;
        run_on_token_refill_callbacks();
    }

    public synchronized void set_backoff(int backoff_seconds) { this.backoff_seconds = backoff_seconds; }
    public synchronized void set_backoff(HttpResponse<String> response) { 
        OptionalLong retry_after = response.headers().firstValueAsLong("Retry-After");
        this.backoff_seconds = retry_after.orElse(0);
    }

    public void add_on_token_refill_callback(Runnable callback) {on_token_refill_callbacks.add(callback); }
    private void run_on_token_refill_callbacks(){ on_token_refill_callbacks.forEach(Runnable::run); }
}
