package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    /**
     * holds the binding for the schedualer
     */
    private ActivitySchedualerBinding schedualer;

    /**
     * adapter for the recycle view
     */
    private SchedulerAdapter adapter;
    /**
     * holds the recycle view items
     */
    private List<ScheduleListItem> EventList;

    /**
     * stores the selected date milli secounds from the calender
     */
    private long selectedDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        schedualer = ActivitySchedualerBinding.inflate(getLayoutInflater());
        setContentView(schedualer.getRoot());


        Calendar calendar = Calendar.getInstance();
        selectedDateMillis = calendar.getTimeInMillis();

        onclick();

        EventList = new ArrayList<>();
        populateEvents();
        fillFABColor();

    }

    private void fillFABColor() {
        //change button fill colors to match the theme
        ArrayList<FloatingActionButton> fabList = new ArrayList<>();
        TypedValue typedValue = new TypedValue();
        this.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

        int color = typedValue.data;

        fabList.add(schedualer.AddMeeting);

        for(FloatingActionButton button : fabList) {
            Drawable drawable = button.getDrawable();

            if(drawable != null) {
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            }
        }
    }

    /**
     * populates the day current events and classes
     */
    private void populateEvents() {
        EventList.clear();
        /**
         * grabs the current formated date from the calender
         */
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        /**
         * creates a calender object to retrieve the current selected date later on
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);
        /**
         * grabs the selected based on the current day of the week
         */
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        /**
         * formates the date to the proper string formate being saved on the firebase
         */
        String formattedDate = sdf.format(calendar.getTime());
        /**
         * creates the firebase object to pinpoint the collection we are trying to read later on
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /**
         * creates refrence to the firebase
         */
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", formattedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    /**
                     * used to check if it has events later on
                     */
                    boolean hasEvents = false;
                    List<ScheduleListItem.Event> tempEventList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        /**
                         * stores the event name aka the title from the firebase document
                         */
                        String eventName = document.getString("Event_Title");
                        /**
                         * stores the event time from the firebase document
                         */
                        String eventTime = document.getString("Event_Time");
                        /**
                         * stores the event creation date from the firebase document
                         */
                        String eventDate = document.getString("Event_Date");
                        /**
                         * stores the building name from the firebase document
                         */
                        String buildingName = document.getString("Building_Name");
                        /**
                         * stores the users color prefrence for the current event
                         */
                        String colorPreference = document.getString("Color_Preference");
                        /**
                         * stores the event type from the firebase document
                         */
                        String eventType = document.getString("Event_Type");
                        /**
                         * stores the event id number from the firebase document
                         */
                        String id = document.getId();
                        /**
                         * stores the room number from the firebase document
                         */
                        String roomNum = document.getString("Room_Number");
                        /**
                         * stores the repeated event to check which days have a repeated event day
                         */
                        String repeatedEvent = document.getString("Repeated_Events");
                        /**
                         * stores the repeated event condition that tells weither it is a repeating event or not
                         */
                        String repeatingCondition = document.getString("Repeating_Condition");
                        /**
                         * stores the end date for the current event to see when the event ends.
                         */
                        String End_Date = document.getString("End_Date");

                        /**
                         * used to check if the current viewed event is repeating
                         */
                        boolean isRepeatingEvent = false;

                        if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                            try {
                                repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                /**
                                 * Splits the repeated events string into an actual array to see if
                                 * an the current selected day is an actual repeating event
                                 */
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
                            /**
                             * stores the events that will stored acrossed views
                             */
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

                    /**
                     * Gets the formate of the time that will be used to sort based on time
                     */
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    /**
                     * Sorts event one with event two
                     */
                    Collections.sort(tempEventList, (e1, e2) -> {
                        try {
                            /**
                             * Saves the first event time and compares the time on both events saving it as date objects
                             */
                            Date time1 = timeFormat.parse(e1.getTime());
                            /**
                             * Saves the seccound event time and compares the time on both events saving it as date objects
                             */
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

    /**
     *  Creates a selected date String set to null that will used to pass the currently selected date on the calender
     */
    private String selectedDate = null;

    /**
     * declares the onclick listeners
     */
    private void onclick() {
        schedualer.AddMeeting.setOnClickListener(v -> {
            // If no date is selected, default to today's date dynamically
            if (selectedDate == null) {
                selectedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(new Date());
            }

            /**
             * creates a popup at the bottom when the addmeeting button is pressed
             */
            AddMeetingBottomSheet bottomSheet = AddMeetingBottomSheet.newInstance(selectedDate);
            bottomSheet.show(getSupportFragmentManager(), "AddMeetingBottomSheet");
        });

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
            /**
             * Will be used to see what the current calender
             */
            Calendar calendar = Calendar.getInstance();
            /**
             * sets the calender to the new date
             */
            calendar.set(year, month, dayOfMonth);

            /**
             * updates the new selected millisecounds from the calender
             */
            selectedDateMillis = calendar.getTimeInMillis();
            /**
             * formates the selected date string
             */
            selectedDate = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(calendar.getTime());
            populateEvents();
        });

        schedualer.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    /**
     * pulls suggestions for the week
     */
    private void grabSuggestions() {
        /**
         * creates a calender object to retrieve the current selected date info later on
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        /**
         * retrieves the start of the week info
         */
        long startOfWeekMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        /**
         * retrieves the end of the week info
         */
        long endOfWeekMillis = calendar.getTimeInMillis();

        /**
         * formates the string to the proper date
         */
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        /**
         * properly formates the start of the week
         */
        String startOfWeek = sdf.format(new Date(startOfWeekMillis));
        /**
         * properly formates the end of the week
         */
        String endOfWeek = sdf.format(new Date(endOfWeekMillis));


        /**
         * creates a firebase object
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /**
         * creates a target reference to access the subcollection collection information from the firebase
         */
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", startOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        /**
                         * Holds the event documents that will be passed through the recycle later on
                         */
                        Map<String, List<ScheduleListItem.Event>> events = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            /**
                             * contains the event title of the currently viewed document
                             */
                            String eventName = document.getString("Event_Title");
                            /**
                             * contains the event time from the currently viewed document
                             */
                            String eventTime = document.getString("Event_Time");
                            /**
                             * contains the current creation date/ start date of the event
                             */
                            String eventDate = document.getString("Event_Date");
                            /**
                             * holds the building name of the event
                             */
                            String buildingName = document.getString("Building_Name");
                            /**
                             * Holds the color prefrences for the current event
                             */
                            String colorPreference = document.getString("Color_Preference");
                            /**
                             * holds the event type of the document
                             */
                            String eventType = document.getString("Event_Type");
                            /**
                             * contains the event ID of the document
                             */
                            String id = document.getId();
                            /**
                             * contains the room number from the document
                             */
                            String roomNum = document.getString("Room_Number");
                            /**
                             * contains an array as a string of all repeated events for this current event sunday - saturday
                             */
                            String repeatedEvent = document.getString("Repeated_Events");
                            /**
                             * contains the repeating event condition that will tell if the event is repeating or not
                             */
                            String repeatingCondition = document.getString("Repeating_Condition");
                            /**
                             * holds the end date of the event
                             */
                            String End_Date = document.getString("End_Date");

                            /**
                             * contains all info that will stored acrossed views
                             */
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

                            /**
                             * contains a list of all the repeating event dates to be displayed as a header later on
                             */
                            List<String> repeatingDates = new ArrayList<>();

                            if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                                try {
                                    repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                    /**
                                     * splits the string up to view the actual repeating event days.
                                     */
                                    String[] daysArray = repeatedEvent.split(",\\s*");

                                    if (daysArray.length == 7) {
                                        /**
                                         * creates a calender to check each day of the week starting from sunday
                                         */
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
                                events.putIfAbsent(eventDate, new ArrayList<>());

                            }


                            for (String repeatDate : repeatingDates) {
                                events.putIfAbsent(repeatDate, new ArrayList<>());

                            }
                        }

                        /**
                         * formates the time properly to the correct String formate
                         */
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        for (List<ScheduleListItem.Event> eventList : events.values()) {
                            Collections.sort(eventList, (e1, e2) -> {
                                try {
                                    /**
                                     * gets the time from event 1
                                     */
                                    Date time1 = timeFormat.parse(e1.getTime());
                                    /**
                                     * gets the time from event 2
                                     */
                                    Date time2 = timeFormat.parse(e2.getTime());
                                    return time1.compareTo(time2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });
                        }

                        for (String eventDate : new TreeSet<>(events.keySet())) {
                            EventList.add(new ScheduleListItem.SectionHeader(eventDate));
                            EventList.addAll(events.get(eventDate));
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


    /**
     * gets the week view from the calender
     */
    private void weekView() {
        EventList.clear();
        schedualer.calendarView.setVisibility(View.GONE);
        schedualer.AddMeeting.setVisibility(View.GONE);

        /**
         * recreates the calender to retrieve the current day data
         */
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(selectedDateMillis);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        /**
         * retrieves the start of the week
         */
        long startOfWeekMillis = calendar.getTimeInMillis();
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        /**
         * retrieves the end of the week
         */
        long endOfWeekMillis = calendar.getTimeInMillis();

        /**
         * formates the date properly
         */
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        /**
         * formates the start of the week properly
         */
        String startOfWeek = sdf.format(new Date(startOfWeekMillis));
        /**
         * starts the end of the week properly
         */
        String endOfWeek = sdf.format(new Date(endOfWeekMillis));

        /**
         * creates the firebase object
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        /**
         * creates a refrence to the subcollection
         */
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", startOfWeek)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        /**
                         * Holds the items that will be displayed on the recycle view
                         */
                        Map<String, List<ScheduleListItem.Event>> events = new HashMap<>();

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            /**
                             * Holds the event title
                             */
                            String eventName = document.getString("Event_Title");
                            /**
                             * holds the event time for the current event
                             */
                            String eventTime = document.getString("Event_Time");
                            /**
                             * holds the current time for the event
                             */
                            String eventDate = document.getString("Event_Date");
                            /**
                             * holds the building name that will be used for navigation
                             */
                            String buildingName = document.getString("Building_Name");
                            /**
                             * holds the users color prefrence for the specific event
                             */
                            String colorPreference = document.getString("Color_Preference");
                            /**
                             * holds the event type info
                             */
                            String eventType = document.getString("Event_Type");
                            /**
                             * holds the event id information
                             */
                            String id = document.getId();
                            /**
                             * holds the room number for the event
                             */
                            String roomNum = document.getString("Room_Number");
                            /**
                             * holds the list of repeated events as a string formated as an array
                             */
                            String repeatedEvent = document.getString("Repeated_Events");
                            /**
                             * grabs the repeating condition from the document to see if the even is actually repeating or not
                             */
                            String repeatingCondition = document.getString("Repeating_Condition");
                            /**
                             * retrieves the end date from the document
                             */
                            String End_Date = document.getString("Repeating_Condition");

                            /**
                             * Attaches the data to a temp hash map to be sorted
                             */
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

                            /**
                             * contains all repeating event dates
                             */
                            List<String> repeatingDates = new ArrayList<>();

                            if ("true".equalsIgnoreCase(repeatingCondition) && repeatedEvent != null) {
                                try {
                                    repeatedEvent = repeatedEvent.replace("[", "").replace("]", "").trim();
                                    /**
                                     * converts the repeating event string to an actual array and check if their is a 1 in the proper locaiton
                                     */
                                    String[] daysArray = repeatedEvent.split(",\\s*");

                                    if (daysArray.length == 7) {
                                        /**
                                         * creates a calender view starting at the starting of the week sunday - saturday
                                         */
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
                                events.putIfAbsent(eventDate, new ArrayList<>());
                                if (!repeatingDates.contains(eventDate)) {
                                    events.get(eventDate).add(new ScheduleListItem.Event(eventName, eventTime, map));
                                }
                            }


                            for (String repeatDate : repeatingDates) {
                                events.putIfAbsent(repeatDate, new ArrayList<>());
                                events.get(repeatDate).add(new ScheduleListItem.Event(eventName, eventTime, map));
                            }
                        }

                        /**
                         * gets the formated string for the time.
                         */
                        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                        for (List<ScheduleListItem.Event> eventList : events.values()) {
                            Collections.sort(eventList, (e1, e2) -> {
                                try {
                                    /**
                                     * gets the event 1 time as a date object
                                     */
                                    Date time1 = timeFormat.parse(e1.getTime());
                                    /**
                                     * gets the event 2 time as a date object
                                     */
                                    Date time2 = timeFormat.parse(e2.getTime());
                                    return time1.compareTo(time2);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            });
                        }


                        EventList.clear();
                        for (String eventDate : new TreeSet<>(events.keySet())) {
                            EventList.add(new ScheduleListItem.SectionHeader(eventDate));
                            EventList.addAll(events.get(eventDate));
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