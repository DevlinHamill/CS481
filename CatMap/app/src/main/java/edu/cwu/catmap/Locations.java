package edu.cwu.catmap;

import java.util.ArrayList;
import java.util.HashMap;

public class Locations {
    private HashMap<String, Location> locationsMap;

    private void objectifyLocations() {
        //read location data from file and inflate into location objects, run this in constructor
        //inflate might be a better name for this process :P
    }

    public Locations() {
        objectifyLocations();
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
