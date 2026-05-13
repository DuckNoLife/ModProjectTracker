package com.example.modprojecttracker.ai;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.example.modprojecttracker.BuildConfig;
import com.example.modprojecttracker.util.NetworkUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gemini API wrapper using direct OkHttp calls to the Google Generative Language REST API.
 */
public class GeminiService {

    public interface GeminiCallback {
        void onSuccess(String text);

        void onError(String message);
    }

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String GEMINI_HOST = "generativelanguage.googleapis.com";
    private static final String GEMINI_API_VERSION = "v1beta";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private static final int MAX_ATTEMPTS = 4;
    private static final long BASE_BACKOFF_MS = 1_000L;
    private static final long MAX_BACKOFF_MS = 15_000L;
    private static final long MIN_REQUEST_INTERVAL_MS = 1_000L;
    private static final long DUPLICATE_PROMPT_WINDOW_MS = 1_500L;

    private static final long DEFAULT_RATE_LIMIT_COOLDOWN_MS = 20_000L;
    private static final long MAX_RATE_LIMIT_COOLDOWN_MS = 60_000L;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AtomicBoolean inFlight = new AtomicBoolean(false);
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    private volatile long lastRequestTimestampMs = 0L;
    private volatile String lastPrompt = "";
    private volatile long lastPromptTimestampMs = 0L;
    private volatile long rateLimitedUntilMs = 0L;

    /** Returns true when GEMINI_API_KEY is available. */
    public boolean isConfigured() {
        return !TextUtils.isEmpty(BuildConfig.GEMINI_API_KEY);
    }

    private String resolveModelName() {
        String configured = BuildConfig.GEMINI_MODEL;
        if (TextUtils.isEmpty(configured)) {
            return DEFAULT_MODEL;
        }
        configured = configured.trim();
        return TextUtils.isEmpty(configured) ? DEFAULT_MODEL : configured;
    }

