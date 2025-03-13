package edu.cwu.catmap.adapters;

public class FavoriteLocationsListItem {
    public static class SectionHeader extends FavoriteLocationsListItem {
        private final String title;

        public SectionHeader(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }

    public static class FavoriteLocation extends FavoriteLocationsListItem {
        private final String name;
        private final int color; // Stores the color of the location

        public FavoriteLocation(String name, int color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public int getColor() {
            return color;
        }
    }

    public static class Location extends FavoriteLocationsListItem {
        private final String name;

        public Location(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
