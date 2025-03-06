package edu.cwu.catmap.managers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import edu.cwu.catmap.core.Location;
import edu.cwu.catmap.utilities.Constants;

public class LocationsManager {
    private static LocationsManager instance;
    private final HashMap<String, Location> locationsMap;

    private LocationsManager(Context context) {
        locationsMap = new HashMap<>();
        inflateLocations(context, Constants.KEY_LOCATION_JSON_NAME);
    }

    public static synchronized LocationsManager getInstance(Context context) {
        if (instance == null) {
            instance = new LocationsManager(context.getApplicationContext());
        }
        return instance;
    }

    private String loadJsonFromAssets(Context context, String fileName) {
        String returnString = null;
        try {
            AssetManager assetManager = context.getAssets();
            InputStream inputStream = assetManager.open(fileName);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            returnString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e("LocationsManager", "Cannot find or open json file");
        }
        return returnString;
    }

    private void inflateLocations(Context context, String fileName) {
        String jsonString = loadJsonFromAssets(context, fileName);
        if (jsonString == null) {
            Log.e("LocationsManager", "jsonString is null");
            return;
        }

        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        Type listType = new TypeToken<List<Location>>() {}.getType();
        List<Location> locations = gson.fromJson(jsonObject.get("locations"), listType);

        for (Location location : locations) {
            locationsMap.put(location.getName(), location);
        }
    }

    public HashMap<String, Location> getLocations() {
        return new HashMap<>(locationsMap);
    }

    public List<String> getLocationNames() {
        List locationNamesList = new ArrayList<>(getLocations().keySet());
        Collections.sort(locationNamesList);

        return locationNamesList;
    }

    public Location getLocation(String locationName) {
        return getLocations().get(locationName);
    }

    public HashMap<String, Location> getLocationsMinus(HashMap<String, Location> locations) {
        HashMap<String, Location> difference = getLocations();
        difference.keySet().removeAll(locations.keySet());
        return difference;
    }

    public HashMap<String, Location> getLocationsMinus(ArrayList<Location> locations) {
        ArrayList<Location> remainingLocations = new ArrayList<>(getLocations().values());
        remainingLocations.removeAll(locations);
        return mappifyLocationsArrayList(remainingLocations);
    }

    public HashMap<String, Location> mappifyLocationsArrayList(ArrayList<Location> locations) {
        HashMap<String, Location> newLocationMap = new HashMap<>();
        for (Location location : locations) {
            newLocationMap.put(location.getName(), location);
        }
        return newLocationMap;
    }
}
