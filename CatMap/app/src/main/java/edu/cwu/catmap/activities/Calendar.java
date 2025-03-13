package edu.cwu.catmap.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

import edu.cwu.catmap.R;


public class Calendar extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calender);  // Ensure this matches your activity layout

        ImageButton fabAddMeeting = findViewById(R.id.fabAddMeeting);

        // Show bottom sheet on button click
        fabAddMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddMeetingBottomSheet bottomSheet = new AddMeetingBottomSheet();
                bottomSheet.show(getSupportFragmentManager(), "AddMeetingBottomSheet");
            }
        });
    }
}
