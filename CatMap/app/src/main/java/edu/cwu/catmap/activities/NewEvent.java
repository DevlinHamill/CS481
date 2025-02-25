package edu.cwu.catmap.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import edu.cwu.catmap.databinding.ActivityNewEventBinding;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private String name;
    /**
     * the time of the meeting
     */
    private String time;
    /**
     * the description of the meeting
     */
    private String desc;

    private Context context;

    /**
     * creates the application
     * @param savedInstanceState the current application instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = edu.cwu.catmap.databinding.ActivityNewEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        date = getIntent().getStringExtra("SELECTED_DATE");
        setListeners();

        context = this;

        checkbuildings(binding.BuildingSearch);
        checkEventGroups(binding.AddToEventGroupSearch);

    }

    /**
     * sets listeners for buttons
     */
    private void setListeners() {

        binding.backarrow.setOnClickListener(v ->
                onBackPressed()
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
                        }
                    })
                    .build()
                    .show();
        });


        binding.confirmEventButton.setOnClickListener(v -> {

            if(isValidateMeetingDetails()) {
                name = binding.EventTitle.getText().toString();
                desc = binding.EventRoom.getText().toString();

                /**
                 * get the hours from the TimePicker
                 */
                int hour = binding.EventTime.getHour(); // 24-hour format
                /**
                 * get the hours from the TimePicker
                 */
                int minute = binding.EventTime.getMinute();
                // Convert 24-hour format to 12-hour format with AM/PM
                String period = (hour >= 12) ? "PM" : "AM";
                /**
                 *  since hour is still in military time, we change it back with modulo math
                 */
                int hour12Format = (hour == 0 || hour == 12) ? 12 : hour % 12;
                time = hour + ":" + minute + " " + period;

                addMeetingToFirebase();

                onBackPressed();
            }
        });
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
    private void addMeetingToFirebase(){
        /**
         * contains the firebase refrence
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /**
         * stores all meeting data
         */
        HashMap<String, String> meetings = new HashMap<>();
        /**
         * updates the new time with the converter
         */
        String newtime = convertTo12Hour(time);

        meetings.put("Meeting_Name", name);
        meetings.put("Meeting_Date", date);
        meetings.put("Meeting_Time", newtime);
        meetings.put("Meeting_Description", desc);

        db.collection("Meetings").add(meetings).addOnSuccessListener( documentReference -> {
            showToast("meeting added");
        }).addOnFailureListener(exception ->{
            showToast(exception.getMessage());
        });
    }

    /**
     * checks if the input is valid
     * @return a boolean value that tells if the input is valid
     */
    private boolean isValidateMeetingDetails() {
        if (binding.EventRoom.getText().toString().trim().isEmpty()){
            showToast("Please enter a description for the meeting");
            return false;
        }else if(binding.EventTitle.getText().toString().trim().isEmpty()) {
            showToast("Please enter a meeting title");
            return false;
        }else{
            return true;
        }
    }

    /**
     * converts military time to 12 hour based time
     * @param time24 takes in a
     * @return the new time
     */
    private String convertTo12Hour(String time24){
        /**
         * tokenizes the time string without the :
         */
        StringTokenizer tokenizer = new StringTokenizer(time24, ":");
        /**
         * stores the current hour based on the string
         */
        int hour = 0;
        hour = Integer.parseInt(tokenizer.nextToken());
        /**
         * converts the string to a 12 hour time
         */
        int hour12 = (hour == 0 || hour == 12) ? 12 : hour % 12;

        return hour12 +":"+ tokenizer.nextToken();
    }

    private void setWeekVisible(){

        if(binding.Weeklayout.getVisibility() == View.INVISIBLE){
            binding.Weeklayout.setVisibility(View.VISIBLE);
        }else{
            binding.Weeklayout.setVisibility(View.INVISIBLE);
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
        /*Sample building names for suggestions*/
        String[] buildingNames = {"Library", "Recreational Center", "Rebirth"};

        /*Adapter for auto suggestions*/
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, buildingNames);

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

    private void checkEventGroups(SearchView EventGroupSearch){
        /*Sample event names for suggestions*/
        String[] EventGroups = {"Group1", "Group2"};

        /*Adapter for auto suggestions*/
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, EventGroups);

        /*Attaches autocomplete functionality to the search view*/
        AutoCompleteTextView searchAutoComplete = EventGroupSearch.findViewById(androidx.appcompat.R.id.search_src_text);
        if (searchAutoComplete != null) {
            searchAutoComplete.setAdapter(adapter);

            searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
                String selectedItem = adapter.getItem(position);
                searchAutoComplete.setText(selectedItem);
                EventGroupSearch.setQuery(selectedItem, false);
            });
        }
    }


    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


}

