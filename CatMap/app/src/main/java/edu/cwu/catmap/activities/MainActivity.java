package edu.cwu.catmap.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import edu.cwu.catmap.R;
import edu.cwu.catmap.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private ActivityMainBinding hub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        */
        super.onCreate(savedInstanceState);
        hub = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(hub.getRoot());
        onclick();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
        gMap = googleMap;
        LatLng location = new LatLng(47.0076653, -120.5366559);
        googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("CWU")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
        gMap.setMinZoomPreference(15);

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(47.0066653, -120.5366559))
                .title("you")
                .icon(BitmapDescriptorFactory.fromBitmap(BitmapUtils.getBitmapFromDrawable(this,R.drawable.logo_pin)))
                );

        //outer bound of polygon (whole globe)
        LatLng[] outerBounds = {
                new LatLng(27.0076653, -140.5366559),
                new LatLng(27.0076653, -100.5366559),
                new LatLng(67.0076653, -100.5366559),
                new LatLng(67.0076653, -140.5366559),
        };
        //campus polygon
        LatLng[] campusBounds = {
                new LatLng(47.0103436, -120.5319118),
                new LatLng(47.0103708, -120.5433145),
                new LatLng(47.0059781, -120.5432776),
                new LatLng(47.0060419, -120.5440722),
                new LatLng(47.0042626, -120.5441085),
                new LatLng(47.0042080, -120.5434423),
                new LatLng(46.9998168, -120.5431942),
                new LatLng(46.9999197, -120.5383953),
                new LatLng(47.0000166, -120.5375176),
                new LatLng(47.0000617, -120.5373084),
                new LatLng(47.0005613, -120.5363522),
                new LatLng(47.0019359, -120.5339452),
                new LatLng(47.0020242, -120.5337176),
                new LatLng(47.0031505, -120.5336498),
                new LatLng(47.0035362, -120.5336270),
                new LatLng(47.0038684, -120.5330688),
                new LatLng(47.0031903, -120.5318514),
                new LatLng(47.0063638, -120.5319969),
                new LatLng(47.0063560, -120.5303953),
                new LatLng(47.0065949, -120.5294750),
                new LatLng(47.0071077, -120.5286851),
                new LatLng(47.0073789, -120.5283079),
                new LatLng(47.0080846, -120.5276679),
                new LatLng(47.0083306, -120.5276139),
                new LatLng(47.0087271, -120.5276645),
                new LatLng(47.0097465, -120.5270117),
                new LatLng(47.0100531, -120.5267287),
                new LatLng(47.0104467, -120.5268756),
                new LatLng(47.0104531, -120.5288588),
                new LatLng(47.0103539, -120.5299139)
        };

        //cut hole in world polygon
        PolygonOptions polygonOptions = new PolygonOptions();
        polygonOptions.add(outerBounds);
        polygonOptions.addHole(java.util.Arrays.asList(campusBounds));
        polygonOptions.strokeColor(Color.GRAY);
        polygonOptions.strokeWidth(0);
        polygonOptions.fillColor(Color.argb(128,200,200,200));

        Polygon campusHighlight = gMap.addPolygon(polygonOptions);

    }

    public void onclick(){
        hub.MiscButton.setOnClickListener( v->
                miscclick()
        );

        hub.profileButton.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), Profile.class))
        );
        hub.SchedulerButton.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(), Calendar.class))
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
}

