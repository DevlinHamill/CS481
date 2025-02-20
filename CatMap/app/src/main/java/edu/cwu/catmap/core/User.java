package edu.cwu.catmap.core;

import java.util.Map;

import edu.cwu.catmap.managers.UserManager;
import edu.cwu.catmap.utilities.Constants;
import edu.cwu.catmap.utilities.FirestoreUtility;

public class User {
    private String name, email, encodedProfilePicture, colorBlindMode;
    private boolean leftHandedMode, wheelchairMode, darkMode, enableNotifications;
    private int minutesBeforeEventToNotify;
    private Schedule schedule;
    private FavoriteLocations favoriteLocations;

    private static User currentUser;

    /**
     * Constructor used to construct a user object from the map returned from the database
     * @param userData String : Object map containing user data
     */
    public User(Map<String, Object> userData) {
        //TODO: implement this constructor to take the data returned from the firebase, this will be used to construct user from login
        this.name = (String) userData.get(Constants.KEY_NAME);
        this.email = (String) userData.get(UserManager.getInstance().getCurrentFirebaseUser().getEmail());
        this.encodedProfilePicture = (String) userData.get(Constants.KEY_ENCODED_PROFILE_PICTURE);
        this.colorBlindMode = (String) userData.get(Constants.KEY_PREF_COLOR_BLIND_MODE);
        this.leftHandedMode = (boolean) userData.get(Constants.KEY_PREF_LEFT_HANDED_MODE);
        this.wheelchairMode = (boolean) userData.get(Constants.KEY_PREF_WHEEL_CHAIR_MODE);
        this.darkMode = (boolean) userData.get(Constants.KEY_PREF_DARK_MODE);
        this.enableNotifications = (boolean) userData.get(Constants.KEY_ENABLE_NOTIFICATIONS);
        this.minutesBeforeEventToNotify = (int) userData.get(Constants.KEY_MINUTES_BEFORE_EVENT_TO_NOTIFY);

        //TODO: figure out how to attach schedule (events and groups) and favorites to the user asynchronously
        //schedule = FirestoreUtility.get;
        //favoriteLocations = FirestoreUtility.get;
    }

    /**
     * New user constructor that will be used when a user signs up to the app. sets some things to default values
     * @param name users name
     * @param email users email address
     * @param encodedProfilePicture users encoded profile picture
     */
    public User(String name, String email, String encodedProfilePicture) {
        this.name = name;
        this.email = email;
        this.encodedProfilePicture = encodedProfilePicture;
        this.minutesBeforeEventToNotify = Constants.VALUE_DEFAULT_MINUTES_BEFORE_EVENT_TO_NOTIFY;
        this.colorBlindMode = Constants.VALUE_NORMAL_VISION;
        this.leftHandedMode = false;
        this.wheelchairMode = false;
        this.darkMode = false;
        this.enableNotifications = true;
        //schedule = new Schedule(); //TODO figure out when to create the schedule (needs to be after the user chooses the current quarter date range
        this.favoriteLocations = new FavoriteLocations();
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
