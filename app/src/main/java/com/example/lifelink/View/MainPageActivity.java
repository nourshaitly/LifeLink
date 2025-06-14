package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.Controller.BluetoothManager;
import com.example.lifelink.R;
import com.example.lifelink.View.LiveHealthDataUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST = 100;
    private static final int SMS_PERMISSION_REQUEST = 101;
    private static final int LOCATION_PERMISSION_REQUEST = 102;

    private static final int BLUETOOTH_PERMISSION_REQUEST = 103;
    private String sosnum = "+96171010584";

    private CardView healthTrackerCard, nearbyMedicalCentersCard, reminderCard,
            emergencyCard, aiFeatureCard, extraCard;
    private Button sosButton;

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        BluetoothManager.init(this);


        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(MainPageActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Init cards
        healthTrackerCard = findViewById(R.id.healthTrackerCard);
        nearbyMedicalCentersCard = findViewById(R.id.nearbyMedicalCentersCard);
        reminderCard = findViewById(R.id.reminderCard);
        emergencyCard = findViewById(R.id.emergencyCard);
        aiFeatureCard = findViewById(R.id.aiFeatureCard);
        extraCard = findViewById(R.id.extraCard);

        sosButton = findViewById(R.id.sosButton);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Button click listeners
        healthTrackerCard.setOnClickListener(v -> startActivity(new Intent(this, HealthTrackerActivity.class)));
        nearbyMedicalCentersCard.setOnClickListener(v -> startActivity(new Intent(this, MapIntroActivity.class)));
        reminderCard.setOnClickListener(v -> startActivity(new Intent(this, RemindersWelcomeActivity.class)));
        emergencyCard.setOnClickListener(v -> startActivity(new Intent(this, EmergencyActivity.class)));
        aiFeatureCard.setOnClickListener(v -> startActivity(new Intent(this, AIChatActivity.class)));
        extraCard.setOnClickListener(v -> startActivity(new Intent(this, WellnessWelcomeActivity.class)));

        sosButton.setOnClickListener(v -> handleSOSButtonClick());

        // Optional: fetch location when activity starts
        fetchUserLocation();
    }

    private void handleSOSButtonClick() {
        triggerCall();         // Call the SOS number
        fetchUserLocation();   // Update current location
        sendSmsToSosNumber();  // Send emergency SMS
    }

    private void triggerCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + sosnum));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST);
        }
    }

    private void sendSmsToSosNumber() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
            return;
        }
        

        String message = LiveHealthDataUtils.getEmergencyMessage();

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(sosnum, null, parts, null, null);
            Toast.makeText(this, "✅ SOS SMS sent", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to send SOS SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                LiveHealthDataUtils.setUserLocation(lat, lng);
            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        BluetoothManager.handlePermissionResult(this, requestCode, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerCall();
        } else if (requestCode == SMS_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSmsToSosNumber();
        } else if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
