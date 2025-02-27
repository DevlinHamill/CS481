package edu.cwu.catmap.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.cwu.catmap.R;
import edu.cwu.catmap.adapters.FavoriteLocationsAdapter;
import edu.cwu.catmap.core.FavoriteLocationsListItem;
import edu.cwu.catmap.databinding.ActivityLocationsBinding;

public class LocationsActivity extends AppCompatActivity {
    private ActivityLocationsBinding binding;
    private FavoriteLocationsAdapter adapter;
    private List<FavoriteLocationsListItem> locationsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_locations);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding = ActivityLocationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        locationsList = new ArrayList<>();
        populateLocations();

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

    private void populateLocations() {
        locationsList.add(new FavoriteLocationsListItem.SectionHeader("Favorite Locations"));
        locationsList.add(new FavoriteLocationsListItem.FavoriteLocation("Samuelson Hall", Color.GREEN));
        locationsList.add(new FavoriteLocationsListItem.FavoriteLocation("Student Union Recreation Center", Color.RED));
        locationsList.add(new FavoriteLocationsListItem.FavoriteLocation("Brooks Library", Color.YELLOW));
        locationsList.add(new FavoriteLocationsListItem.SectionHeader("All Locations"));
        locationsList.add(new FavoriteLocationsListItem.Location("Barge Hall"));
        locationsList.add(new FavoriteLocationsListItem.Location("Black Hall"));
        locationsList.add(new FavoriteLocationsListItem.Location("Bouillon Hall"));
        locationsList.add(new FavoriteLocationsListItem.Location("Dean Hall"));
        locationsList.add(new FavoriteLocationsListItem.Location("Discovery Hall"));
    }
}