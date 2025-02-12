package edu.cwu.catmap;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String
            PREF_NAME = "UserSession",
            KEY_IS_LOGGED_IN = "isLoggedIn",
            KEY_USERNAME = "username";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void loginUser(String username) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }
}
