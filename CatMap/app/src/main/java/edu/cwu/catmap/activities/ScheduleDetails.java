package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityScheduleDetailsBinding;
import com.google.firebase.firestore.DocumentReference;

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

        onclick();

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void onclick(){
        binding.RemoveButton.setOnClickListener(v->
                remove()
        );

        binding.editButton.setOnClickListener(v->
                edit()
        );
    }

    private void remove(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("event_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events")
                .document(binding.idresult.getText().toString())
                .delete();
        onBackPressed();
    }

    private void edit() {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("event_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events")
                .document(binding.idresult.getText().toString());

        HashMap<String, Object> tempmap = new HashMap<String, Object>();


        tempmap.put("Event_Title", binding.nameresult.getText().toString());
        tempmap.put("Event_Date", binding.DateResult.getText().toString());
        tempmap.put("Event_Time", binding.timeresult.getText().toString());
        tempmap.put("Building_Name", binding.buildingresult.getText().toString());
        tempmap.put("Room_Number", binding.roomresult.getText().toString());
        tempmap.put("Event_Group", binding.groupresult.getText().toString());
//        binding.colorResult.setText(map.get("Color_Preference"));
//        binding.RepeatingResult.setText(map.get("Repeated_Events"));
        ref.update(tempmap)
                .addOnSuccessListener(v -> showToast("Updated Sucessfully"))
                .addOnFailureListener(v -> showToast("Unable to update firebase"));

    }

}