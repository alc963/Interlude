package com.naugroup3.interlude.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.naugroup3.interlude.utils.RateLimiter.Executor;

class RateLimiterTest {
    private ScheduledExecutorService scheduler;
    private SharedRateLimitBudget budget;

    @BeforeEach
    void setUp() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        budget = new SharedRateLimitBudget(20, scheduler);
    }

    @AfterEach
    void tearDown() {
        scheduler.shutdownNow();
    }

    private Executor<Integer, Integer, HttpResponse<String>> increment() {
        return (x) -> CompletableFuture.completedFuture(new Expected.Success<>(1 + x));
    }

    private <T> T await(CompletableFuture<T> future) throws Exception {
        return future.get(5, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mock_429() {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(429);
        HttpHeaders headers = HttpHeaders.of(Map.of(), (k, v) -> true);
        when(response.headers()).thenReturn(headers);
        return response;
    }

    @Test
    @DisplayName("two limters on the same budget")
    void shared_budget_results() throws Exception {
        final RateLimiter<Integer, Integer, HttpResponse<String>> limiter_1 = new RateLimiter<>(budget, null, increment());
        final RateLimiter<Integer, Integer, HttpResponse<String>> limiter_2 = new RateLimiter<>(budget, null, increment());
        try {
            var result_1_1 = limiter_1.add(10);
            var result_1_2 = limiter_1.add(20);
            var result_2_1 = limiter_2.add(41);

            assertThat(await(result_1_1.future).has_value()).isTrue();
            assertThat(await(result_1_1.future).value()).isEqualTo(11);
            assertThat(await(result_1_2.future).value()).isEqualTo(21);
            assertThat(await(result_2_1.future).value()).isEqualTo(42);
        } finally {
            limiter_1.shutdown();
            limiter_2.shutdown();
        }
    }

    @Test
    @DisplayName("a 429 is re-queued and retried until it succeeds")
    void retries_on_429() throws Exception {
        final HttpResponse<String> TOO_MANY_REQUESTS = mock_429();
        final AtomicInteger calls = new AtomicInteger();
        final Executor<Integer, Integer, HttpResponse<String>> executor = x -> {
            if (calls.getAndIncrement() == 0) {
                return CompletableFuture.completedFuture(new Expected.Failure<>(TOO_MANY_REQUESTS));
            }
            return CompletableFuture.completedFuture(new Expected.Success<>(1 + x));
        };

        final RateLimiter<Integer, Integer, HttpResponse<String>> limiter = new RateLimiter<>(budget, null, executor);
        try {
            var item = limiter.add(41);

            assertThat(await(item.future).has_value()).isTrue();
            assertThat(await(item.future).value()).isEqualTo(42);
            assertThat(calls.get()).isEqualTo(2);
        } finally {
            limiter.shutdown();
        }
    }
}
