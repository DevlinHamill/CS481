package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.cwu.catmap.R;
import edu.cwu.catmap.adapters.FavoriteLocationsListItem;
import edu.cwu.catmap.core.ScheduleListItem;
import edu.cwu.catmap.databinding.ActivitySchedualerBinding;
import edu.cwu.catmap.adapters.SchedulerAdapter;

public class SchedualerGUI extends AppCompatActivity {

    private ActivitySchedualerBinding schedualer;

    private SchedulerAdapter adapter;

    private List<ScheduleListItem> EventList;

    private long selectedDateMillis;

    private boolean weekviewcond;

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

        weekviewcond = false;

        Calendar calendar = Calendar.getInstance();
        selectedDateMillis = calendar.getTimeInMillis();

        onclick();

        EventList = new ArrayList<>();
        populateEvents();


    }

    private void populateEvents() {

        EventList.clear();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(selectedDateMillis));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("event_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereEqualTo("Event_Date", formattedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<ScheduleListItem.Event> tempEventList = new ArrayList<>();
                        String lastDate = "";

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventName = document.getString("Event_Title");
                            String eventTime = document.getString("Event_Time");
                            String eventDate = document.getString("Event_Date");
                            String BuildingName = document.getString("Building_Name");
                            String ColorPreference = document.getString("Color_Preference");
                            String EventGroup = document.getString("Event_Group");
                            String id = document.getId();
                            String roomNum = document.getString("Room_Number");
                            String RepeatedEvent = document.getString("Repeated_Events");

                            HashMap<String, String> map = new HashMap<>();
                            map.put("Event_Title", eventName);
                            map.put("Event_Time", eventTime);
                            map.put("Event_Date", eventDate);
                            map.put("Building_Name", BuildingName);
                            map.put("Color_Preference", ColorPreference);
                            map.put("Event_Group", EventGroup);
                            map.put("ID", id);
                            map.put("Room_Number", roomNum);
                            map.put("Repeated_Events",RepeatedEvent);

                            if (eventName != null && eventTime != null && eventDate != null) {
                                tempEventList.add(new ScheduleListItem.Event(eventName, eventTime, map));
                            }
                        }

                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        Collections.sort(tempEventList, (e1, e2) -> {
                            try {
                                Date time1 = timeFormat.parse(e1.getTime());
                                Date time2 = timeFormat.parse(e2.getTime());
                                return time1.compareTo(time2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        });

                        for (ScheduleListItem.Event event : tempEventList) {
                            if (!lastDate.equals(formattedDate)) {
                                EventList.add(new ScheduleListItem.SectionHeader(formattedDate));
                                lastDate = formattedDate;
                            }
                            EventList.add(event);
                        }
                    } else {

                        EventList.add(new ScheduleListItem.SectionHeader("No events this day, try:"));
                        grabSuggestions();
                    }


                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load daily events", Toast.LENGTH_SHORT).show();
                });

    }

    private void onclick() {
        schedualer.AddMeeting.setOnClickListener(v ->
                adding()
        );

        schedualer.WeekButton.setOnClickListener(v ->
                weekView()
        );

        schedualer.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();
            populateEvents();
        });
    }

    private void grabSuggestions() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(selectedDateMillis));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("event_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");


        eventsRef
                .orderBy("Event_Date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String lastDate = "";
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventDate = document.getString("Event_Date");

                            if (eventDate != null) {
                                // Add section header if it's a new date
                                if (!eventDate.equals(lastDate)) {
                                    EventList.add(new ScheduleListItem.SectionHeader(eventDate));
                                    lastDate = eventDate;
                                }

                            }
                        }
                    }

                    // Notify adapter after fetching data
                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load suggested events", Toast.LENGTH_SHORT).show();
                });

        adapter = new SchedulerAdapter(EventList);
        schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        schedualer.eventRecyclerView.setAdapter(adapter);
    }

    private void weekView() {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);


        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());


        long startOfWeekMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        long endOfWeekMillis = calendar.getTimeInMillis();


        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String startOfWeek = sdf.format(new Date(startOfWeekMillis));
        String endOfWeek = sdf.format(new Date(endOfWeekMillis));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("event_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("Event_Date", startOfWeek)
                .whereLessThanOrEqualTo("Event_Date", endOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, List<ScheduleListItem.Event>> eventsByDate = new HashMap<>();


                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventName = document.getString("Event_Title");
                            String eventTime = document.getString("Event_Time");
                            String eventDate = document.getString("Event_Date");
                            String BuildingName = document.getString("Building_Name");
                            String ColorPreference = document.getString("Color_Preference");
                            String EventGroup = document.getString("Event_Group");
                            String id = document.getId();
                            String roomNum = document.getString("Room_Number");
                            String RepeatedEvent = document.getString("Repeated_Events");

                            HashMap<String, String> map = new HashMap<>();
                            map.put("Event_Title", eventName);
                            map.put("Event_Time", eventTime);
                            map.put("Event_Date", eventDate);
                            map.put("Building_Name", BuildingName);
                            map.put("Color_Preference", ColorPreference);
                            map.put("Event_Group", EventGroup);
                            map.put("ID", id);
                            map.put("Room_Number", roomNum);
                            map.put("Repeated_Events",RepeatedEvent);



                            if (eventName != null && eventTime != null && eventDate != null) {
                                ScheduleListItem.Event event = new ScheduleListItem.Event(eventName, eventTime, map);


                                if (!eventsByDate.containsKey(eventDate)) {
                                    eventsByDate.put(eventDate, new ArrayList<>());
                                }
                                eventsByDate.get(eventDate).add(event);
                            }
                        }


                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        for (Map.Entry<String, List<ScheduleListItem.Event>> entry : eventsByDate.entrySet()) {
                            Collections.sort(entry.getValue(), (e1, e2) -> {
                                try {
                                    Date time1 = timeFormat.parse(e1.getTime());
                                    Date time2 = timeFormat.parse(e2.getTime());
                                    return time1.compareTo(time2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });
                        }


                        EventList.clear();
                        for (Map.Entry<String, List<ScheduleListItem.Event>> entry : eventsByDate.entrySet()) {
                            String eventDate = entry.getKey();
                            List<ScheduleListItem.Event> events = entry.getValue();

                            EventList.add(new ScheduleListItem.SectionHeader(eventDate));


                            for (ScheduleListItem.Event event : events) {
                                EventList.add(event);
                            }
                        }
                    } else {

                        EventList.add(new ScheduleListItem.SectionHeader("No events this week"));
                        grabSuggestions();
                    }


                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load weekly events", Toast.LENGTH_SHORT).show();
                });
    }

    private void adding() {

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(selectedDateMillis));

        Intent intent = new Intent(getApplicationContext(), SchedulerOptions.class);
        intent.putExtra("SELECTED_DATE", formattedDate);
        startActivity(intent);
    }


}