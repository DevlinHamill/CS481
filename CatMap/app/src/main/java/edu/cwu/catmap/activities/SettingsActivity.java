package edu.cwu.catmap.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.google.android.material.materialswitch.MaterialSwitch;

import edu.cwu.catmap.R;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch switchDarkMode, switchHighContrast, switchAdaAccessible, switchNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply the correct theme before setting content view
        applyTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Initialize UI components
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchHighContrast = findViewById(R.id.switch_high_contrast_mode);
        switchAdaAccessible = findViewById(R.id.switch_ada_accessible);
        switchNotifications = findViewById(R.id.switch_notifications);

        // Load saved preferences
        loadSettings();

        // Assign event listeners
        switchDarkMode.setOnCheckedChangeListener(this::onSwitchToggled);
        switchHighContrast.setOnCheckedChangeListener(this::onSwitchToggled);
        switchAdaAccessible.setOnCheckedChangeListener(this::onSwitchToggled);
        switchNotifications.setOnCheckedChangeListener(this::onSwitchToggled);
    }

    /**
     * Applies the saved theme before setting the UI.
     */
    private void applyTheme() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Loads saved preferences and updates the switches accordingly.
     */
    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        switchDarkMode.setChecked(preferences.getBoolean("dark_mode", false));
        switchHighContrast.setChecked(preferences.getBoolean("high_contrast", false));
        switchAdaAccessible.setChecked(preferences.getBoolean("ada_accessible", false));
        switchNotifications.setChecked(preferences.getBoolean("notifications", true));

        // Get references
        TextView textSelectedAlert = findViewById(R.id.text_selected_alert);
        LinearLayout alertRow = findViewById(R.id.alert_option_container);

        boolean notificationsEnabled = preferences.getBoolean("notifications", true);
        String alertOption = preferences.getString("alert_option", "10 minutes before");

        if (notificationsEnabled) {
            textSelectedAlert.setText(alertOption);
            alertRow.setEnabled(true);
            alertRow.setAlpha(1f);
        } else {
            textSelectedAlert.setText("None");
            alertRow.setEnabled(false);
            alertRow.setAlpha(0.5f);
        }
    }

    /**
     * Saves preference when a switch is toggled and updates the UI dynamically.
     */
    private void onSwitchToggled(CompoundButton buttonView, boolean isChecked) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();

        if (buttonView.getId() == R.id.switch_dark_mode) {
            editor.putBoolean("dark_mode", isChecked);
            if (isChecked) {
                // Disable High Contrast Mode if Dark Mode is enabled
                switchHighContrast.setChecked(false);
                editor.putBoolean("high_contrast", false);
            }
            editor.apply();
            restartActivity(); // Restart activity to apply theme

        } else if (buttonView.getId() == R.id.switch_high_contrast_mode) {
            editor.putBoolean("high_contrast", isChecked);
            if (isChecked) {
                // Disable Dark Mode if High Contrast Mode is enabled
                switchDarkMode.setChecked(false);
                editor.putBoolean("dark_mode", false);
            }
            editor.apply();
            restartActivity(); // Restart activity to apply theme

        } else if (buttonView.getId() == R.id.switch_ada_accessible) {
            editor.putBoolean("ada_accessible", isChecked);

        } else if (buttonView.getId() == R.id.switch_notifications) {
            editor.putBoolean("notifications", isChecked);
            updateNotificationUI(isChecked);
        }

        editor.apply();
    }

    /**
     * Restarts the activity to apply theme changes.
     */
    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Updates UI based on notifications toggle.
     */
    private void updateNotificationUI(boolean isChecked) {
        TextView textSelectedAlert = findViewById(R.id.text_selected_alert);
        LinearLayout alertRow = findViewById(R.id.alert_option_container);

        if (isChecked) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String lastAlert = preferences.getString("alert_option", "10 minutes before");
            textSelectedAlert.setText(lastAlert);
            alertRow.setEnabled(true);
            alertRow.setAlpha(1f);
        } else {
            textSelectedAlert.setText("None");
            alertRow.setEnabled(false);
            alertRow.setAlpha(0.5f);
        }
    }
}
