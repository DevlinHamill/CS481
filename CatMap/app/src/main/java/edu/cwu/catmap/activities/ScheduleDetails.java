package edu.cwu.catmap.activities;

import static android.view.View.VISIBLE;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding;
import com.google.firebase.firestore.DocumentReference;

public class ScheduleDetails extends AppCompatActivity {

    private String colorPreference;

    private String[] buildingNames = {"Alford-Montgomery Hall", "Anderson Hall", "Aquatic Center", "Avation Training Center",
            "Barge Hall", "Barto Hall", "Beck Hall", "Black Hall", "Bouillon Hall", "Breeze Thru Caf", "Brook Lane Village Apartments",
            "Brooks library", "Button Hall", "Carmody-Munro Hall", "Cat Trax East", "Cat Trax West & Cats Market",
            "Central Marketplace", "Coach's Coffee House", "Davies Hall", "Dean Hall", "Discovery Hall", "Dougmore Hall",
            "Early Childhood Learning Center", "Farrell Hall", "Flight Instructor Office Building", "Flight Training Center",
            "Getz-Shortz Apartments", "Green Hall", "Greenhouse", "Grupe Faculty Center", "Health Sciences Building", "Hebeler Hall",
            "Hitchcock Hall", "Hogue Technology Building", "Holmes Dining Room", "Jimmy B's Caf", "Jongeward Building",
            "Kamola Hall", "Kennedy Hall", "Lind Hall", "McConnell Hall", "Mcintyre Music Building", "Meisner Hall",
            "Michaelsen Hall", "Mitchell Hall", "Moore Hall", "Munson Hall", "Naneum Building", "Nicholson Pavilion", "North Hall",
            "Northside Commons", "Old Heating Plant", "Psychology Building", "Public Saftey Building", "Quigley Hall",
            "Randall Hall", "Residence Life", "Samuelson Building", "Science Building", "Shaw-Smyser hall", "Sparks Hall",
            "Stephens-Whitney Hall", "Student Health Services", "Student Union and Recreation Center", "Student Village",
            "Sue Lombard Dining Room", "Sue Lombard Hall", "Surplus Property Warehouse", "The Bistro", "The Village Coffee, Market, Grill",
            "Tomlinson Stadium", "Wahle Apartment Complex", "Wendell Hill Hall A", "Wendell Hill Hall B", "Wildcat Printing", "Wilson Hall"};


    private Context context;

    ActivityScheduleDetailsBinding binding;

    private int[] repeatingevents = new int[7];

    private boolean repeatingCondition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding.inflate(getLayoutInflater());
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
        binding.buildingresult.setIconifiedByDefault(false);
        binding.buildingresult.setIconified(false);
        binding.buildingresult.setQuery(map.get("Building_Name"), false);
        binding.roomresult.setText(map.get("Room_Number"));
        binding.typeresult.setText(map.get("Event_Type"));
        binding.colorResult.setBackgroundColor(Integer.parseInt(map.get("Color_Preference")));
        binding.endresult.setText(map.get("End_Date"));
        colorPreference = map.get("Color_Preference");
        repeatingCondition = Boolean.parseBoolean(map.get("Repeating_Condition"));

        checkbuildings(binding.buildingresult);

        if(repeatingCondition){

            binding.RepeatEventSelector.setChecked(true);
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

        }else if(binding.buildingresult.getQuery().toString().trim().isEmpty()) {
            showToast("Please enter a valid building name");
            return false;

        }else if(!Arrays.asList(buildingNames).contains(binding.buildingresult.getQuery().toString())){
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

    private void checkbuildings(SearchView BuildingSearchView){

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

    private void onclick(){
        binding.RemoveButton.setOnClickListener(v->
                remove()
        );

        binding.editButton.setOnClickListener(v->
                edit()
        );
        binding.colorResult.setOnClickListener(view -> {
            ColorDrawable backgroundColor = (ColorDrawable) binding.colorbackground.getBackground();
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
                            binding.colorResult.setBackgroundColor(lastSelectedColor);
                            colorPreference = ""+lastSelectedColor;
                        }
                    })
                    .build()
                    .show();
        });

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

    private void remove(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events")
                .document(binding.idresult.getText().toString())
                .delete();
        onBackPressed();
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
            tempmap.put("Building_Name", binding.buildingresult.getQuery().toString());
            tempmap.put("Room_Number", binding.roomresult.getText().toString());
            tempmap.put("Event_Type", binding.typeresult.getText().toString());
            tempmap.put("Color_Preference", colorPreference);
            tempmap.put("End_Date", binding.endresult.getText().toString());


            if (binding.RepeatEventSelector.isSelected()) {
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