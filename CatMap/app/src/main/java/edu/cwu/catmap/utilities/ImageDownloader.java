package edu.cwu.catmap.utilities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import android.app.Activity;
import android.content.Context;
import android.app.Service;

import com.google.firebase.firestore.DocumentSnapshot;

import edu.cwu.catmap.activities.MainActivity;
import edu.cwu.catmap.manager.UserManager;

public class ImageDownloader extends AsyncTask<Uri, Void, Bitmap> {
    private String userId;
    private UserManager userManager;
    private Uri imageUri;

    public ImageDownloader(String userId, UserManager userManager, Uri imageUri) {
        this.userId = userId;
        this.userManager = userManager;
        this.imageUri = imageUri;
    }

    @Override
    protected Bitmap doInBackground(Uri... uris) {
        //Uri imageUri = uris[0];
        return downloadImageFromUrl(imageUri.toString());
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);
        userManager = UserManager.getInstance();
        // Handle the scaled image (result) here, like encoding it
        if (result != null) {
            String encodedProfilePicture = encodeImage(result);
            userManager.updateGooglePhoto(userId, encodedProfilePicture,
                    task -> {
                if (task.isSuccessful()) {
                    // successful pfp sync
                } else {
                    // failed pfp sync
                }
            });

        } else {
            // showToast("Failed to retrieve image.");
        }
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

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Bitmap scaleImage(Uri imageUri) {
        try {
            // Download the image
            InputStream inputStream = new URL(imageUri.toString()).openStream();

            // Decode the image size without loading the full bitmap into memory
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            // Calculate optimal inSampleSize
            int targetWidth = 150; // Target size
            int targetHeight = 150;
            options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);

            // Decode with inSampleSize to get a scaled-down version
            inputStream = new URL(imageUri.toString()).openStream();
            Bitmap scaledBitmap = BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            return scaledBitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream); // Compression for storage efficiency
        return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
    }

}