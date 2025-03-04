package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import edu.cwu.catmap.R;
import edu.cwu.catmap.core.FavoriteLocationsListItem;
import edu.cwu.catmap.core.ScheduleListItem;
import edu.cwu.catmap.databinding.ActivitySchedualerBinding;
import edu.cwu.catmap.adapters.SchedulerAdapter;

public class SchedualerGUI extends AppCompatActivity {

    private ActivitySchedualerBinding schedualer;

    private SchedulerAdapter adapter;

    private List<ScheduleListItem> EventList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        schedualer = edu.cwu.catmap.databinding.ActivitySchedualerBinding.inflate(getLayoutInflater());
        setContentView(schedualer.getRoot());
        //schedualer.calendarView.setLabelFor(R.id.calendarView);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        onclick();

        EventList = new ArrayList<>();
        populateEvents();

        adapter = new SchedulerAdapter(EventList);
        schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        schedualer.eventRecyclerView.setAdapter(adapter);

    }

    private void populateEvents() {
        EventList.add(new ScheduleListItem.SectionHeader("2/27/2025"));
        EventList.add(new ScheduleListItem.Event("Meeting", "12:05 PM"));
        EventList.add(new ScheduleListItem.Event("CS 470", "2:00 PM"));
        EventList.add(new ScheduleListItem.Event("CS 481", "4:00 PM"));
    }

    private void onclick(){
        schedualer.AddMeeting.setOnClickListener(v ->
                adding()
        );
    }

    private void adding(){
        Intent intent = new Intent(getApplicationContext(), SchedulerOptions.class);
        intent.putExtra("SELECTED_DATE", schedualer.calendarView.getDate());
        startActivity(intent);
    }


}