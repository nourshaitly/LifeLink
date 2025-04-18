package com.example.lifelink.View;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.lifelink.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Lebanon bounds
    private final LatLngBounds LEBANON_BOUNDS = new LatLngBounds(
            new LatLng(33.05, 35.1),
            new LatLng(34.7, 36.6)
    );

    // Put your actual key here directly or load from secure config
    private final String GOOGLE_MAPS_API_KEY = "AIzaSyDGkTori7s092HchmI4Nyc48RARecxjHDo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Center on Lebanon
        LatLng lebanonCenter = new LatLng(33.8547, 35.8623);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lebanonCenter, 8f));

        // Limit interaction within Lebanon
        mMap.setLatLngBoundsForCameraTarget(LEBANON_BOUNDS);
        mMap.setMinZoomPreference(6f);
        mMap.setMaxZoomPreference(18f);

        enableUserLocation();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14f));
                fetchNearbyMedicalCenters(userLocation.latitude, userLocation.longitude);
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNearbyMedicalCenters(double latitude, double longitude) {
        int[] radiusSteps = {8000, 12000, 20000};
        String[] searchParams = {
                "type=hospital",
                "type=pharmacy",
                "keyword=clinic",
                "keyword=medical center"
        };

        new Thread(() -> {
            Set<String> shownMarkers = new HashSet<>();

            for (int radius : radiusSteps) {
                for (String param : searchParams) {
                    try {
                        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"
                                + "?location=" + latitude + "," + longitude
                                + "&radius=" + radius
                                + "&" + param
                                + "&key=" + GOOGLE_MAPS_API_KEY;

                        Log.d("PlacesAPI", "Request URL: " + url);

                        URL requestUrl = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                        connection.setRequestMethod("GET");

                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(connection.getInputStream())
                        );

                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject jsonObject = new JSONObject(response.toString());
                        String status = jsonObject.optString("status", "NO_STATUS");
                        Log.d("PlacesAPI", "API Status: " + status);
                        Log.d("PlacesAPI", "Full JSON Response: " + jsonObject.toString());



                        if (!status.equals("OK")) {
                            Log.w("PlacesAPI", "⚠️ API returned status: " + status);
                            continue;
                        }

                        JSONArray results = jsonObject.getJSONArray("results");

                        runOnUiThread(() -> {
                            for (int i = 0; i < results.length(); i++) {
                                try {
                                    JSONObject place = results.getJSONObject(i);
                                    String name = place.getString("name");
                                    String address = place.optString("vicinity", "No address");

                                    String markerId = name + "_" + address;
                                    if (shownMarkers.contains(markerId)) continue;
                                    shownMarkers.add(markerId);

                                    JSONObject locationObj = place.getJSONObject("geometry").getJSONObject("location");
                                    double lat = locationObj.getDouble("lat");
                                    double lng = locationObj.getDouble("lng");

                                    LatLng placeLatLng = new LatLng(lat, lng);
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(placeLatLng)
                                            .title(name));
                                    marker.setTag(address);

                                    Log.d("PlacesAPI", "✔ Marker added: " + name + " @ " + lat + "," + lng);

                                } catch (Exception e) {
                                    Log.e("PlacesAPI", "❌ Marker parse error: ", e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("PlacesAPI", "❌ API fetch error: ", e);
                    }
                }
            }

            runOnUiThread(() -> {
                mMap.setOnMarkerClickListener(marker -> {
                    String title = marker.getTitle();
                    String address = (String) marker.getTag();
                    if (title == null || address == null) return false;

                    MedicalInfoBottomSheet bottomSheet = MedicalInfoBottomSheet.newInstance(title, address);
                    bottomSheet.show(getSupportFragmentManager(), "MedicalInfoBottomSheet");
                    return true;
                });
            });

        }).start();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission is required to view nearby medical centers.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
