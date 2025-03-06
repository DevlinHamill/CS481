package edu.cwu.catmap.managers;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

import edu.cwu.catmap.core.Location;

public class LocationsManager {
    private HashMap<String, Location> locationsMap;

    public LocationsManager(Context context, String fileName) {
        locationsMap = new HashMap<>();
        inflateLocations(context, fileName);
    }

    /**
     * Given a specific filename, return the json stored at that location
     * as a string so it can be used by a JsonParser
     * @param context current app context
     * @param fileName name of locations json
     * @return
     */
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
            Log.e("LocationManager", "Cannot find or open json file");
        }

        return returnString;
    }

    private void inflateLocations(Context context, String fileName) {
        String jsonString = loadJsonFromAssets(context, fileName);

        if(jsonString == null) {
            Log.e("LocationManager", "jsonString is null");
            return;
        }

        Gson gson = new Gson();
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();


    }

    public HashMap<String, Location> getLocations() {
        return new HashMap<>(locationsMap);
    }

    public HashMap<String, Location> getLocationsMinus(HashMap<String, Location> locations) {
        HashMap<String, Location> difference = getLocations();
        difference.keySet().removeAll(locations.keySet());
        return difference;
    }

    /**
     * Get a Hashmap of all Locations minus the Locations provided in the input ArrayList of Loccations
     * @param locations ArrayList of Locations to remove from all Locations
     * @return HashMap of all Locations minus input ArrayList of Locations
     */
    public HashMap<String, Location> getLocationsMinus(ArrayList<Location> locations) {
        ArrayList<Location> remainingLocations = new ArrayList<Location>(getLocations().values());
        remainingLocations.removeAll(locations);
        return mappifyLocationsArrayList(remainingLocations);
    }

    /**
     * Turn an ArrayList of Locations into a HashMap of Locations with Location name as key
     * @param locations ArrayList of Locations
     * @return HashMap of Locations with Location name as key
     */
    public HashMap<String, Location> mappifyLocationsArrayList(ArrayList<Location> locations) {
        HashMap<String, Location> newLocationMap = new HashMap<>();

        for(Location location : locations) {
            newLocationMap.put(location.getName(), location);
        }

        return newLocationMap;
    }
}
