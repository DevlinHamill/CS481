package edu.cwu.catmap.utilities;

public class Constants {

    //user details constants
    public static final String KEY_NAME = "name";
    public static final String KEY_ENCODED_PROFILE_PICTURE = "encoded_profile_picture";

    //user preferences constants
    public static final String KEY_PREF_LEFT_HANDED_MODE = "left_handed_mode";
    public static final String KEY_PREF_WHEEL_CHAIR_MODE = "wheel_chair_mode";
    public static final String KEY_PREF_DARK_MODE = "dark_mode";

    //colorblind mode constants
    public static final String KEY_PREF_COLOR_BLIND_MODE = "color_blind_mode";
    public static final String VALUE_NORMAL_VISION = "normal_vision";

    //notification constants
    public static final String KEY_ENABLE_NOTIFICATIONS = "enable_notifications";
    public static final String KEY_MINUTES_BEFORE_EVENT_TO_NOTIFY = "minutes_before_event_to_notify";
    public static final int VALUE_DEFAULT_MINUTES_BEFORE_EVENT_TO_NOTIFY = 10;

    //collection names constants
    public static final String KEY_USER_COLLECTION = "user_collection"; //TODO: CHANGE TO CORRECT COLLECTION NAME IN DATABASE, THEN HERE
    public static final String KEY_EVENT_COLLECTION = "event_collection";
    public static final String KEY_EVENT_GROUP_COLLECTION = "event_group_collection";
    public static final String KEY_FAVORITE_LOCATIONS_COLLECTION = "favorite_locations_collection";
    public static final String KEY_SCHEDULE_COLLECTION = "schedule_collection";

    //intent extra/hashmap keys
    public static final String KEY_LOCATION_NAME = "location_name";
    public static final String KEY_COLOR = "color";

    //location json filename
    public static final String KEY_LOCATION_JSON_NAME = "json/locations.json";



}
