package edu.cwu.catmap;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch switchDarkMode, switchHighContrast, switchLeftHand, switchAdaAccessible, switchNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        // Initialize UI components
        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchHighContrast = findViewById(R.id.switch_high_contrast_mode);
        switchLeftHand = findViewById(R.id.switch_left_hand);
        switchAdaAccessible = findViewById(R.id.switch_ada_accessible);
        switchNotifications = findViewById(R.id.switch_notifications);

        // Load saved preferences
        loadSettings();

        // Assign event listeners
        switchDarkMode.setOnCheckedChangeListener(this::onSwitchToggled);
        switchHighContrast.setOnCheckedChangeListener(this::onSwitchToggled);
        switchLeftHand.setOnCheckedChangeListener(this::onSwitchToggled);
        switchAdaAccessible.setOnCheckedChangeListener(this::onSwitchToggled);
        switchNotifications.setOnCheckedChangeListener(this::onSwitchToggled);
    }


    /**
     * Loads saved preferences and updates the switches accordingly.
     */
    private void loadSettings() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        switchDarkMode.setChecked(preferences.getBoolean("dark_mode", false));
        switchHighContrast.setChecked(preferences.getBoolean("high_contrast", false));
        switchLeftHand.setChecked(preferences.getBoolean("left_hand", false));
        switchAdaAccessible.setChecked(preferences.getBoolean("ada_accessible", false));
        switchNotifications.setChecked(preferences.getBoolean("notifications", true));

        // Get references
        TextView textSelectedAlert = findViewById(R.id.text_selected_alert);
        LinearLayout alertRow = findViewById(R.id.alert_option_container);

        boolean notificationsEnabled = preferences.getBoolean("notifications", true);
        String alertOption = preferences.getString("alert_option", "10 minutes before"); // Default

        if (notificationsEnabled) {
            textSelectedAlert.setText(alertOption);
            alertRow.setEnabled(true);
            alertRow.setAlpha(1f);
        } else {
            textSelectedAlert.setText("None");
            alertRow.setEnabled(false);
            alertRow.setAlpha(0.5f);
        }

        // REMOVE THIS LISTENER (since it's already set in onCreate)
        // switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> toggleNotifications(null));
    }


    /**
     * Saves preference when a switch is toggled.
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
        } else if (buttonView.getId() == R.id.switch_high_contrast_mode) {
            editor.putBoolean("high_contrast", isChecked);
            if (isChecked) {
                // Disable Dark Mode if High Contrast Mode is enabled
                switchDarkMode.setChecked(false);
                editor.putBoolean("dark_mode", false);
            }
        } else if (buttonView.getId() == R.id.switch_left_hand) {
            editor.putBoolean("left_hand", isChecked);
        } else if (buttonView.getId() == R.id.switch_ada_accessible) {
            editor.putBoolean("ada_accessible", isChecked);
        } else if (buttonView.getId() == R.id.switch_notifications) {
            editor.putBoolean("notifications", isChecked);

            // 🚀 Update UI dynamically
            TextView textSelectedAlert = findViewById(R.id.text_selected_alert);
            LinearLayout alertRow = findViewById(R.id.alert_option_container);

            if (isChecked) {
                // Restore last alert selection
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String lastAlert = preferences.getString("alert_option", "10 minutes before");
                textSelectedAlert.setText(lastAlert);
                alertRow.setEnabled(true);
                alertRow.setAlpha(1f);
            } else {
                // Disable alert row
                textSelectedAlert.setText("None");
                alertRow.setEnabled(false);
                alertRow.setAlpha(0.5f);
            }
        }

        editor.apply(); // Save the preference
    }


    public void toggleDarkMode(View view) {
        MaterialSwitch switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchDarkMode.toggle(); // Toggle the switch manually
    }

    //need to make so if high contrast mode is on, then dark mode is off, and if dark mode is turned on then high contrast mode is turned off.
    public void toggleHighContrast(View view) {
        MaterialSwitch switchHighContrast = findViewById(R.id.switch_high_contrast_mode);
        switchHighContrast.toggle();

    }

    public void toggleLeftHandMode(View view) {
        MaterialSwitch switchLeftHand = findViewById(R.id.switch_left_hand);
        switchLeftHand.toggle();
    }

    public void toggleAdaAccessible(View view) {
        MaterialSwitch switchAda = findViewById(R.id.switch_ada_accessible);
        switchAda.toggle();
    }



    public void toggleNotifications(View view) {
        MaterialSwitch switchNotifications = findViewById(R.id.switch_notifications);
        switchNotifications.toggle();

        TextView textSelectedAlert = findViewById(R.id.text_selected_alert);
        LinearLayout alertRow = findViewById(R.id.alert_option_container);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();

        boolean isChecked = switchNotifications.isChecked();
        editor.putBoolean("notifications", isChecked);

        if (isChecked) {
            // Restore last selected alert option or default to "10 minutes before"
            String lastAlert = preferences.getString("alert_option", "10 minutes before");
            textSelectedAlert.setText(lastAlert);
            alertRow.setEnabled(true);
            alertRow.setAlpha(1f);
        } else {
            // When notifications are OFF, set alert to "None" and disable the row
            textSelectedAlert.setText("None");
            alertRow.setEnabled(false);
            alertRow.setAlpha(0.5f);
        }

        editor.apply();
    }


    public void showAlertDialog(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Prevent dialog from opening if notifications are disabled
        if (!preferences.getBoolean("notifications", true)) {
            return;
        }

        // Define alert options
        final String[] alertOptions = {"At time of event", "5 minutes before", "10 minutes before", "15 minutes before"};

        // Get reference to alert text view
        TextView textSelectedAlert = findViewById(R.id.text_selected_alert);

        // Load the currently selected alert option
        String currentSelection = preferences.getString("alert_option", "10 minutes before");

        // Find the index of the current selection
        int checkedItem = 2; // Default to "10 minutes before"
        for (int i = 0; i < alertOptions.length; i++) {
            if (alertOptions[i].equals(currentSelection)) {
                checkedItem = i;
                break;
            }
        }

        // Create and show the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Alert Time")
                .setSingleChoiceItems(alertOptions, checkedItem, (dialog, which) -> {
                    // Update text view with the selected option
                    textSelectedAlert.setText(alertOptions[which]);

                    // Save selection in SharedPreferences
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("alert_option", alertOptions[which]);
                    editor.apply();

                    // Dismiss the dialog
                    dialog.dismiss();
                });

        builder.create().show();
    }


}