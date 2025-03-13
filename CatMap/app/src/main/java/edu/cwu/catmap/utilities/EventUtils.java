package edu.cwu.catmap.utils;

import android.content.Context;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

import edu.cwu.catmap.adapters.DailyEventAdapter;
import edu.cwu.catmap.core.ScheduleListItem;

public class EventUtils {
    public static void populateEvents(Context context, RecyclerView recyclerView, long selectedDateMillis) {
        List<ScheduleListItem> itemList = new ArrayList<>(); // Store ScheduleListItems
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
                        String endDate = document.getString("End_Date");

                        boolean isRepeatingEvent = false;

                        if ("true".equalsIgnoreCase(repeatingCondition)) {
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
                            map.put("End_Date", endDate);

                            if (eventName != null && eventTime != null) {
                                // Create a new ScheduleListItem for the event and add it to the list
                                ScheduleListItem eventItem = new ScheduleListItem.Event(eventName, eventTime, map);
                                itemList.add(eventItem);
                            }
                        }
                    }

                    // Sort items by event time (if applicable)
                    SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                    Collections.sort(itemList, (item1, item2) -> {
                        if (item1 instanceof ScheduleListItem.Event && item2 instanceof ScheduleListItem.Event) {
                            ScheduleListItem.Event event1 = (ScheduleListItem.Event) item1;
                            ScheduleListItem.Event event2 = (ScheduleListItem.Event) item2;
                            try {
                                Date time1 = timeFormat.parse(event1.getTime());
                                Date time2 = timeFormat.parse(event2.getTime());
                                return time1.compareTo(time2);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                return 0;
                            }
                        }
                        return 0; // If they aren't both Event objects, don't sort
                    });

                    // Update the RecyclerView adapter with the list of ScheduleListItem
                    DailyEventAdapter adapter = new DailyEventAdapter(itemList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to load daily events", Toast.LENGTH_SHORT).show();
                });
    }
}
