package edu.cwu.catmap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import edu.cwu.catmap.databinding.ActivityMainBinding;
import edu.cwu.catmap.databinding.ActivitySchedualerBinding;

public class SchedualerGUI extends AppCompatActivity {

    private ActivitySchedualerBinding schedualer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        schedualer = edu.cwu.catmap.databinding.ActivitySchedualerBinding.inflate(getLayoutInflater());
        setContentView(schedualer.getRoot());
        schedualer.calendarView.setLabelFor(R.id.calendarView);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onclick();
    }

    private void onclick(){
        schedualer.AddMeeting.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), NewEvent.class))
        );
    }
}