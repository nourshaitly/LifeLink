package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MapIntroActivity extends AppCompatActivity {

    private MapView miniMapView;
    private FusedLocationProviderClient fusedLocationClient;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_intro);

        // Initialize views
        miniMapView = findViewById(R.id.miniMapView);
        CardView miniMapCard = findViewById(R.id.miniMapCard);

        // Initialize fused location
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize MapView
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }
        miniMapView.onCreate(mapViewBundle);

        miniMapView.getMapAsync(googleMap -> {
            // Disable gestures to make it a preview
            googleMap.getUiSettings().setAllGesturesEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            // Enable location if permissions granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap.setMyLocationEnabled(true);

                // Get last known location
                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    }
                });
            }
        });

        // Click on map preview → open full map
        View overlayView = findViewById(R.id.overlayView);
        overlayView.setClickable(true);
        overlayView.setOnClickListener(v -> {

            // Later → Open GeoMapsActivity
            Intent intent = new Intent(MapIntroActivity.this, GeoMapsActivity.class);
            startActivity(intent);
        });



    }

    // MAPVIEW Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        miniMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        miniMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        miniMapView.onStop();
    }

    @Override
    protected void onPause() {
        miniMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        miniMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        miniMapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }
        miniMapView.onSaveInstanceState(mapViewBundle);
    }
}
