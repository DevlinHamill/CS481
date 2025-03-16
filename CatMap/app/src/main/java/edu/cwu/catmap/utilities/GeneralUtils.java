package edu.cwu.catmap.utilities;
import android.content.Context;
import android.content.SharedPreferences;

import edu.cwu.catmap.R;

public class generalUtils {
    public static int getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String theme = prefs.getString("theme", "light"); // Default to light theme

        if (theme.equals("dark")) {
            return R.style.Theme_CatMap_Dark; // Use dark theme
        } else {
            return R.style.Theme_CatMap; // Use light theme
        }
    }
}
