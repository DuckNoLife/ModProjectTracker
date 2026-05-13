package com.example.modprojecttracker.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class ProjectImageLoader {

    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private ProjectImageLoader() {
    }

    public static void load(String imageUrl, ImageView imageView, View placeholderView) {
        imageView.setImageDrawable(null);
        imageView.setVisibility(View.GONE);
        placeholderView.setVisibility(View.VISIBLE);

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }

        EXECUTOR.execute(() -> {
            Bitmap bitmap;
            try {
                bitmap = downloadBitmap(imageUrl);
            } catch (Exception ignored) {
                bitmap = null;
            }

            Bitmap finalBitmap = bitmap;
            MAIN_HANDLER.post(() -> {
                if (finalBitmap != null) {
                    imageView.setImageBitmap(finalBitmap);
                    imageView.setVisibility(View.VISIBLE);
                    placeholderView.setVisibility(View.GONE);
                } else {
                    imageView.setVisibility(View.GONE);
                    placeholderView.setVisibility(View.VISIBLE);
                }
            });
        });
    }

    private static Bitmap downloadBitmap(String imageUrl) throws IOException {
        Request request = new Request.Builder().url(imageUrl).build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return null;
            }
            try (InputStream stream = response.body().byteStream()) {
                return BitmapFactory.decodeStream(stream);
            }
        }
    }
}


