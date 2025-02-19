package edu.cwu.catmap;

public class Location {
    private final String name, address, coordinate, encodedImage, description;

    public Location(String name, String address, String coordinate, String encodedImage, String description) {
        this.name = name;
        this.address = address;
        this.coordinate = coordinate;
        this.encodedImage = encodedImage;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public String getDescription() {
        return description;
    }
}
