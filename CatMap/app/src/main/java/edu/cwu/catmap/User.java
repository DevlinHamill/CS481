package edu.cwu.catmap;

import java.util.Map;

import edu.cwu.catmap.core.Schedule;
import edu.cwu.catmap.core.FavoriteLocations;
import edu.cwu.catmap.utilities.Constants;

public class User {
    private String name, email, encodedProfilePicture, colorBlindMode;
    private boolean leftHandedMode, wheelchairMode, darkMode;
    private int minutesBeforeEventToNotify;
    private Schedule schedule;
    private final FavoriteLocations favoriteLocations;

    private static User currentUser;

    public User(Map<String, Object> userData) {
        //TODO: implement this constructor to take the data returned from the firebase, this will be used to construct user from login
    }

    public User(String name, String email, String encodedProfilePicture) {
        this.name = name;
        this.email = email;
        this.encodedProfilePicture = encodedProfilePicture;
        minutesBeforeEventToNotify = Constants.VALUE_DEFAULT_MINUTES_BEFORE_EVENT_TO_NOTIFY;
        colorBlindMode = Constants.VALUE_NORMAL_VISION;
        leftHandedMode = false;
        wheelchairMode = false;
        darkMode = false;
        //schedule = new Schedule(); //TODO figure out when to create the schedule (needs to be after the user chooses the current quarter date range
        favoriteLocations = new FavoriteLocations();
    }

    public static void setCurrentUser(User currentUser) {
        User.currentUser = currentUser;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getEncodedProfilePicture() {
        return encodedProfilePicture;
    }

    public String isColorBlindMode() {
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

    public FavoriteLocations getFavoriteLocations() {
        return favoriteLocations;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setEncodedProfilePicture(String encodedProfilePicture) {
        this.encodedProfilePicture = encodedProfilePicture;
    }

    public void setColorBlindMode(String colorBlindMode) {
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
