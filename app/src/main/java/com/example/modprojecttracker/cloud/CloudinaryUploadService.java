package com.example.modprojecttracker.cloud;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.example.modprojecttracker.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CloudinaryUploadService {

    public interface UploadCallback {
        void onSuccess(String imageUrl);

        void onError(String errorMessage);
    }

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public boolean isConfigured() {
        return !BuildConfig.CLOUDINARY_CLOUD_NAME.isEmpty() && !BuildConfig.CLOUDINARY_UPLOAD_PRESET.isEmpty();
    }

    public void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        if (!isConfigured()) {
            callback.onError("Cloudinary is not configured. Set CLOUDINARY_CLOUD_NAME and CLOUDINARY_UPLOAD_PRESET.");
            return;
        }
        executor.execute(() -> {
            try {
                byte[] bytes = readBytes(context, imageUri);
                RequestBody fileBody = RequestBody.create(bytes, MediaType.parse("image/*"));
                MultipartBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                        .addFormDataPart("file", "project_image.jpg", fileBody)
                        .build();

                String endpoint = "https://api.cloudinary.com/v1_1/" + BuildConfig.CLOUDINARY_CLOUD_NAME + "/image/upload";
                Request request = new Request.Builder()
                        .url(endpoint)
                        .post(requestBody)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    String body = response.body() != null ? response.body().string() : "";
                    if (!response.isSuccessful()) {
                        postError(callback, "Upload failed: " + response.code());
                        return;
                    }
                    JSONObject json = new JSONObject(body);
                    String secureUrl = json.optString("secure_url", "");
                    if (secureUrl.isEmpty()) {
                        postError(callback, "Upload succeeded but secure_url is empty");
                        return;
                    }
                    postSuccess(callback, secureUrl);
                }
            } catch (Exception ex) {
                postError(callback, ex.getMessage() != null ? ex.getMessage() : "Upload error");
            }
        });
    }

    private byte[] readBytes(Context context, Uri uri) throws IOException {
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                throw new IOException("Cannot open selected image");
            }
            byte[] buffer = new byte[8192];
            int read;
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        }
    }

    private void postSuccess(UploadCallback callback, String url) {
        mainHandler.post(() -> callback.onSuccess(url));
    }

    private void postError(UploadCallback callback, String message) {
        mainHandler.post(() -> callback.onError(message));
    }
}

