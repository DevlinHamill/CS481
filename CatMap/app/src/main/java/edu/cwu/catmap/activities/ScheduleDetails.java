package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding;

public class ScheduleDetails extends AppCompatActivity {

    ActivityScheduleDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        binding.groupresult.setText(map.get("Event_Group"));
        binding.colorResult.setText(map.get("Color_Preference"));
        binding.RepeatingResult.setText(map.get("Repeated_Events"));

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}