package edu.cwu.catmap.activities;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import edu.cwu.catmap.R;
import edu.cwu.catmap.adapters.FavoriteLocationsAdapter;
import edu.cwu.catmap.adapters.FavoriteLocationsListItem;
import edu.cwu.catmap.databinding.ActivityLocationsBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.utilities.Constants;
import edu.cwu.catmap.utilities.FirestoreTraceback;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

public class LocationsActivity extends AppCompatActivity {
    private ActivityLocationsBinding binding;
    private FavoriteLocationsAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdge.enable(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ArrayList<FavoriteLocationsListItem> locationsList = new ArrayList<>();
        populateLocations(locationsList);

        adapter = new FavoriteLocationsAdapter(locationsList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.searchBar.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                adapter.getFilter().filter(query);
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<FavoriteLocationsListItem> locationsList = new ArrayList<>();
        populateLocations(locationsList);

        updateAdapterInfo(locationsList);
    }

    private void updateAdapterInfo(List<FavoriteLocationsListItem> locationsList) {
        adapter.updateData(locationsList);
    }

    private void populateLocations(List<FavoriteLocationsListItem> locationsList) {
        locationsList.add(new FavoriteLocationsListItem.SectionHeader("Favorite Locations"));

        Context context = this;
        FirestoreUtility.getInstance().getFavoriteLocations(new FirestoreTraceback() {
            @Override
            public void success(String message) {
                Log.i("Locations Activity", "Error case of firebase util calling success with no data, message: " + message);
                ToastHelper.showToast(context, message);
                locationsList.add(new FavoriteLocationsListItem.Location(Constants.VALUE_NO_LOCATIONS_FOUND));
                addAllLocationsToLocationsList(locationsList, context);
            }

            @Override
            public void success(String message, Object data) {
                List<HashMap<String, String>> favoriteLocationsList = (ArrayList<HashMap<String, String>>) data;

                for (HashMap<String, String> favoriteLocationMap : favoriteLocationsList) {
                    Log.i("Locations Activity", "location_name: " + favoriteLocationMap.get(Constants.KEY_LOCATION_NAME) +
                            " color: " + favoriteLocationMap.get(Constants.KEY_COLOR) +
                            " color int: " + Integer.parseInt(Objects.requireNonNull(favoriteLocationMap.get(Constants.KEY_COLOR))));

                    locationsList.add(new FavoriteLocationsListItem.FavoriteLocation(favoriteLocationMap.get(Constants.KEY_LOCATION_NAME), Integer.parseInt(favoriteLocationMap.get(Constants.KEY_COLOR))));
                }

                addAllLocationsToLocationsList(locationsList, context);
            }

            @Override
            public void failure(String message) {
                ToastHelper.showToast(context, "Failed to retrieve favorite location data");
            }
        });
    }

    private void addAllLocationsToLocationsList(List<FavoriteLocationsListItem> locationsList, Context context) {
        locationsList.add(new FavoriteLocationsListItem.SectionHeader("All Locations"));
        LocationsManager locationsManager = LocationsManager.getInstance(context);

        ArrayList<String> sortedLocations = new ArrayList<>(locationsManager.getLocationNames());
        Collections.sort(sortedLocations);

        for (String location : sortedLocations) {
            locationsList.add(new FavoriteLocationsListItem.Location(location));
        }

        // Once Firestore has finished loading, update RecyclerView
        runOnUiThread(() -> updateAdapterInfo(locationsList));
    }
}