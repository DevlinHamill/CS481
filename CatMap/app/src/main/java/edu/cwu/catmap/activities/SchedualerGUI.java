package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import java.util.Set;
import java.util.TreeSet;

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        String formattedDate = sdf.format(calendar.getTime());

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", formattedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasEvents = false;
                    List<ScheduleListItem.Event> tempEventList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String eventName = document.getString("Event_Title");
                        String eventTime = document.getString("Event_Time");
                        String eventDate = document.getString("Event_Date");
                        String buildingName = document.getString("Building_Name");
                        String colorPreference = document.getString("Color_Preference");
                        String eventType = document.getString("Event_Type");
                        String id = document.getId();
                        String roomNum = document.getString("Room_Number");
                        String repeatedEvent = document.getString("Repeated_Events"); // Stored as string
                        String repeatingCondition = document.getString("Repeating_Condition"); // Stored as string
                        String End_Date = document.getString("End_Date");

                        boolean isRepeatingEvent = false;

                        if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                            try {
                                repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                String[] daysArray = repeatedEvent.split(",\\s*");

                                if (daysArray.length == 7 && "1".equals(daysArray[selectedDayOfWeek])) {
                                    isRepeatingEvent = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }


                        if (formattedDate.equals(eventDate) || isRepeatingEvent) {
                            hasEvents = true;
                            HashMap<String, String> map = new HashMap<>();
                            map.put("Event_Title", eventName);
                            map.put("Event_Time", eventTime);
                            map.put("Event_Date", eventDate);
                            map.put("Building_Name", buildingName);
                            map.put("Color_Preference", colorPreference);
                            map.put("Event_Type", eventType);
                            map.put("ID", id);
                            map.put("Room_Number", roomNum);
                            map.put("Repeated_Events", repeatedEvent);
                            map.put("Repeating_Condition", repeatingCondition);
                            map.put("End_Date", End_Date);

                            if (eventName != null && eventTime != null) {
                                tempEventList.add(new ScheduleListItem.Event(eventName, eventTime, map));
                            }
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


                    if (hasEvents) {
                        EventList.add(new ScheduleListItem.SectionHeader(formattedDate));
                        EventList.addAll(tempEventList);
                    } else {

                        EventList.add(new ScheduleListItem.SectionHeader("No events this day, try:"));


                        grabSuggestions();
                    }

                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                    schedualer.eventRecyclerView.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load daily events", Toast.LENGTH_SHORT).show();
                });

    }


    private void onclick() {
        schedualer.AddMeeting.setOnClickListener(v ->
                adding()
        );

        schedualer.WeekButton.setOnClickListener(v -> {
                    schedualer.eventRecyclerView.setVisibility(View.INVISIBLE);
                    weekView();
                }
        );

        schedualer.MonthButton.setOnClickListener(v->
                {
                    schedualer.eventRecyclerView.setVisibility(View.INVISIBLE);
                    schedualer.calendarView.setVisibility(View.VISIBLE);
                    schedualer.AddMeeting.setVisibility(View.VISIBLE);
                    populateEvents();
                }
        );

        schedualer.calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            selectedDateMillis = calendar.getTimeInMillis();
            populateEvents();
        });

        schedualer.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void grabSuggestions() {
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
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", startOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, List<ScheduleListItem.Event>> eventsByDate = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventName = document.getString("Event_Title");
                            String eventTime = document.getString("Event_Time");
                            String eventDate = document.getString("Event_Date");
                            String buildingName = document.getString("Building_Name");
                            String colorPreference = document.getString("Color_Preference");
                            String eventType = document.getString("Event_Type");
                            String id = document.getId();
                            String roomNum = document.getString("Room_Number");
                            String repeatedEvent = document.getString("Repeated_Events");
                            String repeatingCondition = document.getString("Repeating_Condition");
                            String End_Date = document.getString("End_Date");

                            HashMap<String, String> map = new HashMap<>();
                            map.put("Event_Title", eventName);
                            map.put("Event_Time", eventTime);
                            map.put("Event_Date", eventDate);
                            map.put("Building_Name", buildingName);
                            map.put("Color_Preference", colorPreference);
                            map.put("Event_Type", eventType);
                            map.put("ID", id);
                            map.put("Room_Number", roomNum);
                            map.put("Repeated_Events", repeatedEvent);
                            map.put("Repeating_Condition", repeatingCondition);
                            map.put("End_Date", End_Date);

                            boolean isRepeatingEvent = false;
                            List<String> repeatingDates = new ArrayList<>();

                            if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                                try {
                                    repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                    String[] daysArray = repeatedEvent.split(",\\s*");

                                    if (daysArray.length == 7) {
                                        Calendar weekCalendar = Calendar.getInstance();
                                        weekCalendar.setTimeInMillis(startOfWeekMillis);

                                        for (int i = 0; i < 7; i++) {
                                            if ("1".equals(daysArray[i])) {
                                                repeatingDates.add(sdf.format(weekCalendar.getTime()));
                                            }
                                            weekCalendar.add(Calendar.DAY_OF_WEEK, 1);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            if (eventDate != null && eventTime != null && (eventDate.compareTo(startOfWeek) >= 0 && eventDate.compareTo(endOfWeek) <= 0)) {
                                eventsByDate.putIfAbsent(eventDate, new ArrayList<>());

                            }


                            for (String repeatDate : repeatingDates) {
                                eventsByDate.putIfAbsent(repeatDate, new ArrayList<>());

                            }
                        }


                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        for (List<ScheduleListItem.Event> eventList : eventsByDate.values()) {
                            Collections.sort(eventList, (e1, e2) -> {
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

                        for (String eventDate : new TreeSet<>(eventsByDate.keySet())) {
                            EventList.add(new ScheduleListItem.SectionHeader(eventDate));
                            EventList.addAll(eventsByDate.get(eventDate));
                        }
                    } else {
                        EventList.add(new ScheduleListItem.SectionHeader("No events this week"));
                    }

                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), "Failed to load weekly events", Toast.LENGTH_SHORT).show();
                });
        schedualer.eventRecyclerView.setVisibility(View.VISIBLE);
    }


    private void weekView() {
        EventList.clear();
        schedualer.calendarView.setVisibility(View.GONE);
        schedualer.AddMeeting.setVisibility(View.GONE);

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
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", startOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Map<String, List<ScheduleListItem.Event>> eventsByDate = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventName = document.getString("Event_Title");
                            String eventTime = document.getString("Event_Time");
                            String eventDate = document.getString("Event_Date");
                            String buildingName = document.getString("Building_Name");
                            String colorPreference = document.getString("Color_Preference");
                            String eventType = document.getString("Event_Type");
                            String id = document.getId();
                            String roomNum = document.getString("Room_Number");
                            String repeatedEvent = document.getString("Repeated_Events");
                            String repeatingCondition = document.getString("Repeating_Condition");
                            String End_Date = document.getString("Repeating_Condition");

                            HashMap<String, String> map = new HashMap<>();
                            map.put("Event_Title", eventName);
                            map.put("Event_Time", eventTime);
                            map.put("Event_Date", eventDate);
                            map.put("Building_Name", buildingName);
                            map.put("Color_Preference", colorPreference);
                            map.put("Event_Type", eventType);
                            map.put("ID", id);
                            map.put("Room_Number", roomNum);
                            map.put("Repeated_Events", repeatedEvent);
                            map.put("Repeating_Condition", repeatingCondition);
                            map.put("End_Date", End_Date);
                            boolean isRepeatingEvent = false;
                            List<String> repeatingDates = new ArrayList<>();

                            if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                                try {
                                    repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                    String[] daysArray = repeatedEvent.split(",\\s*");

                                    if (daysArray.length == 7) {
                                        Calendar weekCalendar = Calendar.getInstance();
                                        weekCalendar.setTimeInMillis(startOfWeekMillis);

                                        for (int i = 0; i < 7; i++) {
                                            if ("1".equals(daysArray[i])) {
                                                repeatingDates.add(sdf.format(weekCalendar.getTime()));
                                            }
                                            weekCalendar.add(Calendar.DAY_OF_WEEK, 1);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }


                            if (eventDate != null && eventTime != null && (eventDate.compareTo(startOfWeek) >= 0 && eventDate.compareTo(endOfWeek) <= 0)) {
                                eventsByDate.putIfAbsent(eventDate, new ArrayList<>());
                                if (!repeatingDates.contains(eventDate)) {
                                    eventsByDate.get(eventDate).add(new ScheduleListItem.Event(eventName, eventTime, map));
                                }
                            }


                            for (String repeatDate : repeatingDates) {
                                eventsByDate.putIfAbsent(repeatDate, new ArrayList<>());
                                eventsByDate.get(repeatDate).add(new ScheduleListItem.Event(eventName, eventTime, map));
                            }
                        }


                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        for (List<ScheduleListItem.Event> eventList : eventsByDate.values()) {
                            Collections.sort(eventList, (e1, e2) -> {
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
                        for (String eventDate : new TreeSet<>(eventsByDate.keySet())) {
                            EventList.add(new ScheduleListItem.SectionHeader(eventDate));
                            EventList.addAll(eventsByDate.get(eventDate));
                        }
                    } else {
                        EventList.add(new ScheduleListItem.SectionHeader("No events this week"));

                    }

                    adapter = new SchedulerAdapter(EventList);
                    schedualer.eventRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    schedualer.eventRecyclerView.setAdapter(adapter);
                    schedualer.eventRecyclerView.setVisibility(View.VISIBLE);
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