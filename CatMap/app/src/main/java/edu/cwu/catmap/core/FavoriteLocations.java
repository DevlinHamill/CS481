package edu.cwu.catmap.core;

import java.util.HashMap;

public class FavoriteLocations {
    private HashMap<String, Location> favoriteLocationsMap;

    public FavoriteLocations() {
        favoriteLocationsMap = new HashMap<>();
    }

    public void addLocation(Location location) {
        favoriteLocationsMap.put(location.getName(), location);
    }

    public void removeLocation(Location location) {
        favoriteLocationsMap.remove(location.getName());
    }

    public boolean isFavorite(Location location) {
        return favoriteLocationsMap.containsKey(location.getName());
    }

    public HashMap<String, Location> getFavoriteLocations() {
        return favoriteLocationsMap;
    }

}
