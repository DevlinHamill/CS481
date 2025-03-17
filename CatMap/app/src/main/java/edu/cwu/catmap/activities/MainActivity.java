package edu.cwu.catmap.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;

import org.json.JSONArray;
import org.json.JSONObject;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import android.Manifest;
import edu.cwu.catmap.R;
import edu.cwu.catmap.core.Location;
import edu.cwu.catmap.core.ScheduleListItem;
import edu.cwu.catmap.databinding.ActivityMainBinding;
import edu.cwu.catmap.adapters.DailyEventAdapter;
import edu.cwu.catmap.manager.LocationsManager;
import edu.cwu.catmap.utilities.LocationPermissionHelper;
import edu.cwu.catmap.utils.EventUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private ActivityMainBinding hub;
    private RecyclerView eventRecyclerView;
    private DailyEventAdapter dailyEventAdapter;

    private FusedLocationProviderClient fusedLocationClient;
    private Marker userMarker;

    private LatLng destination;
    private Handler handler = new Handler();
    private Runnable refreshTask;
    private static final long REFRESH_INTERVAL = 6000; // 6 seconds (adjust as needed)

    //private PolylineOptions oldPolylineOptions;
    private Polyline oldPolyline;
    //private MarkerOptions oldDestinationMarkerOptions;
    private Marker oldDestinationMarker;
    private PolylineOptions newPolylineOptions;
    private Polyline newPolyline;
    private MarkerOptions newDestinationMarkerOptions;
    private Marker newDestinationMarker;
    private String EtaText;
    private boolean isDirectionsRequestInProgress = false; //flag to prevent overlapping requests


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        hub = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(hub.getRoot());
        //destination = new LatLng(47.0076653, -120.5366559); //destination override

        refreshTask = new Runnable() {
            @Override
            public void run() {
                refreshEvents();
                refreshDirections(); // Call the refresher method
                displayRouteOnMap();
                handler.postDelayed(this, REFRESH_INTERVAL); // Schedule the task again
            }
        };
        handler.postDelayed(refreshTask, REFRESH_INTERVAL);

        //initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //request location permissions on startup using utility class
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            LocationPermissionHelper.requestLocationPermissions(this);
        } else {
            //permissions are already granted start location updates
            startLocationUpdates();
        }

        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        onclick();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        dailyEventAdapter = new DailyEventAdapter(new ArrayList<>());
        eventRecyclerView.setAdapter(dailyEventAdapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshEvents();
        refreshDirections();
        displayRouteOnMap();
    }

    //handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (LocationPermissionHelper.handlePermissionResult(requestCode, grantResults)) {
            //permissions granted start location updates
            startLocationUpdates();
        } else {
            //permissions denied
            //Toast.makeText(this, "Location permissions are required to show your location on the map.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        gMap = googleMap;
        LatLng location = new LatLng(47.0076653, -120.5366559);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        gMap.setMinZoomPreference(15);

        navigateToNextEvent();
        getNextUpcomingEvent(new OnNextUpcomingEventListener() {
            @Override
            public void onNextUpcomingEvent(Map<String, String> eventDetails) {
                if (eventDetails != null) {
                    String buildingName = eventDetails.get("Building_Name");
                    String roomNumber = eventDetails.get("Room_Number");
                    String eventTitle = eventDetails.get("Event_Title");
                    String eventTime = eventDetails.get("Event_Time");

                    //use the event details (e.g., set a waypoint)
                    //Toast.makeText(MainActivity.this, "Next Event: " + eventTitle + " at " + buildingName + ", Room " + roomNumber, Toast.LENGTH_SHORT).show();
                } else {
                    //Toast.makeText(MainActivity.this, "No upcoming events found", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //getDirections(location,new LatLng(46.999784885243926, -120.5448810745124));
        //getDirections(new LatLng(47.01407056573746, -120.51994167974972),location);

        //outer bound of polygon (not whole globe)
        LatLng[] outerBounds = {
                new LatLng(27.0076653, -140.5366559),
                new LatLng(27.0076653, -100.5366559),
                new LatLng(67.0076653, -100.5366559),
                new LatLng(67.0076653, -140.5366559),
        };
        //campus polygon
        LatLng[] campusBounds = {
                new LatLng(47.01407056573746, -120.51994167974972),
                new LatLng(47.01408304551457, -120.52262955204823),
                new LatLng(47.01279391692807, -120.52425364288959),
                new LatLng(47.011941099692415, -120.52550440049475),
                new LatLng(47.01079458969304, -120.52582779530084),
                new LatLng(47.01066583712106, -120.5288502037981),
                new LatLng(47.01051100626275, -120.53164600654705),
                new LatLng(47.01047229847818, -120.5396005618308),
                new LatLng(47.01049572681534, -120.54484041122356),
                new LatLng(47.00942383682609, -120.54490208586678),
                new LatLng(47.00860601682181, -120.5434794545431),
                new LatLng(47.00674009324229, -120.54347410205268),
                new LatLng(47.006704911171276, -120.54486896176985),
                new LatLng(47.00616111163828, -120.5448657967118),
                new LatLng(47.00605013098817, -120.54724012352013),
                new LatLng(47.004861619334235, -120.54717389210595),
                new LatLng(47.00429047654235, -120.54595828473344),
                new LatLng(47.00349049362342, -120.54662394137064),
                new LatLng(47.00332533280491, -120.54662970129527),
                new LatLng(47.003345867333195, -120.54528856709489),
                new LatLng(47.002799764728984, -120.54527056027807),
                new LatLng(47.00287539341067, -120.54339282495086),
                new LatLng(47.00236647447496, -120.54333198405641),
                new LatLng(47.00233823224595, -120.54425178157626),
                new LatLng(47.001823038426856, -120.54422671934262),
                new LatLng(47.00185478036978, -120.54329583637754),
                new LatLng(47.000788808829505, -120.54328047002583),
                new LatLng(47.000782346759536, -120.54398943276557),
                new LatLng(47.000444735444724, -120.54396775676832),
                new LatLng(47.00044392060786, -120.54412188444245),
                new LatLng(47.000288501613696, -120.54412195874754),
                new LatLng(47.000270420082174, -120.54459062372904),
                new LatLng(46.99979964181349, -120.54455844906596),
                new LatLng(46.999784885243926, -120.5448810745124),
                new LatLng(46.99861311639834, -120.54483024862996),
                new LatLng(46.99865186439213, -120.54323582399306),
                new LatLng(46.99937602102182, -120.54329690063364),
                new LatLng(46.99943991105609, -120.5421884737499),
                new LatLng(46.99864988059308, -120.54215632986747),
                new LatLng(46.9987233525702, -120.53917605441777),
                new LatLng(46.999761293382306, -120.53922789652587),
                new LatLng(46.999805111810176, -120.53794116424261),
                new LatLng(46.99984286097891, -120.5375749985139),
                new LatLng(46.9999227149018, -120.53725353906603),
                new LatLng(47.000020080200315, -120.53696528113892),
                new LatLng(47.00016091293502, -120.53667149700775),
                new LatLng(47.00049861636629, -120.53613678564281),
                new LatLng(47.000752693497354, -120.53570036718709),
                new LatLng(47.00135615883033, -120.53466708249655),
                new LatLng(47.00183320863204, -120.53379406347356),
                new LatLng(47.001907252242525, -120.53353647014117),
                new LatLng(47.003013874157446, -120.53360386145755),
                new LatLng(47.003046650455765, -120.53166095505163),
                new LatLng(47.00618269489664, -120.53178236436707),
                new LatLng(47.00626683115237, -120.53017840346078),
                new LatLng(47.00640035145624, -120.5299960132666),
                new LatLng(47.00656679410747, -120.52968755926172),
                new LatLng(47.00709787980966, -120.52868386278034),
                new LatLng(47.00739662482033, -120.52828518308418),
                new LatLng(47.0080879590786, -120.52767633885593),
                new LatLng(47.00834222654154, -120.52759315178061),
                new LatLng(47.00873359772935, -120.52765489108211),
                new LatLng(47.00973827734657, -120.52704215801924),
                new LatLng(47.010047186710906, -120.52675842443178),
                new LatLng(47.010504317834624, -120.52669156078822),
                new LatLng(47.0106304470581, -120.52288676679845)
        };

        //cut hole in world polygon
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.add(outerBounds);
        polygonOptions.addHole(java.util.Arrays.asList(campusBounds));
        polygonOptions.strokeColor(Color.GRAY);
        polygonOptions.strokeWidth(0);
        polygonOptions.fillColor(Color.argb(250,200,200,200));

        Polygon campusHighlight = gMap.addPolygon(polygonOptions);

        setupEventRecyclerView();
    }

    public void onclick(){
        hub.MiscButton.setOnClickListener( v->
                miscclick()
        );

        hub.profileButton.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Profile.class))
        );
        hub.SchedulerButton.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SchedualerGUI.class))
        );
        hub.LocationsButton.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), LocationsActivity.class))
        );
        hub.settingsButton.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class))
        );


    }
    public void miscclick(){
        if(hub.misc.getVisibility() == View.INVISIBLE){
            hub.misc.setVisibility(View.VISIBLE);
            hub.profileButton.setVisibility(View.VISIBLE);
            hub.settingsButton.setVisibility(View.VISIBLE);
            hub.LocationsButton.setVisibility(View.VISIBLE);
            hub.SchedulerButton.setVisibility(View.VISIBLE);
        }else{
            hub.misc.setVisibility(View.INVISIBLE);
            hub.profileButton.setVisibility(View.INVISIBLE);
            hub.settingsButton.setVisibility(View.INVISIBLE);
            hub.LocationsButton.setVisibility(View.INVISIBLE);
            hub.SchedulerButton.setVisibility(View.INVISIBLE);
        }
    }

    public void getDirections(LatLng origin, LatLng destination) {
        //prevent overlapping requests
        if (isDirectionsRequestInProgress) {
            return;
        }

        //set the flag to true
        isDirectionsRequestInProgress = true;

        String apiUrl = "https://routes.googleapis.com/directions/v2:computeRoutes";

        //JSON request body
        JSONObject data = new JSONObject();
        try {
            //origin
            JSONObject originJson = new JSONObject();
            JSONObject originLocation = new JSONObject();
            JSONObject originLatLng = new JSONObject();
            originLatLng.put("latitude", origin.latitude);
            originLatLng.put("longitude", origin.longitude);
            originLocation.put("latLng", originLatLng);
            originJson.put("location", originLocation);
            data.put("origin", originJson);

            //destination
            JSONObject destinationJson = new JSONObject();
            JSONObject destinationLocation = new JSONObject();
            JSONObject destinationLatLng = new JSONObject();
            destinationLatLng.put("latitude", destination.latitude);
            destinationLatLng.put("longitude", destination.longitude);
            destinationLocation.put("latLng", destinationLatLng);
            destinationJson.put("location", destinationLocation);
            data.put("destination", destinationJson);

            //travel mode always "WALK"
            data.put("travelMode", "WALK");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //send the HTTP request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        isDirectionsRequestInProgress = false; // Reset the flag
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray routes = jsonResponse.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);

                            //extract polyline
                            JSONObject polyline = route.getJSONObject("polyline");
                            String encodedPolyline = polyline.getString("encodedPolyline");

                            //decode the polyline
                            List<LatLng> decodedPath = decodePolyline(encodedPolyline);

                            //store the polyline options
                            newPolylineOptions = new PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(10)
                                    .color(Color.BLUE);

                            //extract duration
                            String duration = route.getString("duration");

                            //convert duration to minutes
                            String durationInMinutes = convertDurationToMinutes(duration);

                            //store the destination marker options and ETA text
                            newDestinationMarkerOptions = new MarkerOptions()
                                    .position(destination)
                                    .title("Travel Time")
                                    .snippet("Duration: " + durationInMinutes)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            EtaText = durationInMinutes;

                            //display the route on the map
                            displayRouteOnMap();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        isDirectionsRequestInProgress = false; // Reset the flag
                        if (error.networkResponse != null) {
                            Log.e("API Error", "Status code: " + error.networkResponse.statusCode);
                            Log.e("API Error", "Response data: " + new String(error.networkResponse.data));
                        }
                        error.printStackTrace();
                    }
                }) {
            @Override
            public byte[] getBody() {
                return data.toString().getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Goog-Api-Key", getString(R.string.google_maps_key));
                headers.put("X-Goog-FieldMask", "routes.polyline,routes.duration");
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);

    }

    //method to convert duration to minutes
    private String convertDurationToMinutes(String duration) {
        try {
            //remove "s" from duration string
            String durationValue = duration.replace("s", "");

            //convert duration to seconds
            long seconds = Long.parseLong(durationValue);

            //convert seconds to minutes
            long minutes = seconds / 60;

            //format the duration as "X mins"
            return minutes + " mins";
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A";
        }
    }

    //method to update TextView position
    private void updateTextViewPosition(TextView textView, LatLng position) {
        Projection projection = gMap.getProjection();
        Point point = projection.toScreenLocation(position);

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) textView.getLayoutParams();
        params.leftMargin = point.x;
        params.topMargin = point.y;
        textView.setLayoutParams(params);
    }
    public List<LatLng> decodePolyline(String encoded) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            polyline.add(position);
        }
        return polyline;
    }
    public void drawRouteOnMap(List<LatLng> decodedPath) {
        PolylineOptions polylineOptions = new PolylineOptions().addAll(decodedPath).width(10).color(Color.GREEN);
        gMap.addPolyline(polylineOptions);
    }

    //method to set up the RecyclerView
    private void setupEventRecyclerView() {
        //set up the layout manager for horizontal scrolling
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        eventRecyclerView.setLayoutManager(layoutManager);

        //use EventUtils to populate the RecyclerView
        long currentDateMillis = System.currentTimeMillis();
        EventUtils.populateEvents(this, eventRecyclerView, currentDateMillis, dailyEventAdapter);
    }
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //request location updates
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            //update user location marker
                            updateUserLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    });
        }
    }
    private void updateUserLocationMarker(LatLng location) {
        if (userMarker == null) {
            //add marker for user location
            userMarker = gMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("you")
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getBitmapFromDrawable(this,R.drawable.logo_pin)))
            );
        } else {
            //update marker position
            userMarker.setPosition(location);
        }
        //move the camera to the user's location
        //gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

    public interface OnNextUpcomingEventListener {
        void onNextUpcomingEvent(Map<String, String> eventDetails);
    }


    public void getNextUpcomingEvent(OnNextUpcomingEventListener listener) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        long selectedDateMillis = System.currentTimeMillis();
        calendar.setTimeInMillis(selectedDateMillis);
        int selectedDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        String formattedDate = sdf.format(calendar.getTime());

        //get the current date and time
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeMillis = currentCalendar.getTimeInMillis();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("user_collection")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Events");

        eventsRef.whereGreaterThanOrEqualTo("End_Date", formattedDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, String> nextUpcomingEventDetails = null;
                    long nextEventTimeMillis = Long.MAX_VALUE;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String eventName = document.getString("Event_Title");
                        String eventTime = document.getString("Event_Time");
                        String eventDate = document.getString("Event_Date");
                        String buildingName = document.getString("Building_Name");
                        String roomNum = document.getString("Room_Number");
                        String repeatedEvent = document.getString("Repeated_Events");
                        String repeatingCondition = document.getString("Repeating_Condition");

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
                            //combine event date and time
                            String eventDateTimeString = eventDate + " " + eventTime;

                            try {
                                //parse the event date and time
                                Date eventDateTime = timeFormat.parse(eventDateTimeString);
                                Calendar eventCalendar = Calendar.getInstance();
                                eventCalendar.setTime(eventDateTime);

                                //compare the event date and time with the current
                                if (eventCalendar.getTimeInMillis() >= currentTimeMillis) {
                                    //Check if this is the next event
                                    if (eventCalendar.getTimeInMillis() < nextEventTimeMillis) {
                                        nextEventTimeMillis = eventCalendar.getTimeInMillis();
                                        nextUpcomingEventDetails = new HashMap<>();
                                        nextUpcomingEventDetails.put("Event_Title", eventName);
                                        nextUpcomingEventDetails.put("Event_Time", eventTime);
                                        nextUpcomingEventDetails.put("Event_Date", eventDate);
                                        nextUpcomingEventDetails.put("Building_Name", buildingName);
                                        nextUpcomingEventDetails.put("Room_Number", roomNum);
                                    }
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    //notify the listener
                    if (listener != null) {
                        listener.onNextUpcomingEvent(nextUpcomingEventDetails);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load events", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onNextUpcomingEvent(null); // Notify the listener of the failure
                    }
                });
    }

    public void navigateToNextEvent() {

        getNextUpcomingEvent(new OnNextUpcomingEventListener() {
            @Override
            public void onNextUpcomingEvent(Map<String, String> eventDetails) {
                if (eventDetails != null) {
                    String buildingName = eventDetails.get("Building_Name");

                    // Load locations from JSON
                    LocationsManager locationsManager = LocationsManager.getInstance(MainActivity.this);
                    Location eventLocation = locationsManager.getLocation(buildingName);

                    if (eventLocation != null) {
                        String mainEntranceCoordinate = eventLocation.getMainEntranceCoordinate();
                        if (mainEntranceCoordinate != null && !mainEntranceCoordinate.isEmpty()) {
                            // Parse the mainEntranceCoordinate into a LatLng object
                            String[] latLngParts = mainEntranceCoordinate.split(",");
                            double lat = Double.parseDouble(latLngParts[0]);
                            double lng = Double.parseDouble(latLngParts[1]);
                            LatLng temDest = new LatLng(lat, lng);

                            // Get user's current location
                            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                fusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(MainActivity.this, location -> {
                                            if (location != null) {
                                                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                                // Call getDirections() with the user's location and the destination
                                                getDirections(userLocation, temDest);
                                            } else {
                                                //Toast.makeText(MainActivity.this, "Unable to get your current location.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                //Toast.makeText(MainActivity.this, "Location permission is required.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            //Toast.makeText(MainActivity.this, "No entrance coordinates found for " + buildingName, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //Toast.makeText(MainActivity.this, "Building not found: " + buildingName, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(MainActivity.this, "No upcoming events found.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void refreshEvents() {
        long currentDateMillis = System.currentTimeMillis();
        EventUtils.populateEvents(this, eventRecyclerView, currentDateMillis, dailyEventAdapter);
    }

    private void refreshDirections() {
        //update the user's location marker
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            updateUserLocationMarker(userLocation);

                            //check if destination is null
                            if (destination == null) {
                                //if destination is null, navigate to the next event
                                navigateToNextEvent();
                            } else {
                                //if destination is not null, get directions to the destination
                                getDirections(userLocation, destination);
                            }
                        } else {
                            //Toast.makeText(this, "Unable to get your current location.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            //Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayRouteOnMap() {
        //ensure the map is ready
        if (gMap == null) {
            return;
        }

        //remove old polyline and marker
        if (oldPolyline != null) {
            oldPolyline.remove();
        }
        if (oldDestinationMarker != null) {
            oldDestinationMarker.remove();
        }

        //add new polyline and marker
        if (newPolylineOptions != null) {
            newPolyline = gMap.addPolyline(newPolylineOptions);
        }

        if (newDestinationMarkerOptions != null) {
            newDestinationMarker = gMap.addMarker(newDestinationMarkerOptions); // Add the new marker
            newDestinationMarker.showInfoWindow();
        }

        //step 3: Update references
        oldPolyline = newPolyline;
        oldDestinationMarker = newDestinationMarker;


        //step 4: Reset new references
        newPolyline = null;
        newDestinationMarker = null;
        newPolylineOptions = null;
        newDestinationMarkerOptions = null;
    }

}