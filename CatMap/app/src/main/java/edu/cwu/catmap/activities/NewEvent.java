package edu.cwu.catmap.activities;

import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import edu.cwu.catmap.core.Event;
import edu.cwu.catmap.databinding.ActivityNewEventBinding;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.manager.UserManager;
import edu.cwu.catmap.utilities.FirestoreUtility;
import edu.cwu.catmap.utilities.ToastHelper;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;
/**
 * @author BT&T
 * CS 460
 */
public class NewEvent extends AppCompatActivity {
    /**
     * binds the xml with the java
     */
    private @NonNull ActivityNewEventBinding binding;
    /**
     * the date of the meeting
     */
    private String date;
    /**
     * the name of the meeting
     */
    private String Title;
    /**
     * the time of the meeting
     */
    private String time;
    /**
     * the description of the meeting
     */
    private String RoomNum;

    private String colorPreference;

    private String Building;

    private String Event_Type;

    private String EndDate;

    private int[] repeatingevents = new int[7];


    private Context context;

    private boolean repeatingcondtion;

    /**
     * creates the application
     * @param savedInstanceState the current application instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNewEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Event_Type = getIntent().getStringExtra("header");
        date = getIntent().getStringExtra("SELECTED_DATE");
        addtitle(Event_Type);
        setListeners();
        context = this;
        repeatingcondtion = false;
        checkbuildings(binding.BuildingSearch);

    }

    /**
     * sets listeners for buttons
     */
    private void setListeners() {

        binding.backarrow.setOnClickListener(v ->{
                onBackPressed();
                onBackPressed();
                }
        );


        binding.EndResult.setOnClickListener(v->
                showDatePicker()
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

        binding.colorPickerButton.setOnClickListener(view -> {
            ColorDrawable backgroundColor = (ColorDrawable) binding.createMeetingLayout.getBackground();
            int backgroundColorInt = backgroundColor.getColor();

            ColorPickerDialogBuilder
                    .with(this)
                    .setTitle("Choose a color")
                    .initialColor(backgroundColorInt)
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .showAlphaSlider(false)
                    .setOnColorSelectedListener(new OnColorSelectedListener() {
                        @Override
                        public void onColorSelected(int selectedColor) {
                            showToast(context, "selected color " + selectedColor);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showToast(context, "Select color canceled");
                        }
                    })
                    .setPositiveButton("confirm", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                            binding.colorPickerButton.setBackgroundColor(lastSelectedColor);
                            colorPreference = ""+lastSelectedColor;
                        }
                    })
                    .build()
                    .show();
        });

        binding.EventTime.setOnClickListener(v->
                showTimePicker()
        );


