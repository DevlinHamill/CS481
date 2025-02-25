package edu.cwu.catmap.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
        hub = edu.cwu.catmap.databinding.ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(hub.getRoot());
        onclick();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.id_map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng location = new LatLng(47.0076653, -120.5366559);
        googleMap.addMarker(new MarkerOptions().position(location).title("CWU"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16));
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
                startActivity(new Intent(getApplicationContext(), LocationsGUI.class))
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
