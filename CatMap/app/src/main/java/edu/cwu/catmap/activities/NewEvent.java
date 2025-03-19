package edu.cwu.catmap.activities;

import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.cwu.catmap.databinding.ActivityNewEventBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.Constants;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;


public class NewEvent extends AppCompatActivity {
    /**
     * activity binding
     */
    private @NonNull ActivityNewEventBinding binding;
    /**
     * string values that are being saved on the firebase
     */
    private String date, Title, time, RoomNum, colorPreference, Building, Event_Type, EndDate;
    /**
     * stores the events as an array list from sunday - saturday
     */
    private int[] repeatingevents = new int[7];
    /**
     * the current app instance
     */
    private Context context;
    /**
     * a boolean condition that tells if the event is repeating or not
     */
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

        Log.i("New Event", "Passed event type: " + Event_Type + ", date: " + date);

        setListeners();
        addTitle(Event_Type);
        updateGUIIfAddingToClass();
        setupBuildingSearch(binding.BuildingSearch);
    }

    /**
     * sets on click listeners
     */
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
            binding.endDateLayout.setVisibility(isChecked ? VISIBLE : View.GONE);
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

    /**
     * updates the UI more effectively when the class is added
     */
    private void updateGUIIfAddingToClass() {
        String eventType = getIntent().getStringExtra(Constants.KEY_NEW_EVENT_TYPE);
        boolean isExistingClass = getIntent().getBooleanExtra(Constants.KEY_NEW_EVENT_IS_EXISTING_CLASS, false);

        if(eventType != null && eventType.equals(Constants.VALUE_EVENT_TYPE_CLASS)) {
            if(isExistingClass) {
                //set the event title
                binding.eventTitle.setText(getIntent().getStringExtra(Constants.KEY_NEW_EVENT_CLASS_NAME));
                binding.eventTitle.setEnabled(false);

                //set the event color
                int providedColor = getIntent().getIntExtra(Constants.KEY_NEW_EVENT_CLASS_COLOR, Color.BLACK);
                binding.colorPickerButton.setBackgroundColor(providedColor);
                binding.colorPickerButton.setText("Class Color");
                binding.colorPickerButton.setClickable(false);

                colorPreference = String.valueOf(providedColor);

                //set the header to include more context
                binding.layoutHeader.setTitle("Add to Existing Class");
            }
            else {
                binding.layoutHeader.setTitle("Create New Class");
            }
        }
        else {
            binding.layoutHeader.setTitle("Create New Event");
        }
    }

    /**
     * sets the buttons for specific days to reflect repeating events
     * @param button button being selected
     */
    private void setselecteddays( com.google.android.material.button.MaterialButton button){
        if(!button.isSelected()) {
            button.setSelected(true);
        }else{
            button.setSelected(false);
        }
    }

    /**
     * shows the date picker to retrieve the end date selection
     */
    private void showDatePicker() {
        /**
         * creates the calender object to retreive the current selected date
         */
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    /**
                     * formats the date properly
                     */
                    String formattedDate = String.format("%02d/%02d/%d", month + 1, dayOfMonth, year);
                    binding.EndResult.setText(formattedDate);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    /**
     * shows the time picker
     */
    private void showTimePicker() {
        /**
         * Creates a calender object to retrieve the selected time
         */
        final Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    /**
                     * Sets the am pm string based on the selected hours
                     */
                    String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                    /**
                     * sets hours to a 12 hour interval
                     */
                    int hour12 = (hourOfDay == 0) ? 12 : (hourOfDay > 12 ? hourOfDay - 12 : hourOfDay);
                    /**
                     * sets the time selected to the proper formate
                     */
                    String formattedTime = String.format("%d:%02d %s", hour12, minute, amPm);
                    binding.EventTime.setText(formattedTime);
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);

        timePickerDialog.show();
    }

    /**
     * shows the color picker
     */
    private void showColorPicker() {

        /**
         * Retreieves the background layout color of the app
         */
        Drawable background = binding.createMeetingLayout.getBackground();

        /**
         * grabs a basic white color to edit later with the color picker
         */
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

    /**
     * Recommends buildings based on location manager methods
     * @param buildingSearch building textfield being checked
     */
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

    /**
     * grabs the new info
     */
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

    /**
     * adds the new info to the firebase
     */
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

    /**
     * Checks for valid event details
     * @return if the event inputs are valid
     */
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

    /**
     * adds a title to the header
     * @param str string updating the title
     */
    private void addTitle(String str) {
        binding.layoutHeader.setTitle(str);
    }

    /**
     * shows an toast message
     * @param message message being displayed
     */
    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
