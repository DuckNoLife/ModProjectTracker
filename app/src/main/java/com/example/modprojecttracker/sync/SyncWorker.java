package com.example.modprojecttracker.sync;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SyncWorker extends Worker {

    private static final long SYNC_TIMEOUT_SECONDS = 30L;

    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean success = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>(null);

        SyncCoordinator.syncNow(getApplicationContext(), new SyncCoordinator.SyncCallback() {
            @Override
            public void onSuccess() {
                success.set(true);
                latch.countDown();
            }

            @Override
            public void onError(String message) {
                errorMessage.set(message);
                latch.countDown();
            }
        });

        boolean completed;
        try {
            completed = latch.await(SYNC_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Result.retry();
        }

        if (!completed) {
            return Result.retry();
        }

        if (success.get()) {
            return Result.success();
        }

        String message = errorMessage.get();
        if (isPermanentError(message)) {
            return Result.failure();
        }
        return Result.retry();
    }

    private boolean isPermanentError(String message) {
        if (message == null) {
            return false;
        }
        String normalized = message.toLowerCase(Locale.US);
        return normalized.contains("permission")
                || normalized.contains("unauthorized")
                || normalized.contains("auth");
    }
}

