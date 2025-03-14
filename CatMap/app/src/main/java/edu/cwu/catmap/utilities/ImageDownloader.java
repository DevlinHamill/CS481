package edu.cwu.catmap.utilities;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.cwu.catmap.manager.UserManager;

public class ImageDownloader {
    private final String userId;
    private final UserManager userManager;
    private final Uri imageUri;
    private final ExecutorService executorService;

    public ImageDownloader(String userId, UserManager userManager, Uri imageUri) {
        this.userId = userId;
        this.userManager = userManager;
        this.imageUri = imageUri;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void downloadAndProcessImage() {
        executorService.execute(() -> {
            Bitmap result = downloadImageFromUrl(imageUri.toString());
            if (result != null) {
                String encodedProfilePicture = encodeImage(result);
                userManager.updateGooglePhoto(userId, encodedProfilePicture, task -> {
                    if (task.isSuccessful()) {
                        // successful pfp sync, should notify
                    } else {
                        // failed pfp sync, should notify
                    }
                });
            }
            executorService.shutdown();
        });
    }

    private Bitmap downloadImageFromUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();

            InputStream input = connection.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }
}