        binding.confirmEventButton.setOnClickListener(v -> {

            if(isValidateMeetingDetails()) {
                Title = binding.EventTitle.getText().toString();
                RoomNum = binding.EventRoom.getText().toString();
                Building = binding.BuildingSearch.getQuery().toString();
                EndDate = binding.EndResult.getText().toString();

                if(binding.RepeatEventSelector.isSelected()){
                    if(binding.sunbutton.isSelected()){
                        repeatingevents[0] = 1;
                    }if(binding.monbutton.isSelected()){
                        repeatingevents[1] = 1;
                    }if(binding.tuebutton.isSelected()){
                        repeatingevents[2] = 1;
                    }if(binding.wenbutton.isSelected()){
                        repeatingevents[3] = 1;
                    }if(binding.Thurbutton.isSelected()){
                        repeatingevents[4] = 1;
                    }if(binding.Fributton.isSelected()){
                        repeatingevents[5] = 1;
                    }if(binding.satbutton.isSelected()){
                        repeatingevents[6] = 1;
                    }
                    repeatingcondtion = !repeatingcondtion;
                }

                time = binding.EventTime.getText().toString();

                addMeetingToFirebase();

                finish();
                startActivity(new Intent(getApplicationContext(), SchedualerGUI.class));
            }
        });
    }


    private void showDatePicker() {

        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String formattedDate = String.format("%02d/%02d/%d", selectedMonth + 1, selectedDay, selectedYear);
                    binding.EndResult.setText(formattedDate);
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
                    binding.EventTime.setText(formattedTime);

                }, hour, minute, false);

        timePickerDialog.show();
    }

    /**
     * displays the toast message
     * @param message string that needs to be displayed
     */
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * adds the meeting to the firebase
     */
    private void addMeetingToFirebase() {


        /**
         * contains the firebase refrence
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /**
         * stores all meeting data
         */
        HashMap<String, String> meetings = new HashMap<>();

        meetings.put("Event_Title", Title);
        meetings.put("Event_Type", Event_Type);
        meetings.put("Building_Name", Building);
        meetings.put("Room_Number", RoomNum);
        meetings.put("Event_Date", date);
        meetings.put("Event_Time", time);
        meetings.put("Color_Preference", colorPreference);
        meetings.put("Repeated_Events", Arrays.toString(repeatingevents));
        meetings.put("Repeating_Condition", repeatingcondtion+"");
        meetings.put("End_Date", EndDate);

        FirestoreUtility.getInstance().teststoreEvents(UserManager.getInstance().getCurrentFirebaseUser(), meetings, new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                ToastHelper.showToast(context, "Sent Event");
            }
        });
    }

    /**
     * checks if the input is valid
     * @return a boolean value that tells if the input is valid
     */
    private boolean isValidateMeetingDetails() {
        if(binding.EventRoom.getText().toString().trim().isEmpty()){
            showToast("Please enter a room number");
            return false;

        }
        else if(binding.EventTitle.getText().toString().trim().isEmpty()) {
            showToast("Please enter a Event title");
            return false;

        }
        else if(!LocationsManager.getInstance(this).hasLocation(binding.BuildingSearch.getQuery().toString().trim())) {
            showToast("Please enter a valid building name");
            return false;

        }
        else if(binding.EndResult.getText().toString().trim().isEmpty()) {
            showToast("Please select an end date");
            return false;

        }
        else if(binding.EventTime.getText().toString().trim().isEmpty()){
            showToast("Please select a time");
            return false;
        }
        else if(colorPreference.isEmpty()){
            showToast("Please pick a valid color");
            return false;

        }
        else{
            return true;
        }
    }



    private void setWeekVisible(){

        if(binding.Weeklayout.getVisibility() == View.INVISIBLE){
            binding.Weeklayout.setVisibility(VISIBLE);
            binding.RepeatEventSelector.setSelected(true);
        }else{
            binding.Weeklayout.setVisibility(View.INVISIBLE);
            binding.RepeatEventSelector.setSelected(false);
        }

    }

    private void setselecteddays(TextView button){
        if(!button.isSelected()) {
            button.setBackgroundColor(Color.GRAY);
            button.setSelected(true);
        }else{
            button.setBackgroundColor(Color.WHITE);
            button.setSelected(false);
        }
    }

    private void checkbuildings(SearchView BuildingSearchView){
        /*Adapter for auto suggestions*/
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, LocationsManager.getInstance(this).getLocationNames().toArray(new String[0]));

        /*Attaches autocomplete functionality to the search view*/
        AutoCompleteTextView searchAutoComplete = BuildingSearchView.findViewById(androidx.appcompat.R.id.search_src_text);

        if (searchAutoComplete != null) {
            searchAutoComplete.setAdapter(adapter);

            searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = adapter.getItem(position);
                searchAutoComplete.setText(selectedItem);
                BuildingSearchView.setQuery(selectedItem, false); /*Set the autocomplete query without submitting it*/
            });
        }
    }


    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public void addtitle(String str){
        binding.neweventheader.setText(str);
        binding.neweventheader.setVisibility(VISIBLE);
        showToast(binding.neweventheader.getText().toString());
    }

}

