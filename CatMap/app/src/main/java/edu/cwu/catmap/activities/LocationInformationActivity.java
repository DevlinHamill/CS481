package edu.cwu.catmap.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.Location;
import edu.cwu.catmap.databinding.ActivityLocationInformationBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.utilities.ColorPickerHelper;
import edu.cwu.catmap.utilities.ColorPickerTraceback;
import edu.cwu.catmap.utilities.Constants;
import edu.cwu.catmap.utilities.FirestoreTraceback;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

import java.io.IOException;
import java.io.InputStream;

public class LocationInformationActivity extends AppCompatActivity {
    private static final String TAG = "LocationInfoActivity";

    private ActivityLocationInformationBinding binding;
    private Location location;
    private boolean isFavorite = false;
    private final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.i("Location info controller", "onCreate for Location Information Running!");

        // Set up view binding
        binding = ActivityLocationInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Retrieve location data
        String locationName = getIntent().getStringExtra(Constants.KEY_LOCATION_NAME);
        location = LocationsManager.getInstance(this).getLocation(locationName);

        // Populate UI with location details
        binding.locationName.setText(location.getName());
        binding.locationAddress.setText(location.getAddress());
        binding.locationCoord.setText(location.getMainEntranceCoordinate());
        binding.locationInfo.setText(location.getDescription());

        // Load image if applicable
        loadLocationImage();

        // Disable favorite button while checking Firestore
        disableFavoriteButton();
        checkIfFavorite();

        setListeners();
    }

    private void loadLocationImage() {
        Log.i("Location info controller", "attempt to load image");
        AssetManager assetManager = getAssets();
        String filePath = "images/" + location.getImagePath();
        try (InputStream inputStream = assetManager.open(filePath)) {
            Log.i("Location info controller", "attempt to load image");
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            binding.locationImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e(TAG, "Failed to locate image at " + filePath);
        }
    }

    private void checkIfFavorite() {
        Log.i("Location info controller", "checking if favorite, making database read");
        FirestoreUtility.getInstance().isFavoriteLocation(location.getName(), new FirestoreTraceback() {
            @Override
            public void success(String message) {

            }

            @Override
            public void success(String message, Object data) {
                Log.i("Location info controller", "read successful with message: " + message + " and data " + data);
                updateFavoriteState((boolean) data);
            }

            @Override
            public void failure(String message) {
                Log.i("Location info controller", "failed to read info from the database with message: " + message);
                showToastAndEnableButton(message);
            }
        });
    }

    private void setListeners() {
        binding.favoriteButton.setOnClickListener(v -> {
            Log.i("Location info controller", "favorite button click listener assigned");
            disableFavoriteButton();

            if (isFavorite) {
                removeFavorite();
                return;
            }

            addFavorite();
        });

        binding.navigateButton.setOnClickListener(v -> {
            Log.i("Location info controller", "attempting to start main activity with location: " + location.getName());
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.KEY_LOCATION_NAME, location.getName());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void addFavorite() {
        Log.i("Location info controller", "add to favorite locations, launch the color picker");
        ColorPickerHelper.getColor(this, new ColorPickerTraceback() {
            @Override
            public void selectColor(int color) {

            }

            @Override
            public void confirm(int color) {
                Log.i("Location info controller", "color confirmed, make database call");
                FirestoreUtility.getInstance().addFavoriteLocation(location.getName(), color, new FirestoreTraceback() {
                    @Override
                    public void success(String message) {
                        Log.i("Location info controller", "database operation was successful with message: " + message);
                        updateFavoriteState(true);
                    }

                    @Override
                    public void success(String message, Object data) {


                    }

                    @Override
                    public void failure(String message) {
                        Log.i("Location info controller", "failed to push data to database with message: " + message);
                        showToastAndEnableButton(message);
                    }
                });
            }

            @Override
            public void cancel() {
                Log.i("Location info controller", "color selection was canceled");
                enableFavoriteButton();
            }
        });
    }

    private void removeFavorite() {
        Log.i("Location info controller", "removing from favorite locations");
        FirestoreUtility.getInstance().removeFavoriteLocation(location.getName(), new FirestoreTraceback() {
            @Override
            public void success(String message) {
                Log.i("Location info controller", "success with message: " + message + " called");
                updateFavoriteState(false);
            }

            @Override
            public void success(String message, Object data) {

            }

            @Override
            public void failure(String message) {
                Log.i("Location info controller", "failure with message: " + message + " called");
                showToastAndEnableButton(message);
            }
        });
    }

    private void updateFavoriteState(boolean favorite) {
        Log.i("Location info controller", "update favorite button text and enabled");

        runOnUiThread(() -> {
            isFavorite = favorite;
            updateFavoriteButtonText();
            enableFavoriteButton();
        });
    }

    private void showToastAndEnableButton(String message) {
        Log.i("Location info controller", "showing toast (usually an error) and enabling favorite button");

        runOnUiThread(() -> {
            ToastHelper.showToast(context, message);
            enableFavoriteButton();
        });
    }

    private void updateFavoriteButtonText() {
        Log.i("Location info controller", "favorite button text updated");
        binding.favoriteButton.setText(isFavorite ? getString(R.string.remove_from_favorites) : getString(R.string.add_to_favorites));
    }

    private void enableFavoriteButton() {
        Log.i("Location info controller", "favorite button enabled");
        binding.favoriteButton.setEnabled(true);
    }

    private void disableFavoriteButton() {
        Log.i("Location info controller", "favorite button disabled");
        binding.favoriteButton.setEnabled(false);
    }
}
