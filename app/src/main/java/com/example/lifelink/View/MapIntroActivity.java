package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.cardview.widget.CardView;

import com.example.lifelink.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;

public class MapIntroActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2001;
    private static final int REQUEST_CHECK_SETTINGS = 3001;

    private static final LatLngBounds LEBANON_BOUNDS = new LatLngBounds(
            new LatLng(33.05, 35.1),
            new LatLng(34.7, 36.6)
    );
    private static final LatLng DEFAULT_CENTER = new LatLng(33.8547, 35.8623);

    private MapView miniMapView;
    private GoogleMap miniMap;
    private com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;
    private LocationCallback oneShotCallback;

    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_intro);

        // ----- initialize all shared dashboard UI (toolbar, SOS, bottom nav) -----
        DashboardUtils.init(this, R.id.nav_nearby);

        // --- your map-specific initialization ---
        miniMapView = findViewById(R.id.miniMapView);
        CardView miniMapCard = findViewById(R.id.miniMapCard);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // MapView lifecycle
        Bundle mapViewBundle = (savedInstanceState != null)
                ? savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY)
                : null;
        miniMapView.onCreate(mapViewBundle);

        checkLocationSettings();

        miniMapView.getMapAsync(googleMap -> {
            miniMap = googleMap;
            miniMap.getUiSettings().setAllGesturesEnabled(false);
            miniMap.getUiSettings().setMapToolbarEnabled(false);
            miniMap.setLatLngBoundsForCameraTarget(LEBANON_BOUNDS);

            if (hasLocationPermission()) {
                enableMyLocation();
            } else {
                requestLocationPermission();
                miniMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_CENTER, 8f));
            }
        });

        findViewById(R.id.overlayView).setOnClickListener(v ->
                startActivity(new Intent(this, GeoMapsActivity.class))
        );
        findViewById(R.id.btnNearestHospital).setOnClickListener(v -> {
            Intent intent = new Intent(this, GeoMapsActivity.class);
            intent.putExtra("showNearestHospital", true);
            startActivity(intent);
        });
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @RequiresPermission(allOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    private void enableMyLocation() {
        if (miniMap == null || !hasLocationPermission()) return;

        miniMap.setMyLocationEnabled(true);

        if (oneShotCallback == null) {
            oneShotCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult result) {
                    if (result != null && miniMap != null) {
                        Location loc = result.getLastLocation();
                        if (loc != null) {
                            LatLng userLoc = new LatLng(loc.getLatitude(), loc.getLongitude());
                            miniMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLoc, 15f));
                        }
                    }
                    fusedLocationClient.removeLocationUpdates(this);
                }
            };
        }

        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setNumUpdates(1)
                .setInterval(0);
        fusedLocationClient.requestLocationUpdates(req, oneShotCallback, getMainLooper());
    }

    private void checkLocationSettings() {
        LocationRequest req = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(req);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(response -> {
            // GPS ON
        }).addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ((ResolvableApiException) e)
                            .startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
                } catch (Exception ignore) { }
            } else {
                Toast.makeText(this,
                        "Please enable Location in Settings.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && miniMap != null) {
            if (resultCode == RESULT_OK && hasLocationPermission()) {
                enableMyLocation();
            } else {
                Toast.makeText(this,
                        "Location still off. Showing Lebanon overview.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {
                enableMyLocation();
            } else {
                Toast.makeText(this,
                        "Location permission denied. Showing Lebanon overview.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // let DashboardUtils handle the Up/Home click
        if (DashboardUtils.onHomeClicked(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
