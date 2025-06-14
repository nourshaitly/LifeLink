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
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
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
    private SeekBar radiusSeekBar;
    private TextView radiusTextView;

    private boolean showNearestHospital = false;
    private boolean nearestHandled       = false;


    private List<NearbyPlace> allHospitalsList  = new ArrayList<>();
    private List<NearbyPlace> allPharmaciesList = new ArrayList<>();

    private double currentLat, currentLon;
    private int searchRadiusMeters = 5000; // default 5 km

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler           = new Handler(Looper.getMainLooper());

    private final LatLngBounds LEBANON_BOUNDS = new LatLngBounds(
            new LatLng(33.05, 35.1),
            new LatLng(34.7, 36.6)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geo_maps);
        showNearestHospital = getIntent().getBooleanExtra("showNearestHospital", false);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        filterChips      = findViewById(R.id.placeFilterChips);
        showListButton   = findViewById(R.id.showListButton);
        radiusSeekBar    = findViewById(R.id.radiusSeekBar);
        radiusTextView   = findViewById(R.id.radiusTextView);

        // --- SeekBar setup ---
        radiusSeekBar.setMax(20);
        radiusSeekBar.setProgress(5);
        radiusTextView.setText("Radius: 5 km");
        radiusSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int prog, boolean fromUser) {
                int km = Math.max(prog, 1);
                searchRadiusMeters = km * 1000;
                radiusTextView.setText(km + " km");
            }
            @Override public void onStartTrackingTouch(SeekBar sb) { }
            @Override public void onStopTrackingTouch(SeekBar sb) {
                // re-fetch current filter
                int checked = filterChips.getCheckedChipId();
                mMap.clear();
                allHospitalsList.clear();
                allPharmaciesList.clear();
                if (checked == R.id.chipHospitals || checked == R.id.chipAll) {
                    fetchPlaces("hospital");
                }
                if (checked == R.id.chipPharmacies || checked == R.id.chipAll) {
                    fetchPlaces("pharmacy");
                }
            }
        });

        filterChips.setOnCheckedStateChangeListener((group, checkedIds) -> {
            mMap.clear();
            allHospitalsList.clear();
            allPharmaciesList.clear();
            if (checkedIds.contains(R.id.chipAll)) {
                fetchPlaces("hospital");
                fetchPlaces("pharmacy");
                fetchPlaces("hospital");
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

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


    }

    @Override

    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Clamp camera to Lebanon
        mMap.setLatLngBoundsForCameraTarget(LEBANON_BOUNDS);
        mMap.setMinZoomPreference(6f);
        mMap.setMaxZoomPreference(18f);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Always start framed on all of Lebanon:
        int paddingPx = (int)(getResources().getDisplayMetrics().widthPixels * 0.10);
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(LEBANON_BOUNDS, paddingPx));

        // Marker click opens detail sheet
        mMap.setOnMarkerClickListener(marker -> {
            LatLng pos = marker.getPosition();
            NearbyPlace clicked = null;
            for (NearbyPlace p : allHospitalsList)
                if (p.getLocation().equals(pos)) clicked = p;
            for (NearbyPlace p : allPharmaciesList)
                if (p.getLocation().equals(pos)) clicked = p;

            if (clicked != null) {
                new NearbyPlaceDetailBottomSheet(clicked)
                        .show(getSupportFragmentManager(), "PlaceDetail");
            }
            return false;
        });

        // If we're in “nearest hospital” mode, bypass chips and fetch only hospitals:
        if (showNearestHospital) {
            // clear any previous data
            allHospitalsList.clear();
            allPharmaciesList.clear();
            mMap.clear();

            // fetch hospitals; the handler.post will zoom to & show the nearest once loaded
            fetchPlaces("hospital");
        } else {
            // normal behavior: check “All” to load both categories
            filterChips.check(R.id.chipAll);
        }

        // Now enable the blue-dot & live location, if permission is granted
        enableUserLocation();
    }


    private void fetchPlaces(String type) {
        Toast.makeText(this, "Fetching " + type + "s…", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            try {
                String urlStr = String.format(Locale.US,
                        "https://api.geoapify.com/v2/places?" +
                                "categories=healthcare.%s&" +
                                "filter=circle:%.7f,%.7f,%d&" +
                                "limit=20&apiKey=%s",
                        type, currentLon, currentLat,
                        searchRadiusMeters, GEOAPIFY_KEY
                );
                HttpURLConnection conn =
                        (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder(); String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONArray features = new JSONObject(sb.toString())
                        .optJSONArray("features");
                List<NearbyPlace> places = new ArrayList<>();

                if (features != null) {
                    for (int i = 0; i < features.length(); i++) {
                        JSONObject props = features.getJSONObject(i)
                                .getJSONObject("properties");
                        JSONArray coord = features.getJSONObject(i)
                                .getJSONObject("geometry")
                                .getJSONArray("coordinates");
                        LatLng pos = new LatLng(coord.getDouble(1), coord.getDouble(0));

                        // restore full contact & datasource logic:
                        String name    = props.optString("name", "Unnamed");
                        String address = props.optString("address_line2", "N/A");
                        String phone   = "Not available", email = "Not available", website = "Not available";

                        JSONObject contact = props.optJSONObject("contact");
                        if (contact != null) {
                            phone   = contact.optString("phone", phone);
                            email   = contact.optString("email", email);
                            website = contact.optString("website", website);
                        }
                        JSONObject ds = props.optJSONObject("datasource");
                        if (ds != null && ds.optJSONObject("raw") != null) {
                            JSONObject raw = ds.getJSONObject("raw");
                            phone   = (phone.equals("Not available")) ?
                                    raw.optString("contact:phone", phone) : phone;
                            email   = (email.equals("Not available")) ?
                                    raw.optString("contact:email", email) : email;
                            website = (website.equals("Not available")) ?
                                    raw.optString("contact:website", website) : website;
                        }

                        NearbyPlace place = new NearbyPlace(
                                name, pos, type,
                                address, phone, email, website
                        );
                        float[] dist = new float[1];
                        Location.distanceBetween(
                                currentLat, currentLon, pos.latitude, pos.longitude, dist
                        );
                        place.setDistance(dist[0]);
                        places.add(place);
                    }
                }

                handler.post(() -> {
                    if (places.isEmpty()) {
                       // Toast.makeText(this,"No " + type + "s found within radius",Toast.LENGTH_SHORT).show();
                    } else {
                        float hue = type.equals("pharmacy")
                                ? BitmapDescriptorFactory.HUE_AZURE
                                : BitmapDescriptorFactory.HUE_RED;
                        // add markers
                        for (NearbyPlace p : places) {
                            mMap.addMarker(new MarkerOptions()
                                    .position(p.getLocation())
                                    .title(p.getName())
                                    .snippet(p.getAddress())
                                    .icon(BitmapDescriptorFactory.defaultMarker(hue))
                            ).setTag(type);
                        }

                        // collect into lists
                        if (type.equals("hospital")) {
                            allHospitalsList.addAll(places);
                        } else {
                            allPharmaciesList.addAll(places);
                        }

                        // if we're in “nearest hospital” mode and haven't handled it yet:
                        if (showNearestHospital && type.equals("hospital") && !nearestHandled) {
                            // sort by distance and pick the first
                            Collections.sort(allHospitalsList,
                                    Comparator.comparingDouble(NearbyPlace::getDistance));
                            NearbyPlace nearest = allHospitalsList.get(0);

                            // zoom in on the nearest hospital
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                    nearest.getLocation(), 17f
                            ));

                            // show its detail bottom sheet
                            new NearbyPlaceDetailBottomSheet(nearest)
                                    .show(getSupportFragmentManager(), "PlaceDetail");

                            nearestHandled = true;
                        }
                    }
                });


            } catch (Exception e) {
                Log.e(TAG, "Fetch error", e);
                handler.post(() ->
                        Toast.makeText(this,
                                "Error fetching " + type + "s.",
                                Toast.LENGTH_SHORT
                        ).show()
                );
            }
        });
    }

    private void showBottomSheet(List<NearbyPlace> hospitals,
                                 List<NearbyPlace> pharmacies) {
        Collections.sort(hospitals,   Comparator.comparingDouble(NearbyPlace::getDistance));
        Collections.sort(pharmacies, Comparator.comparingDouble(NearbyPlace::getDistance));
        new NearbyPlacesBottomSheet(hospitals, pharmacies, place ->
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(place.getLocation(), 17f)
                )
        ).show(getSupportFragmentManager(), "NearbyPlaces");
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }
        mMap.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, loc -> {
            if (loc != null) {
                currentLat = loc.getLatitude();
                currentLon = loc.getLongitude();
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                new LatLng(currentLat, currentLon), 14f
                        )
                );

                // ✅ Delay chip check after coordinates are set
                handler.postDelayed(() -> filterChips.check(R.id.chipAll), 200);
            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int code,
                                           @NonNull String[] perms, @NonNull int[] results) {
        super.onRequestPermissionsResult(code, perms, results);
        if (code == LOCATION_PERMISSION_REQUEST_CODE
                && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
        } else {
            Toast.makeText(this,
                    "Location permission required",
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}