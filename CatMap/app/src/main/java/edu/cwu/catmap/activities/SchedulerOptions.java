package edu.cwu.catmap.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivitySchedulerOptionsBinding;

public class SchedulerOptions extends AppCompatActivity {

    public ActivitySchedulerOptionsBinding scheduleroptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_scheduler_options);
        scheduleroptions = ActivitySchedulerOptionsBinding.inflate(getLayoutInflater());
        setContentView(scheduleroptions.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onclick();
    }

    private void onclick(){
        scheduleroptions.AddtoClassbutton.setOnClickListener(v ->
                updatetext("Class")
        );

        scheduleroptions.AddtoEventbutton.setOnClickListener(v ->
                updatetext("Event")
        );
    }

    private void updatetext(String text){
        if(text.equals("Event")) {

            Intent intent = new Intent(getApplicationContext(), NewEvent.class);
            intent.putExtra("header", "Event");
            intent.putExtra("SELECTED_DATE", getIntent().getStringExtra("SELECTED_DATE"));
            startActivity(intent);

        }else{
            Intent intent = new Intent(getApplicationContext(), ClassManagerActivity.class);
            startActivity(intent);
        }
    }

}