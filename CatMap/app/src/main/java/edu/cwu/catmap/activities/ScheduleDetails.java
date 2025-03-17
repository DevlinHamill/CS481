package edu.cwu.catmap.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import edu.cwu.catmap.R;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding;
import edu.cwu.catmap.utilities.Constants;

import com.google.firebase.firestore.DocumentReference;

public class ScheduleDetails extends AppCompatActivity {

    private String Event_type;
    private String colorPreference;
    private Context context;

    ActivityScheduleDetailsBinding binding;

    private int[] repeatingevents = new int[7];

    private boolean repeatingCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityScheduleDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        context = this;
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        HashMap<String, String> map = (HashMap<String, String>)getIntent().getSerializableExtra("Map");

        binding.idresult.setText(map.get("ID"));
        binding.nameresult.setText(map.get("Event_Title"));
        binding.DateResult.setText(map.get("Event_Date"));
        binding.timeresult.setText(map.get("Event_Time"));
//        binding.buildingresult.setIconifiedByDefault(false);
//        binding.buildingresult.setIconified(false);
//        binding.buildingresult.setQuery(map.get("Building_Name"), false);
        binding.buildingresult.setText(map.get("Building_Name"));
        binding.roomresult.setText(map.get("Room_Number"));
        Event_type = map.get("Event_Type");
        binding.layoutHeader.setTitle("Edit "+ Event_type);
        binding.colorResult.setBackgroundColor(Integer.parseInt(map.get("Color_Preference")));
        binding.endresult.setText(map.get("End_Date"));
        colorPreference = map.get("Color_Preference");
        repeatingCondition = Boolean.parseBoolean(map.get("Repeating_Condition"));

        checkbuildings(binding.buildingresult);

        if(repeatingCondition){

            binding.RepeatEventSelector.setChecked(true);
            binding.RepeatEventSelector.setSelected(true);
            binding.Weeklayout.setVisibility(View.VISIBLE);
            binding.RepeatEventSelector.setSelected(true);
            String repeatedEventsString = map.get("Repeated_Events");
            repeatedEventsString = repeatedEventsString.replaceAll("[\\[\\]]", "");
            String[] tempStringArr = repeatedEventsString.split(",");


            int[] intArray = Arrays.stream(tempStringArr)
                    .map(String::trim)
                    .mapToInt(Integer::parseInt)
                    .toArray();

            if(intArray[0] == 1){
                setselecteddays(binding.sunbutton);
            }if(intArray[1] == 1){
                setselecteddays(binding.monbutton);
            }if(intArray[2] == 1){
                setselecteddays(binding.tuebutton);
            }if(intArray[3] == 1){
                setselecteddays(binding.wenbutton);
            }if(intArray[4] == 1){
                setselecteddays(binding.Thurbutton);
            }if(intArray[5] == 1){
                setselecteddays(binding.Fributton);
            }if(intArray[6] == 1){
                setselecteddays(binding.satbutton);
            }

        }


