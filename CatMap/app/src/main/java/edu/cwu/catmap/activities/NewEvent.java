package edu.cwu.catmap.activities;

import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.cwu.catmap.databinding.ActivityNewEventBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

/**
 * @author BT&T
 * CS 460
 */
public class NewEvent extends AppCompatActivity {
    private @NonNull ActivityNewEventBinding binding;
    private String date, Title, time, RoomNum, colorPreference, Building, Event_Type, EndDate;
    private int[] repeatingevents = new int[7];
    private Context context;
    private boolean repeatingCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        context = this;
        repeatingCondition = false;

        // Get event details from intent
        Event_Type = getIntent().getStringExtra("header");
        date = getIntent().getStringExtra("SELECTED_DATE");
        binding.EndResult.setText(date);
        addTitle(Event_Type);
        setListeners();
        setupBuildingSearch(binding.BuildingSearch);
    }

    private void setListeners() {
        binding.EndResult.setOnClickListener(v->
                showDatePicker()
        );

        binding.btnSun.setOnClickListener(v->
                setselecteddays(binding.btnSun)
        );

        binding.btnMon.setOnClickListener(v->
                setselecteddays(binding.btnMon)
        );

        binding.btnTue.setOnClickListener(v->
                setselecteddays(binding.btnTue)
        );

        binding.btnWed.setOnClickListener(v->
                setselecteddays(binding.btnWed)
        );

        binding.btnThu.setOnClickListener(v->
                setselecteddays(binding.btnThu)
        );

        binding.btnFri.setOnClickListener(v->
                setselecteddays(binding.btnFri)
        );

        binding.btnSat.setOnClickListener(v->
                setselecteddays(binding.btnSat)
        );
        binding.layoutHeader.setNavigationOnClickListener(v -> onBackPressed());

        binding.EndResult.setOnClickListener(v -> showDatePicker());
        binding.EventTime.setOnClickListener(v -> showTimePicker());

        binding.RepeatEventSelector.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.repeatOptionsContainer.setVisibility(isChecked ? VISIBLE : View.GONE);
        });

        binding.colorPickerButton.setOnClickListener(view -> showColorPicker());

        binding.confirmEventButton.setOnClickListener(v -> {
            if (validateEventDetails()) {
                saveEventDetails();
                Intent intent = new Intent(getApplicationContext(), SchedualerGUI.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setselecteddays( com.google.android.material.button.MaterialButton button){
        if(!button.isSelected()) {
            button.setSelected(true);
        }else{
            button.setSelected(false);
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String formattedDate = String.format("%02d/%02d/%d", month + 1, dayOfMonth, year);
                    binding.EndResult.setText(formattedDate);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                    int hour12 = (hourOfDay == 0) ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);
                    String formattedTime = String.format("%d:%02d %s", hour12, minute, amPm);
                    binding.EventTime.setText(formattedTime);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    private void showColorPicker() {
        Drawable background = binding.createMeetingLayout.getBackground();

        int backgroundColorInt = Color.WHITE;

        if (background instanceof ColorDrawable) {
            backgroundColorInt = ((ColorDrawable) background).getColor();
        }
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose a color")
                .initialColor(backgroundColorInt)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .showAlphaSlider(false)
                .setPositiveButton("Confirm", (dialog, lastSelectedColor, allColors) -> {
                    binding.colorPickerButton.setBackgroundColor(lastSelectedColor);
                    colorPreference = String.valueOf(lastSelectedColor);
                })
                .setNegativeButton("Cancel", (dialog, which) -> showToast("Color selection canceled"))
                .build()
                .show();
    }

    private void setupBuildingSearch(AutoCompleteTextView buildingSearch) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                LocationsManager.getInstance(this).getLocationNames().toArray(new String[0]));

        buildingSearch.setAdapter(adapter);
        buildingSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBuilding = adapter.getItem(position);
            buildingSearch.setText(selectedBuilding, false);
        });
    }

    private void saveEventDetails() {
        Title = binding.eventTitle.getText().toString();
        RoomNum = binding.EventRoom.getText().toString();
        Building = binding.BuildingSearch.getText().toString();
        EndDate = binding.EndResult.getText().toString();
        time = binding.EventTime.getText().toString();

        if (binding.RepeatEventSelector.isChecked()) {
            repeatingCondition = true;
            repeatingevents = new int[]{binding.btnSun.isSelected() ? 1 : 0,
                    binding.btnMon.isSelected() ? 1 : 0,
                    binding.btnTue.isSelected() ? 1 : 0,
                    binding.btnWed.isSelected() ? 1 : 0,
                    binding.btnThu.isSelected() ? 1 : 0,
                    binding.btnFri.isSelected() ? 1 : 0,
                    binding.btnSat.isSelected() ? 1 : 0};
        }

        addMeetingToFirebase();
    }

    private void addMeetingToFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        HashMap<String, String> meetings = new HashMap<>();

        meetings.put("Event_Title", Title);
        meetings.put("Event_Type", Event_Type);
        meetings.put("Building_Name", Building);
        meetings.put("Room_Number", RoomNum);
        meetings.put("Event_Date", date);
        meetings.put("Event_Time", time);
        meetings.put("Color_Preference", colorPreference);
        meetings.put("Repeated_Events", Arrays.toString(repeatingevents));
        meetings.put("Repeating_Condition", String.valueOf(repeatingCondition));
        meetings.put("End_Date", EndDate);

        FirestoreUtility.getInstance().teststoreEvents(UserManager.getInstance().getCurrentFirebaseUser(),
                meetings, task -> ToastHelper.showToast(context, "Event Saved Successfully"));
    }

    private boolean validateEventDetails() {
        if (binding.EventRoom.getText().toString().trim().isEmpty()) {
            showToast("Please enter a room number");
            return false;
        }
        if (binding.eventTitle.getText().toString().trim().isEmpty()) {
            showToast("Please enter an event title");
            return false;
        }
        if (!LocationsManager.getInstance(this).hasLocation(binding.BuildingSearch.getText().toString().trim())) {
            showToast("Please enter a valid building name");
            return false;
        }
        if (binding.EndResult.getText().toString().trim().isEmpty()) {
            showToast("Please select an end date");
            return false;
        }
        if (binding.EventTime.getText().toString().trim().isEmpty()) {
            showToast("Please select a time");
            return false;
        }
        if (colorPreference == null) {
            showToast("Please pick a valid color");
            return false;
        }
        return true;
    }

    private void addTitle(String str) {
        binding.layoutHeader.setTitle(str);
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
