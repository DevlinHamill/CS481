package edu.cwu.catmap;

public class User {
    private String username, email, encodedProfilePicture;
    private boolean colorBlindMode, leftHandedMode, wheelchairMode, darkMode;
    private int minutesBeforeEventToNotify;
    private final Schedule schedule;
    private final FavoriteLocationsManager favoriteLocations;

    private static User currentUser;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        encodedProfilePicture = "";
        minutesBeforeEventToNotify = 10;
        colorBlindMode = false;
        leftHandedMode = false;
        wheelchairMode = false;
        darkMode = false;
        schedule = new Schedule(); //TODO figure out when to create the schedule (needs to be after the user chooses the current quarter date range
        favoriteLocations = new FavoriteLocationsManager();
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getEncodedProfilePicture() {
        return encodedProfilePicture;
    }

    public boolean isColorBlindMode() {
        return colorBlindMode;
    }

    public boolean isLeftHandedMode() {
        return leftHandedMode;
    }

    public boolean isWheelchairMode() {
        return wheelchairMode;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public int getMinutesBeforeEventToNotify() {
        return minutesBeforeEventToNotify;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public FavoriteLocationsManager getFavoriteLocations() {
        return favoriteLocations;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEncodedProfilePicture(String encodedProfilePicture) {
        this.encodedProfilePicture = encodedProfilePicture;
    }

    public void setColorBlindMode(boolean colorBlindMode) {
        this.colorBlindMode = colorBlindMode;
    }

    public void setLeftHandedMode(boolean leftHandedMode) {
        this.leftHandedMode = leftHandedMode;
    }

    public void setWheelchairMode(boolean wheelchairMode) {
        this.wheelchairMode = wheelchairMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public void setMinutesBeforeEventToNotify(int minutesBeforeEventToNotify) {
        this.minutesBeforeEventToNotify = minutesBeforeEventToNotify;
    }
}
