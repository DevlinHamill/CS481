package edu.cwu.catmap.utilities;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import edu.cwu.catmap.R;

public class GeneralUtils {
    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.KEY_SHARED_PREFRENCES_NAME, Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean(Constants.KEY_SHARED_PREFRENCES_DARK_MODE, false); // Default to light theme

        if (isDarkMode) {
            return R.style.Theme_CatMap_Dark; // Use dark theme
        } else {
            return R.style.Theme_CatMap; // Use light theme
        }
    }
}
