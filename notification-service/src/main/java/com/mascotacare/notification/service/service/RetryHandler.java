package com.mascotacare.notification.service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RetryHandler (componente §C4 Notification): reintentos con backoff
 * exponencial. Máximo 3 intentos: 200 ms, 400 ms, 800 ms.
 */
@Slf4j
@Component
public class RetryHandler {

    private static final int MAX_RETRIES = 3;
    private static final long BASE_DELAY_MS = 200L;

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Ejecuta la acción con backoff exponencial. Devuelve los intentos consumidos. */
    public Result execute(ThrowingRunnable action) {
        Exception last = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                action.run();
                return Result.success(attempt);
            } catch (Exception e) {
                last = e;
                log.warn("intento {} falló: {}", attempt, e.getMessage());
                if (attempt < MAX_RETRIES) sleep(BASE_DELAY_MS * (1L << (attempt - 1)));
            }
        }
        return Result.failure(MAX_RETRIES, last);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public record Result(boolean ok, int intentos, String errorMessage) {
        public static Result success(int n) { return new Result(true, n, null); }
        public static Result failure(int n, Exception e) {
            return new Result(false, n, e == null ? "unknown" : e.getMessage());
        }
    }
}