        onclick();
        updateGUIIfEditingClass();
    }

    private void updateGUIIfEditingClass() {
        String eventType = Event_type;

        if(eventType != null && eventType.equals(Constants.VALUE_EVENT_TYPE_CLASS)) {
            //set the event title
            binding.nameresult.setEnabled(false);

            //set the event color
            binding.colorPickerText.setText("Class Color");
            binding.colorResult.setClickable(false);

            //set the header to include more context
            binding.layoutHeader.setTitle("Edit Existing Class Event");
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * checks if the input is valid
     * @return a boolean value that tells if the input is valid
     */
    private boolean isValidateMeetingDetails() {
        if(binding.roomresult.getText().toString().trim().isEmpty()){
            showToast("Please enter a room number");
            return false;

        }else if(binding.nameresult.getText().toString().trim().isEmpty()) {
            showToast("Please enter a Event title");
            return false;

        }else if(!LocationsManager.getInstance(this).hasLocation(binding.buildingresult.getText().toString().trim())) {
            showToast("Please enter a valid building name");
            return false;

        }else if(!LocationsManager.getInstance(this).getLocationNames().contains(binding.buildingresult.getText().toString())){
            showToast("Please enter an existing building name");
            return false;

        }else if(binding.endresult.getText().toString().trim().isEmpty()) {
            showToast("Please select an end date");
            return false;

        }else if(binding.timeresult.getText().toString().trim().isEmpty()){
            showToast("Please select a time");
            return false;
        }else if(colorPreference == null){
            showToast("Please pick a valid color");
            return false;

        }else{
            return true;
        }
    }

    private void checkbuildings(AutoCompleteTextView buildingSearch) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                LocationsManager.getInstance(this).getLocationNames().toArray(new String[0]));

        buildingSearch.setAdapter(adapter);
        buildingSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selectedBuilding = adapter.getItem(position);
            buildingSearch.setText(selectedBuilding, false);
        });
    }


    private void onclick(){
        binding.RemoveButton.setOnClickListener(v ->
                remove()
        );

        binding.editButton.setOnClickListener(v -> {
            edit();
            Intent intent = new Intent(this, SchedualerGUI.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
                }

        );
        binding.colorResult.setOnClickListener(view ->
            showColorPicker()
        );

        binding.RepeatEventSelector.setOnClickListener(v ->
                setWeekVisible()
        );

        binding.sunbutton.setOnClickListener(v->
                setselecteddays(binding.sunbutton)
        );

        binding.monbutton.setOnClickListener(v->
                setselecteddays(binding.monbutton)
        );

        binding.tuebutton.setOnClickListener(v->
                setselecteddays(binding.tuebutton)
        );

        binding.wenbutton.setOnClickListener(v->
                setselecteddays(binding.wenbutton)
        );

        binding.Thurbutton.setOnClickListener(v->
                setselecteddays(binding.Thurbutton)
        );

        binding.Fributton.setOnClickListener(v->
                setselecteddays(binding.Fributton)
        );

        binding.satbutton.setOnClickListener(v->
                setselecteddays(binding.satbutton)
        );
        binding.timeresult.setOnClickListener(v->
                showTimePicker()
        );

        binding.endresult.setOnClickListener(v->
            showDatePicker()
        );

        binding.layoutHeader.setNavigationOnClickListener(v ->
                onBackPressed()
        );



    }

    private void showColorPicker() {
        Drawable background = binding.colorbackground.getBackground();

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
                    binding.colorResult.setBackgroundColor(lastSelectedColor);
                    colorPreference = String.valueOf(lastSelectedColor);
                })
                .setNegativeButton("Cancel", (dialog, which) -> showToast("Color selection canceled"))
                .build()
                .show();
    }

    private void showDatePicker() {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {

                    String formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    binding.endresult.setText(formattedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {

        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (TimePicker view, int selectedHour, int selectedMinute) -> {

                    String propertime = (selectedHour >= 12) ? "PM" : "AM";
                    int hour12 = (selectedHour == 0) ? 12 : (selectedHour > 12 ? selectedHour - 12 : selectedHour);
                    String formattedTime = String.format("%d:%02d %s", hour12, selectedMinute, propertime);
                    binding.timeresult.setText(formattedTime);

                }, hour, minute, false);

        timePickerDialog.show();
    }

    private void setWeekVisible(){

        boolean isChecked = binding.RepeatEventSelector.isChecked();
        binding.Weeklayout.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);

    }

    private void setselecteddays( com.google.android.material.button.MaterialButton button){
        if(!button.isSelected()) {
            button.setSelected(true);
            button.setChecked(true);
        }else{
            button.setSelected(false);
            button.setChecked(false);
        }
    }

    private void remove(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events")
                .document(binding.idresult.getText().toString())
                .delete();

        Intent intent = new Intent(getApplicationContext(), SchedualerGUI.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void edit() {

        if(isValidateMeetingDetails()) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("user_collection")
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection("Events")
                    .document(binding.idresult.getText().toString());

            HashMap<String, Object> tempmap = new HashMap<String, Object>();


            tempmap.put("Event_Title", binding.nameresult.getText().toString());
            tempmap.put("Event_Date", binding.DateResult.getText().toString());
            tempmap.put("Event_Time", binding.timeresult.getText().toString());
            tempmap.put("Building_Name", binding.buildingresult.getText().toString());
            tempmap.put("Room_Number", binding.roomresult.getText().toString());
            tempmap.put("Event_Type", Event_type);
            tempmap.put("Color_Preference", colorPreference);
            tempmap.put("End_Date", binding.endresult.getText().toString());


            if (binding.RepeatEventSelector.isChecked()) {
                if (binding.sunbutton.isSelected()) {
                    repeatingevents[0] = 1;
                } else {
                    repeatingevents[0] = 0;
                }
                if (binding.monbutton.isSelected()) {
                    repeatingevents[1] = 1;
                } else {
                    repeatingevents[1] = 0;
                }
                if (binding.tuebutton.isSelected()) {
                    repeatingevents[2] = 1;
                } else {
                    repeatingevents[2] = 0;
                }
                if (binding.wenbutton.isSelected()) {
                    repeatingevents[3] = 1;
                } else {
                    repeatingevents[3] = 0;
                }
                if (binding.Thurbutton.isSelected()) {
                    repeatingevents[4] = 1;
                } else {
                    repeatingevents[4] = 0;
                }
                if (binding.Fributton.isSelected()) {
                    repeatingevents[5] = 1;
                } else {
                    repeatingevents[5] = 0;
                }
                if (binding.satbutton.isSelected()) {
                    repeatingevents[6] = 1;
                } else {
                    repeatingevents[6] = 0;
                }
                repeatingCondition = true;
            } else {
                repeatingevents[0] = 0;
                repeatingevents[1] = 0;
                repeatingevents[2] = 0;
                repeatingevents[3] = 0;
                repeatingevents[4] = 0;
                repeatingevents[5] = 0;
                repeatingevents[6] = 0;
                repeatingCondition = false;
            }

            tempmap.put("Repeated_Events", Arrays.toString(repeatingevents));
            tempmap.put("Repeating_Condition", repeatingCondition + "");

            ref.update(tempmap)
                    .addOnSuccessListener(v -> showToast("Updated Sucessfully"))
                    .addOnFailureListener(v -> showToast("Unable to update firebase"));
        }
    }

}