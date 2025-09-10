package org.vimal.api;

import io.restassured.response.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public final class Common {
    private Common() {
    }

    private static final long DEFAULT_TIMEOUT_SECONDS = 30;

    public static Response waitForResponse(Supplier<Response> apiCall) throws ExecutionException, InterruptedException {
        return waitForResponse(
                apiCall,
                DEFAULT_TIMEOUT_SECONDS
        );
    }

    private static Response waitForResponse(Supplier<Response> apiCall,
                                            long timeOutSeconds) throws ExecutionException, InterruptedException {
        try {
            return CompletableFuture.supplyAsync(apiCall)
                    .get(
                            timeOutSeconds,
                            TimeUnit.SECONDS
                    );
        } catch (TimeoutException ex) {
            throw new AssertionError("API call timed out after " + timeOutSeconds + " seconds", ex);
        }
    }
}
