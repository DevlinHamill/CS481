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
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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
        binding.buildingresult.setText(map.get("Building_Name"));
        binding.roomresult.setText(map.get("Room_Number"));
        binding.typeresult.setText(map.get("Event_Type"));
        binding.colorResult.setBackgroundColor(Integer.parseInt(map.get("Color_Preference")));
        binding.endresult.setText(map.get("End_Date"));
        colorPreference = map.get("Color_Preference");
        repeatingCondition = Boolean.parseBoolean(map.get("Repeating_Condition"));



        if(repeatingCondition){
            showToast(repeatingCondition+"");
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
        tempmap.put("Event_Type", binding.typeresult.getText().toString());
        tempmap.put("Color_Preference", colorPreference);
        tempmap.put("End_Date", binding.endresult.getText().toString());


        if(binding.RepeatEventSelector.isSelected()){
            if(binding.sunbutton.isSelected()){
                repeatingevents[0] = 1;
            }else {
                repeatingevents[0] = 0;
            }
            if(binding.monbutton.isSelected()){
                repeatingevents[1] = 1;
            }else{
                repeatingevents[1] = 0;
            }
            if(binding.tuebutton.isSelected()){
                repeatingevents[2] = 1;
            }else {
                repeatingevents[2] = 0;
            }
            if(binding.wenbutton.isSelected()){
                repeatingevents[3] = 1;
            }else{
                repeatingevents[3] = 0;
            }
            if(binding.Thurbutton.isSelected()){
                repeatingevents[4] = 1;
            }else{
                repeatingevents[4] = 0;
            }
            if(binding.Fributton.isSelected()){
                repeatingevents[5] = 1;
            }else{
                repeatingevents[5] = 0;
            }
            if(binding.satbutton.isSelected()){
                repeatingevents[6] = 1;
            }else{
                repeatingevents[6] = 0;
            }
            repeatingCondition = true;
        }else{
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
        tempmap.put("Repeating_Condition", repeatingCondition+"");

        ref.update(tempmap)
                .addOnSuccessListener(v -> showToast("Updated Sucessfully"))
                .addOnFailureListener(v -> showToast("Unable to update firebase"));

    }

}