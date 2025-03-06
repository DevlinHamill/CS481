package edu.cwu.catmap.core;

public class Location {
    private final String name, address, mainEntranceCoordinate, wheelchairEntranceCoordinate, encodedImage, description;

    public Location(String name, String address, String mainEntranceCoordinate, String wheelchairEntranceCoordinate, String encodedImage, String description) {
        this.name = name;
        this.address = address;
        this.mainEntranceCoordinate = mainEntranceCoordinate;
        this.wheelchairEntranceCoordinate = wheelchairEntranceCoordinate;
        this.encodedImage = encodedImage;
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

    public String getEncodedImage() {
        return encodedImage;
    }

    public String getDescription() {
        return description;
    }
}
