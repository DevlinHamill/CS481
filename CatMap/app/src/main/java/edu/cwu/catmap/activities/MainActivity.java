package edu.cwu.catmap.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.FirebaseApp;

import org.json.JSONArray;
import org.json.JSONObject;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.Manifest;
import edu.cwu.catmap.R;
import edu.cwu.catmap.core.ScheduleListItem;
import edu.cwu.catmap.databinding.ActivityMainBinding;
import edu.cwu.catmap.adapters.DailyEventAdapter;
import edu.cwu.catmap.utilities.LocationPermissionHelper;
import edu.cwu.catmap.utilities.EventUtils;
import com.google.android.gms.location.FusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private ActivityMainBinding hub;
    private RecyclerView eventRecyclerView;
    private DailyEventAdapter dailyEventAdapter;

    private FusedLocationProviderClient fusedLocationClient; // Declare the variable
    private Marker userMarker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        super.onCreate(savedInstanceState);
        hub = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(hub.getRoot());

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions on startup using the utility class
        if (!LocationPermissionHelper.hasLocationPermissions(this)) {
            LocationPermissionHelper.requestLocationPermissions(this);
        } else {
            // Permissions are already granted, start location updates
            startLocationUpdates();
        }

        eventRecyclerView = findViewById(R.id.eventRecyclerView);

        onclick();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (LocationPermissionHelper.handlePermissionResult(requestCode, grantResults)) {
            // Permissions granted, start location updates
            startLocationUpdates();
        } else {
            // Permissions denied, show a message to the user
            Toast.makeText(this, "Location permissions are required to show your location on the map.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        gMap = googleMap;
        LatLng location = new LatLng(47.0076653, -120.5366559);

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        gMap.setMinZoomPreference(15);


        //getDirections(location,new LatLng(46.999784885243926, -120.5448810745124));
        //getDirections(new LatLng(47.01407056573746, -120.51994167974972),location);

        //outer bound of polygon (whole globe)
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
        String apiUrl = "https://routes.googleapis.com/directions/v2:computeRoutes";

        // Create JSON request body
        JSONObject data = new JSONObject();
        try {
            // Add origin
            JSONObject originJson = new JSONObject();
            JSONObject originLocation = new JSONObject();
            JSONObject originLatLng = new JSONObject();
            originLatLng.put("latitude", origin.latitude);
            originLatLng.put("longitude", origin.longitude);
            originLocation.put("latLng", originLatLng);
            originJson.put("location", originLocation);
            data.put("origin", originJson);

            // Add destination
            JSONObject destinationJson = new JSONObject();
            JSONObject destinationLocation = new JSONObject();
            JSONObject destinationLatLng = new JSONObject();
            destinationLatLng.put("latitude", destination.latitude);
            destinationLatLng.put("longitude", destination.longitude);
            destinationLocation.put("latLng", destinationLatLng);
            destinationJson.put("location", destinationLocation);
            data.put("destination", destinationJson);

            // Add travel mode
            data.put("travelMode", "WALK");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send the HTTP request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, apiUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONArray routes = jsonResponse.getJSONArray("routes");
                            JSONObject route = routes.getJSONObject(0);

                            // Extract polyline
                            JSONObject polyline = route.getJSONObject("polyline");
                            String encodedPolyline = polyline.getString("encodedPolyline");

                            // Decode and draw the route
                            List<LatLng> decodedPath = decodePolyline(encodedPolyline);
                            PolylineOptions polylineOptions = new PolylineOptions()
                                    .addAll(decodedPath)
                                    .width(10)
                                    .color(Color.BLUE);
                            gMap.addPolyline(polylineOptions);

                            // Extract duration
                            String duration = route.getString("duration");

                            // Convert duration to minutes
                            String durationInMinutes = convertDurationToMinutes(duration);

                            // Add a visible marker at the destination
                            MarkerOptions markerOptions = new MarkerOptions()
                                    .position(destination) // Place the marker at the destination
                                    .title("Travel Time")
                                    .snippet("Duration: " + durationInMinutes)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)); // Customize marker icon if needed
                            Marker destinationMarker = gMap.addMarker(markerOptions);

                            // Show the InfoWindow by default
                            destinationMarker.showInfoWindow();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                headers.put("X-Goog-FieldMask", "routes.polyline,routes.duration"); // Include duration
                return headers;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    // Helper method to convert duration to minutes
    private String convertDurationToMinutes(String duration) {
        try {
            // Remove the "s" from the duration string (e.g., "900s" -> "900")
            String durationValue = duration.replace("s", "");

            // Convert the duration to seconds
            long seconds = Long.parseLong(durationValue);

            // Convert seconds to minutes
            long minutes = seconds / 60;

            // Format the duration as "X mins"
            return minutes + " mins";
        } catch (Exception e) {
            e.printStackTrace();
            return "N/A"; // Return a default value if parsing fails
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

    // Method to set up the RecyclerView
    private void setupEventRecyclerView() {
        // Set up the layout manager for horizontal scrolling
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        eventRecyclerView.setLayoutManager(layoutManager);

        // Use EventUtils to populate the RecyclerView
        long currentDateMillis = System.currentTimeMillis(); // Use the current date or any selected date
        EventUtils.populateEvents(this, eventRecyclerView, currentDateMillis);
    }
    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request location updates
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            // Add or update the user's location marker
                            updateUserLocationMarker(new LatLng(location.getLatitude(), location.getLongitude()));
                        }
                    });
        }
    }
    private void updateUserLocationMarker(LatLng location) {
        if (userMarker == null) {
            // Add a new marker for the user's location
            userMarker = gMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title("you")
                    .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getBitmapFromDrawable(this,R.drawable.logo_pin)))
            );
        } else {
            // Update the existing marker's position
            userMarker.setPosition(location);
        }
        // Move the camera to the user's location
        //gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
    }

}