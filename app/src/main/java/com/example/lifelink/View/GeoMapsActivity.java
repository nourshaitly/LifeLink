package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.lifelink.Model.NearbyPlace;
import com.example.lifelink.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeoMapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "GeoMapsActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String GEOAPIFY_KEY = "8063df3cb2d94b258597b33881517ba4";

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private ChipGroup filterChips;
    private Button showListButton;

    private List<NearbyPlace> allHospitalsList = new ArrayList<>();
    private List<NearbyPlace> allPharmaciesList = new ArrayList<>();

    private double currentLat, currentLon;
    private ExecutorService executor = Executors.newSingleThreadExecutor(); // New executor instead of AsyncTask
    private Handler handler = new Handler(Looper.getMainLooper()); // UI thread handler

    // Restricting to Lebanon Bounds
    private final LatLngBounds LEBANON_BOUNDS = new LatLngBounds(
            new LatLng(33.05, 35.1),
            new LatLng(34.7, 36.6)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        filterChips = findViewById(R.id.placeFilterChips);
        showListButton = findViewById(R.id.showListButton);

        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            mMap.clear();
            allHospitalsList.clear();
            allPharmaciesList.clear();

            if (checkedIds.contains(R.id.chipAll)) {
                fetchPlaces("hospital");
                fetchPlaces("pharmacy");
            } else if (checkedIds.contains(R.id.chipHospitals)) {
                fetchPlaces("hospital");
            } else if (checkedIds.contains(R.id.chipPharmacies)) {
                fetchPlaces("pharmacy");
            }
        });

        showListButton.setOnClickListener(v -> {
            if (filterChips.getCheckedChipId() == R.id.chipHospitals) {
                showBottomSheet(allHospitalsList, new ArrayList<>());
            } else if (filterChips.getCheckedChipId() == R.id.chipPharmacies) {
                showBottomSheet(new ArrayList<>(), allPharmaciesList);
            } else {
                showBottomSheet(allHospitalsList, allPharmaciesList);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void showBottomSheet(List<NearbyPlace> hospitals, List<NearbyPlace> pharmacies) {
        Collections.sort(hospitals, Comparator.comparingDouble(NearbyPlace::getDistance));
        Collections.sort(pharmacies, Comparator.comparingDouble(NearbyPlace::getDistance));

        NearbyPlacesBottomSheet bottomSheet = new NearbyPlacesBottomSheet(hospitals, pharmacies, place -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLocation(), 17f));
            }
        });
        bottomSheet.show(getSupportFragmentManager(), "NearbyPlaces");
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMarkerClickListener(marker -> {
            LatLng position = marker.getPosition();
            NearbyPlace clickedPlace = null;

            for (NearbyPlace place : allHospitalsList)
                if (place.getLocation().equals(position)) clickedPlace = place;
            for (NearbyPlace place : allPharmaciesList)
                if (place.getLocation().equals(position)) clickedPlace = place;

            if (clickedPlace != null) {
                NearbyPlaceDetailBottomSheet detailBottomSheet = new NearbyPlaceDetailBottomSheet(clickedPlace);
                detailBottomSheet.show(getSupportFragmentManager(), "PlaceDetail");
            }
            return false;
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(33.8547, 35.8623), 8f));
        mMap.setLatLngBoundsForCameraTarget(LEBANON_BOUNDS);
        mMap.setMinZoomPreference(6f);
        mMap.setMaxZoomPreference(18f);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLon), 14f));
                filterChips.check(R.id.chipAll);
            } else {
                Toast.makeText(this, "Unable to fetch current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlaces(String type) {
        Toast.makeText(this, "Fetching " + type + "s...", Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                String urlStr = String.format(Locale.US,
                        "https://api.geoapify.com/v2/places?categories=healthcare.%s&filter=circle:%.7f,%.7f,5000&limit=20&apiKey=%s",
                        type, currentLon, currentLat, GEOAPIFY_KEY);

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray features = new JSONObject(sb.toString()).optJSONArray("features");
                List<NearbyPlace> places = new ArrayList<>();

                if (features != null) {
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject props = features.getJSONObject(i).getJSONObject("properties");
                        JSONArray coord = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                        LatLng pos = new LatLng(coord.getDouble(1), coord.getDouble(0));

                        String name = props.optString("name", "Unnamed");
                        String address = props.optString("address_line2", "Address not available");

                        String phone = null, email = null, website = null;
                        JSONObject contact = props.optJSONObject("contact");
                        if (contact != null) {
                            phone = contact.optString("phone", null);
                            email = contact.optString("email", null);
                            website = contact.optString("website", null);
                        }

                        JSONObject datasource = props.optJSONObject("datasource");
                        if (datasource != null) {
                            JSONObject raw = datasource.optJSONObject("raw");
                            if (raw != null) {
                                if (phone == null || phone.isEmpty()) phone = raw.optString("contact:phone", null);
                                if (email == null || email.isEmpty()) email = raw.optString("contact:email", null);
                                if (website == null || website.isEmpty()) website = raw.optString("contact:website", null);
                                if ((website == null || website.isEmpty()) && raw.has("contact:facebook"))
                                    website = raw.optString("contact:facebook");
                            }
                        }

                        if (phone == null) phone = "Not available";
                        if (email == null) email = "Not available";
                        if (website == null) website = "Not available";

                        NearbyPlace place = new NearbyPlace(name, pos, type, address, phone, email, website);

                        float[] results = new float[1];
                        Location.distanceBetween(currentLat, currentLon, pos.latitude, pos.longitude, results);
                        place.setDistance(results[0]);

                        places.add(place);
                    }
                }

                handler.post(() -> {
                    if (places.isEmpty()) {
                        Toast.makeText(GeoMapsActivity.this, "No " + type + "s found nearby", Toast.LENGTH_SHORT).show();
                    } else {
                        for (NearbyPlace place : places) {
                            if (mMap != null) {
                                float hue = type.equals("pharmacy") ? BitmapDescriptorFactory.HUE_AZURE : BitmapDescriptorFactory.HUE_RED;
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(place.getLocation())
                                        .title(place.getName())
                                        .snippet("Address: " + place.getAddress())
                                        .icon(BitmapDescriptorFactory.defaultMarker(hue)));
                                if (marker != null) marker.setTag(type);
                            }
                        }

                        if (type.equals("hospital")) {
                            allHospitalsList.addAll(places);
                        } else {
                            allPharmaciesList.addAll(places);
                        }
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Fetch places error", e);
                handler.post(() ->
                        Toast.makeText(GeoMapsActivity.this, "Error fetching " + type + "s.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Permission permanently denied
                    Toast.makeText(this, "Location permission permanently denied. Enable from settings.", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
