package edu.cwu.catmap.activities;

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

import java.io.IOException;
import java.io.InputStream;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.Location;
import edu.cwu.catmap.databinding.ActivityLocationInformationBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.utilities.Constants;

public class LocationInformationActivity extends AppCompatActivity {
    ActivityLocationInformationBinding binding;

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

        binding = ActivityLocationInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String locationName = getIntent().getStringExtra(Constants.KEY_LOCATION_NAME);

        Location location = LocationsManager.getInstance(this).getLocation(locationName);

        binding.locationName.setText(location.getName());
        binding.locationAddress.setText(location.getAddress());
        binding.locationCoord.setText(location.getMainEntranceCoordinate());
        binding.locationInfo.setText(location.getDescription());

        AssetManager assetManager = this.getAssets();
        String filePath = "images/" + location.getImagePath();

        try {
            //open the image as an input stream, convert to bitmap, set location image to bitmap
            InputStream inputStream = assetManager.open(filePath);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            binding.locationImage.setImageBitmap(bitmap);
        } catch (IOException e) {
            Log.e("LocationInformationActivity", "Failed to locate image at " + filePath);
        }
    }
}