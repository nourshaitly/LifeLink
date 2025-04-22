package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.content.pm.PackageManager;

import com.example.lifelink.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;

import java.util.ArrayList;
import java.util.Locale;

public class EmergencyActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST = 100;
    private static final int SMS_PERMISSION_REQUEST = 101;

    private String emergencyLevel;
    private TextView statusText, heartRateText, spo2Text, recommendationText;
    private Button sosButton, voiceRecommendationsButton, emergencyContactButton, shareLocationButton;
    private TextToSpeech textToSpeech;

    private int heartRate = 120;  // Sample
    private int spo2 = 90;        // Sample
    private String emergencyContact = "+96170875484";
    private String sosnum = "+96170046401";
    private String userLocation = "Location not available";

    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        // Initialize views
        statusText = findViewById(R.id.emergencyStatusValue);
        heartRateText = findViewById(R.id.heartRateValue);
        spo2Text = findViewById(R.id.spo2Value);
        recommendationText = findViewById(R.id.recommendationText);
        sosButton = findViewById(R.id.sosButton);
        voiceRecommendationsButton = findViewById(R.id.voiceRecommendationsButton);
        emergencyContactButton = findViewById(R.id.emergencyContactButton);
        shareLocationButton = findViewById(R.id.shareLocationButton);

        // Text-to-Speech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        sosButton.setOnClickListener(v -> handleSOSButtonClick());
        voiceRecommendationsButton.setOnClickListener(v -> speakRecommendations());
        emergencyContactButton.setOnClickListener(v -> handleEmergencyContactButton());
        shareLocationButton.setOnClickListener(v -> shareLocation());

        // Get emergency level
        emergencyLevel = getIntent().getStringExtra("emergency_level");
        if (emergencyLevel == null) emergencyLevel = "Low";

        // Start location + emergency process
        checkLocationSettingsThenFetch();
    }

    private void checkLocationSettingsThenFetch() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> fetchUserLocationThenContinue())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        try {
                            ((ResolvableApiException) e).startResolutionForResult(this, 103);
                        } catch (Exception ex) {
                            Toast.makeText(this, "Failed to request location settings.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Location services not available.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserLocationThenContinue() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
            return;
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        userLocation = "www.google.com/maps/search/?api=1&query=" + String.format(Locale.US, "%.6f,%.6f", lat, lng);
                    }
                    continueEmergencyFlow();
                });
    }

    private void continueEmergencyFlow() {
        int level = classifyEmergencyLevel(heartRate, spo2);
        updateUIBasedOnEmergencyLevel();

        if (level == 3) {
            sosButton.setText("Calling...");
            triggerCall();
            requestSmsPermissionIfNeeded();
        } else if (level == 2) {
            requestSmsPermissionIfNeeded();
        }
    }

    private void triggerCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + sosnum));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST);
        }
    }

    // Function to send SMS to emergency contact (Medium and High-level)
    private void sendSmsToEmergencyContact() {
        String message = "Test emergency SMS\nCondition: " + getConditionMessage() +
                "\nHR: " + heartRate + "\nSpO₂: " + spo2 + "\nLocation: " + userLocation;

        String phone = emergencyContact.replace(" ", ""); // remove spaces just in case

        try {
            SmsManager smsManager = SmsManager.getDefault();

            // If message is long, divide it into parts
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);

            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }



    private void requestSmsPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        } else {
            sendSmsToEmergencyContact();
        }
    }

    private int classifyEmergencyLevel(int heartRate, int spo2) {
        if (heartRate <= 100 && spo2 >= 95) return 1;
        else if (heartRate <= 120 && spo2 >= 90) return 2;
        else return 3;
    }

    private String getConditionMessage() {
        switch (classifyEmergencyLevel(heartRate, spo2)) {
            case 1: return "Low-level emergency";
            case 2: return "Medium-level emergency";
            case 3: return "High-level emergency";
            default: return "Unknown";
        }
    }

    private void updateUIBasedOnEmergencyLevel() {
        int level = classifyEmergencyLevel(heartRate, spo2);

        sosButton.setText("SOS");
        heartRateText.setText("Heart Rate: " + heartRate + " bpm");
        spo2Text.setText("SpO₂: " + spo2 + "%");

        if (level == 1) {
            statusText.setText("Low-Level Emergency");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            recommendationText.setText("Stay calm and rest.");
        } else if (level == 2) {
            statusText.setText("Medium-Level Emergency");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
            recommendationText.setText("Seek help if symptoms worsen.");
        } else {
            statusText.setText("High-Level Emergency");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            recommendationText.setText("Critical! Help is on the way.");
        }
    }

    private void handleSOSButtonClick() {
        sosButton.setText("Calling...");
        triggerCall();
        requestSmsPermissionIfNeeded();
    }

    private void speakRecommendations() {
        String text = recommendationText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void handleEmergencyContactButton() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + emergencyContact));
        startActivity(intent);
    }

    private void shareLocation() {
        Toast.makeText(this, "Sharing location...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userLocation));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 103 && resultCode == RESULT_OK) {
            fetchUserLocationThenContinue();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerCall();
        } else if (requestCode == SMS_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSmsToEmergencyContact();
        } else if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocationThenContinue();
        }
    }
}