    /** Sends the prepared prompt payload and returns the result via callback. */
    public void ask(@NonNull Context context,
                    @NonNull String question,
                    @NonNull GeminiCallback callback) {

        String trimmedQuestion = question.trim();
        if (TextUtils.isEmpty(trimmedQuestion)) {
            callback.onError("Please enter a message.");
            return;
        }

        // Validate required configuration before sending.
        if (!isConfigured()) {
            callback.onError("Gemini is not configured. Please add GEMINI_API_KEY to local.properties.");
            return;
        }

        // Fail fast when there is no network connection.
        if (!NetworkUtil.isNetworkAvailable(context)) {
            callback.onError("No internet connection available.");
            return;
        }

        long remainingRateLimitMs = getRemainingRateLimitMs();
        if (remainingRateLimitMs > 0L) {
            callback.onError(buildRateLimitCooldownMessage(remainingRateLimitMs));
            return;
        }

        if (isDuplicatePrompt(trimmedQuestion)) {
            callback.onError("Please wait a moment before sending the same message again.");
            return;
        }

        if (!inFlight.compareAndSet(false, true)) {
            callback.onError("A Gemini request is already in progress. Please wait.");
            return;
        }

        rememberPrompt(trimmedQuestion);

        executorService.execute(() -> {
            try {
                applyMinimumInterval();
                String modelName = resolveModelName();
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("Check API", "Request payload: " + trimmedQuestion);
                }
                requestWithRetry(modelName, trimmedQuestion, callback);
            } catch (Exception exception) {
                callback.onError(buildErrorMessage(exception));
            } finally {
                inFlight.set(false);
            }
        });
    }

    private void requestWithRetry(@NonNull String modelName,
                                  @NonNull String question,
                                  @NonNull GeminiCallback callback) {
        String lastErrorMessage = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                String text = generateContent(modelName, question);
                if (TextUtils.isEmpty(text)) {
                    throw new GeminiApiException(200, "EMPTY_RESPONSE", "Gemini returned an empty response.", -1L);
                }

                callback.onSuccess(text);
                return;
            } catch (Exception exception) {
                if (isRateLimitError(exception)) {
                    long cooldownMs = computeRateLimitCooldownMs(exception);
                    applyRateLimitCooldown(cooldownMs);
                    callback.onError(buildRateLimitCooldownMessage(cooldownMs));
                    return;
                }

                lastErrorMessage = buildErrorMessage(exception);

                if (!isRetryable(exception) || attempt >= MAX_ATTEMPTS) {
                    callback.onError(lastErrorMessage);
                    return;
                }

                long delayMs = computeRetryDelayMs(exception, attempt);
                if (!sleepQuietly(delayMs)) {
                    callback.onError("Gemini request was interrupted.");
                    return;
                }
            }
        }

        callback.onError(lastErrorMessage != null
                ? lastErrorMessage
                : "Gemini request failed after retries.");
    }

    private String generateContent(@NonNull String modelName,
                                   @NonNull String prompt) throws Exception {
        HttpUrl endpoint = buildEndpoint(modelName);
        JSONObject payload = new JSONObject()
                .put("contents", new JSONArray()
                        .put(new JSONObject()
                                .put("parts", new JSONArray()
                                        .put(new JSONObject().put("text", prompt)))));

        Request request = new Request.Builder()
                .url(endpoint)
                .post(RequestBody.create(payload.toString(), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String rawBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw buildApiException(response.code(), response.header("Retry-After"), rawBody);
            }

            String text = extractTextFromResponse(rawBody);
            if (TextUtils.isEmpty(text)) {
                throw new GeminiApiException(response.code(), "EMPTY_RESPONSE", "Gemini returned an empty response.", -1L);
            }
            return text;
        }
    }

    private HttpUrl buildEndpoint(@NonNull String modelName) {
        String normalizedModelName = modelName.trim();
        return new HttpUrl.Builder()
                .scheme("https")
                .host(GEMINI_HOST)
                .addPathSegment(GEMINI_API_VERSION)
                .addPathSegment("models")
                .addPathSegment(normalizedModelName + ":generateContent")
                .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                .build();
    }

    private String extractTextFromResponse(@NonNull String rawBody) throws Exception {
        JSONObject root = new JSONObject(rawBody);
        JSONArray candidates = root.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) {
            return null;
        }

        StringBuilder output = new StringBuilder();
        for (int i = 0; i < candidates.length(); i++) {
            JSONObject candidate = candidates.optJSONObject(i);
            if (candidate == null) {
                continue;
            }
            JSONObject content = candidate.optJSONObject("content");
            if (content == null) {
                continue;
            }
            JSONArray parts = content.optJSONArray("parts");
            if (parts == null) {
                continue;
            }

            for (int p = 0; p < parts.length(); p++) {
                JSONObject part = parts.optJSONObject(p);
                if (part == null) {
                    continue;
                }
                String text = part.optString("text");
                if (!TextUtils.isEmpty(text)) {
                    if (output.length() > 0) {
                        output.append('\n');
                    }
                    output.append(text.trim());
                }
            }
        }

        String result = output.toString().trim();
        return TextUtils.isEmpty(result) ? null : result;
    }

    private GeminiApiException buildApiException(int httpCode,
                                                 String retryAfterHeader,
                                                 @NonNull String rawBody) {
        String status = "";
        String message = "HTTP " + httpCode;
        try {
            JSONObject root = new JSONObject(rawBody);
            JSONObject error = root.optJSONObject("error");
            if (error != null) {
                status = error.optString("status", "");
                String apiMessage = error.optString("message", "");
                if (!TextUtils.isEmpty(apiMessage)) {
                    message = apiMessage;
                }
                int apiCode = error.optInt("code", httpCode);
                if (apiCode > 0) {
                    httpCode = apiCode;
                }
            }
        } catch (Exception ignored) {
            if (!TextUtils.isEmpty(rawBody)) {
                message = rawBody;
            }
        }

        long retryAfterMs = extractRetryAfterMs(retryAfterHeader, message);
        return new GeminiApiException(httpCode, status, message, retryAfterMs);
    }

    private boolean isDuplicatePrompt(@NonNull String prompt) {
        long now = System.currentTimeMillis();
        return prompt.equals(lastPrompt) && (now - lastPromptTimestampMs) < DUPLICATE_PROMPT_WINDOW_MS;
    }

    private void rememberPrompt(@NonNull String prompt) {
        lastPrompt = prompt;
        lastPromptTimestampMs = System.currentTimeMillis();
    }

    private void applyMinimumInterval() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRequestTimestampMs;
        if (elapsed < MIN_REQUEST_INTERVAL_MS) {
            sleepQuietly(MIN_REQUEST_INTERVAL_MS - elapsed);
        }
        lastRequestTimestampMs = System.currentTimeMillis();
    }

    private long computeRetryDelayMs(@NonNull Throwable throwable, int attempt) {
        long retryAfterMs = extractRetryAfterMs(throwable);
        if (retryAfterMs > 0) {
            return Math.min(retryAfterMs, MAX_BACKOFF_MS);
        }

        long exponential = BASE_BACKOFF_MS * (1L << Math.max(0, attempt - 1));
        long capped = Math.min(exponential, MAX_BACKOFF_MS);
        long jitter = ThreadLocalRandom.current().nextLong(150L, 500L);
        return Math.min(capped + jitter, MAX_BACKOFF_MS);
    }

    private void applyRateLimitCooldown(long cooldownMs) {
        long safeCooldownMs = Math.max(1_000L, Math.min(cooldownMs, MAX_RATE_LIMIT_COOLDOWN_MS));
        rateLimitedUntilMs = System.currentTimeMillis() + safeCooldownMs;
    }

    private long getRemainingRateLimitMs() {
        long remaining = rateLimitedUntilMs - System.currentTimeMillis();
        return Math.max(remaining, 0L);
    }

    private long computeRateLimitCooldownMs(@NonNull Throwable throwable) {
        long retryAfterMs = extractRetryAfterMs(throwable);
        if (retryAfterMs > 0L) {
            return Math.min(retryAfterMs, MAX_RATE_LIMIT_COOLDOWN_MS);
        }
        return DEFAULT_RATE_LIMIT_COOLDOWN_MS;
    }

    private String buildRateLimitCooldownMessage(long cooldownMs) {
        long seconds = Math.max(1L, (cooldownMs + 999L) / 1_000L);
        return "Gemini is rate-limited. Please wait " + seconds + "s and try again.";
    }

    private boolean isRateLimitError(@NonNull Throwable throwable) {
        if (throwable instanceof GeminiApiException) {
            GeminiApiException apiException = (GeminiApiException) throwable;
            if (apiException.httpCode == 429) {
                return true;
            }
            String status = apiException.status == null ? "" : apiException.status.toLowerCase(Locale.US);
            if (status.contains("resource_exhausted") || status.contains("rate_limit") || status.contains("quota")) {
                return true;
            }
        }

        String message = throwable.getMessage();
        String lower = message == null ? "" : message.toLowerCase(Locale.US);
        return lower.contains("429")
                || lower.contains("resource_exhausted")
                || lower.contains("rate limit")
                || lower.contains("quota");
    }

    private boolean isRetryable(@NonNull Throwable throwable) {
        if (throwable instanceof GeminiApiException) {
            GeminiApiException apiException = (GeminiApiException) throwable;
            if (isRateLimitError(throwable)) {
                return false;
            }
            return apiException.httpCode == 408 || apiException.httpCode >= 500;
        }

        String message = throwable.getMessage();
        String lower = message == null ? "" : message.toLowerCase(Locale.US);

        return lower.contains("503")
                || lower.contains("unavailable")
                || lower.contains("timeout")
                || lower.contains("deadline")
                || throwable instanceof IOException;
    }

    private String buildErrorMessage(@NonNull Throwable throwable) {
        String message = throwable.getMessage();
        String lower = message == null ? "" : message.toLowerCase(Locale.US);

        if (isRateLimitError(throwable)) {
            long remainingMs = getRemainingRateLimitMs();
            if (remainingMs > 0L) {
                return buildRateLimitCooldownMessage(remainingMs);
            }
            return buildRateLimitCooldownMessage(DEFAULT_RATE_LIMIT_COOLDOWN_MS);
        }

        if (throwable instanceof GeminiApiException) {
            GeminiApiException apiException = (GeminiApiException) throwable;
            if (apiException.httpCode == 401 || apiException.httpCode == 403) {
                return "Gemini authentication failed. Check GEMINI_API_KEY in local.properties.";
            }
            if (apiException.httpCode == 400) {
                return "Gemini rejected the request. Please try a shorter or clearer prompt.";
            }
        }

        if (lower.contains("401") || lower.contains("403") || lower.contains("api key")) {
            return "Gemini authentication failed. Check GEMINI_API_KEY in local.properties.";
        }

        if (TextUtils.isEmpty(message)) {
            message = throwable.getClass().getSimpleName();
        }
        return "Gemini request failed: " + message;
    }

    private long extractRetryAfterMs(@NonNull Throwable throwable) {
        if (throwable instanceof GeminiApiException) {
            GeminiApiException apiException = (GeminiApiException) throwable;
            if (apiException.retryAfterMs > 0L) {
                return apiException.retryAfterMs;
            }
            return extractRetryAfterMs(null, apiException.getMessage());
        }
        return extractRetryAfterMs(null, throwable.getMessage());
    }

    private long extractRetryAfterMs(String retryAfterHeader, String fallbackMessage) {
        if (!TextUtils.isEmpty(retryAfterHeader)) {
            try {
                long seconds = Long.parseLong(retryAfterHeader.trim());
                if (seconds > 0L) {
                    return TimeUnit.SECONDS.toMillis(seconds);
                }
            } catch (NumberFormatException ignored) {
                // Keep fallback parsing.
            }
        }

        if (TextUtils.isEmpty(fallbackMessage)) {
            return -1L;
        }

        String lower = fallbackMessage.toLowerCase(Locale.US);
        int retryAfterIndex = lower.indexOf("retry after");
        if (retryAfterIndex < 0) {
            return -1L;
        }

        String suffix = lower.substring(retryAfterIndex);
        String digits = suffix.replaceAll("[^0-9]", "");
        if (TextUtils.isEmpty(digits)) {
            return -1L;
        }

        try {
            return TimeUnit.SECONDS.toMillis(Long.parseLong(digits));
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private static final class GeminiApiException extends Exception {
        final int httpCode;
        final String status;
        final long retryAfterMs;

        GeminiApiException(int httpCode, String status, String message, long retryAfterMs) {
            super(message);
            this.httpCode = httpCode;
            this.status = status;
            this.retryAfterMs = retryAfterMs;
        }
    }

    private boolean sleepQuietly(long delayMs) {
        try {
            Thread.sleep(Math.max(0L, delayMs));
            return true;
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /** Releases the worker thread; call from Activity.onDestroy(). */
    public void shutdown() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}

