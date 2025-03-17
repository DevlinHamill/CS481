package edu.cwu.catmap.utilities;

public class Constants {

    //user details constants
    public static final String KEY_NAME = "name";
    public static final String KEY_ENCODED_PROFILE_PICTURE = "encoded_profile_picture";
    public static final String KEY_ACCOUNT_TYPE = "account_type";
    public static final String VALUE_ACCOUNT_GOOGLE = "google";
    public static final String VALUE_ACCOUNT_EMAIL = "email";


    //user preferences constants
    public static final String KEY_PREF_LEFT_HANDED_MODE = "left_handed_mode";
    public static final String KEY_PREF_WHEEL_CHAIR_MODE = "wheel_chair_mode";
    public static final String KEY_PREF_DARK_MODE = "dark_mode";

    //colorblind mode constants
    public static final String KEY_PREF_COLOR_BLIND_MODE = "color_blind_mode";
    public static final String VALUE_NORMAL_VISION = "normal_vision";
    public static final String VALUE_DEUTERANOPIA = "deuteranopia";
    public static final String VALUE_PROTANOPIA = "protanopia";
    public static final String VALUE_TRITANOPIA = "tritanopia";

    //notification constants
    public static final String KEY_ENABLE_NOTIFICATIONS = "enable_notifications";
    public static final String KEY_MINUTES_BEFORE_EVENT_TO_NOTIFY = "minutes_before_event_to_notify";
    public static final int VALUE_DEFAULT_MINUTES_BEFORE_EVENT_TO_NOTIFY = 10;

    //collection names constants
    public static final String KEY_USER_COLLECTION = "user_collection";
    public static final String KEY_EVENTS_SUBCOLLECTION = "Events";
    public static final String KEY_EVENT_GROUP_COLLECTION = "event_group_collection";
    public static final String KEY_FAVORITE_LOCATIONS_SUBCOLLECTION = "favorite_locations_collection";
    public static final String KEY_SCHEDULE_COLLECTION = "schedule_collection";

    //intent extra keys
    public static final String KEY_LOCATION_NAME = "location_name";
    public static final String KEY_COLOR = "color";

    //location json filename
    public static final String KEY_LOCATION_JSON_NAME = "json/locations.json";

    //events constants
    public static final String KEY_EVENT_TYPE = "Event_Type";
    public static final String KEY_EVENT_TITLE = "Event_Title";
    public static final String KEY_EVENT_COLOR = "Color_Preference";
    public static final String VALUE_EVENT_TYPE_EVENT = "Event";
    public static final String VALUE_EVENT_TYPE_CLASS = "Class";

    //new event keys
    public static final String KEY_NEW_EVENT_TYPE = "header";
    public static final String KEY_NEW_EVENT_SELECTED_DATE = "SELECTED_DATE";
    public static final String KEY_NEW_EVENT_IS_EXISTING_CLASS = "is_existing_class";
    public static final String KEY_NEW_EVENT_CLASS_NAME = "class_name";
    public static final String KEY_NEW_EVENT_CLASS_COLOR = "class_color";



}
