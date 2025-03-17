package edu.cwu.catmap.core;

import com.google.android.gms.maps.model.LatLng;

public class Location {
    private final String name, address, mainEntranceCoordinate, wheelchairEntranceCoordinate, imagePath, description;

    public Location(String name, String address, String mainEntranceCoordinate, String wheelchairEntranceCoordinate, String imagePath, String description) {
        this.name = name;
        this.address = address;
        this.mainEntranceCoordinate = mainEntranceCoordinate;
        this.wheelchairEntranceCoordinate = wheelchairEntranceCoordinate;
        this.imagePath = imagePath;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getMainEntranceCoordinate() {
        return mainEntranceCoordinate;
    }

    public String getWheelchairEntranceCoordinate() {
        return wheelchairEntranceCoordinate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDescription() {
        return description;
    }

    public LatLng getMainEntranceCoordinateLatLng() {
        String[] latLngStringArray = mainEntranceCoordinate.split(",");
        return new LatLng(Double.parseDouble(latLngStringArray[0].trim()), Double.parseDouble(latLngStringArray[1].trim()));
    }
}
