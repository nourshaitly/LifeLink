package com.example.lifelink.View;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.content.pm.PackageManager;

import com.example.lifelink.Controller.FirestoreService;
import com.example.lifelink.Controller.HealthTrackerState;
import com.example.lifelink.Controller.LiveHealthDataHolder;
import com.example.lifelink.Model.HealthData;
import com.example.lifelink.Model.MedicalProfile;
import com.example.lifelink.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.*;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Locale;

public class EmergencyActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST = 100;
    private static final int SMS_PERMISSION_REQUEST = 101;
    private static final String CHANNEL_ID = "emergency_channel";

    private TextView statusText, heartRateText, spo2Text, recommendationText;
    private Button sosButton, voiceRecommendationsButton, emergencyContactButton, shareLocationButton;
    private TextToSpeech textToSpeech;

    private int heartRate;
    private int spo2;
    private String emergencyContact = "+96171010584";
    private String sosnum = "+96171010584";
    private String userLocation = "Location not available";

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Handler handler;
    private Runnable monitorTask;

    private MedicalProfile medicalProfile;  // This will hold the Medical Profile data

    private static final SparseArray<Class<?>> NAV_MAP = new SparseArray<>();
    static {
        NAV_MAP.put(R.id.nav_nearby, MapIntroActivity.class);
        NAV_MAP.put(R.id.nav_reminder, RemindersWelcomeActivity.class);
        NAV_MAP.put(R.id.nav_home, AIChatActivity.class);
        NAV_MAP.put(R.id.nav_emergency, EmergencyActivity.class);
        NAV_MAP.put(R.id.nav_health, HealthTrackerActivity.class);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency);

        createEmergencyNotificationChannel();
        initializeViews();
        initializeTextToSpeech();
        setupButtonListeners();
        // Fetch the medical profile from Firestore
        fetchMedicalProfile();
        checkLocationSettingsThenFetch();
        startMonitoringHealthData();

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display back button


        setupBottomNav();



    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        // bottomNav.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);
        //bottomNav.setSelectedItemId(R.id.nav_nearby); // Set default selected item
    }
    private boolean onNavItemSelected(@NonNull MenuItem item) {
        Class<?> target = NAV_MAP.get(item.getItemId());
        if (target == null) {
            return false; // No matching activity
        }
        Intent intent = new Intent(this, target)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Class<?> target = NAV_MAP.get(R.id.nav_home);
            if (target != null) {
                Intent intent = new Intent(this, target)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0, 0);
            } else {
                onBackPressed();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




    private void initializeViews() {
        statusText = findViewById(R.id.emergencyStatusValue);
        heartRateText = findViewById(R.id.heartRateValue);
        spo2Text = findViewById(R.id.spo2Value);
        recommendationText = findViewById(R.id.recommendationText);
        sosButton = findViewById(R.id.sosButton);
        voiceRecommendationsButton = findViewById(R.id.voiceRecommendationsButton);
        emergencyContactButton = findViewById(R.id.emergencyContactButton);
        shareLocationButton = findViewById(R.id.shareLocationButton);
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });
    }

    private void setupButtonListeners() {
        sosButton.setOnClickListener(v -> handleSOSButtonClick());
        voiceRecommendationsButton.setOnClickListener(v -> speakRecommendations());
        emergencyContactButton.setOnClickListener(v -> handleEmergencyContactButton());
        shareLocationButton.setOnClickListener(v -> shareLocation());
    }

    private void createEmergencyNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Emergency Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Triggered when emergency condition is detected");
            channel.enableVibration(true);
            channel.enableLights(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void startMonitoringHealthData() {
        handler = new Handler();
        monitorTask = () -> {
            fetchAndUpdateEmergencyFlow();
            handler.postDelayed(monitorTask, 3000);
        };
        handler.post(monitorTask);
    }

    private void fetchAndUpdateEmergencyFlow() {
        HealthData healthData = LiveHealthDataHolder.getHealthData();
        if (healthData != null) {
            heartRate = healthData.getHeartRate();
            spo2 = healthData.getSpo2();
            continueEmergencyFlow();
        } else {
            Toast.makeText(this, "Waiting for live health data...", Toast.LENGTH_SHORT).show();
        }
    }

    private void continueEmergencyFlow() {
        if (medicalProfile!= null) {
            int level = evaluatePersonalizedEmergencyLevel(medicalProfile, heartRate, spo2);
            updateUIBasedOnLevel(level);
            handleEmergencyActions(level);
        } else {
            Toast.makeText(this, "User profile not loaded yet.", Toast.LENGTH_SHORT).show();
        }
    }

    private int evaluatePersonalizedEmergencyLevel(MedicalProfile profile, int heartRate, int spo2) {
        int score = 0;
        int age = profile.getAge();
        double bmi = calculateBMI(profile.getHeightCm(), profile.getWeightKg());

        if (age >= 65) score += 2;
        else if (age >= 50) score += 1;

        if (bmi >= 30) score += 2;
        else if (bmi >= 25) score += 1;

        if (profile.isSmoker()) score += 2;
        if (profile.isAlcoholic()) score += 1;

        if (profile.getChronicDiseases().contains("Hypertension") ||
                profile.getChronicDiseases().contains("Heart Disease")) score += 2;

        if (profile.getChronicDiseases().contains("Asthma") ||
                profile.getSymptoms().contains("Shortness of breath")) score += 2;

        if (profile.getFamilyHistory().contains("Heart disease")) score += 1;


        if (spo2 < 88) score += 4;
        else if (spo2 < 92) score += 3;
        else if (spo2 < 95) score += 2;

        if (heartRate > 150) score += 4;
        else if (heartRate > 130) score += 3;
        else if (heartRate > 110) score += 2;

        if (score >= 9) return 3;
        else if (score >= 5) return 2;
        else return 1;
    }

    private double calculateBMI(int heightCm, int weightKg) {
        double heightMeters = heightCm / 100.0;
        return weightKg / (heightMeters * heightMeters);
    }

    private void updateUIBasedOnLevel(int level) {
        heartRateText.setText("Heart Rate: " + heartRate + " bpm");
        spo2Text.setText("SpO₂: " + spo2 + "%");
        sosButton.setText("SOS");

        if (level == 1) {
            statusText.setText("Low-Level Emergency");
            statusText.setTextColor(getResources().getColor(R.color.green));
            recommendationText.setText("Stay calm and rest.");
        } else if (level == 2) {
            statusText.setText("Medium-Level Emergency");
            statusText.setTextColor(getResources().getColor(R.color.orange));
            recommendationText.setText("Seek help if symptoms worsen.");
        } else {
            statusText.setText("High-Level Emergency");
            statusText.setTextColor(getResources().getColor(R.color.LogoRed));
            recommendationText.setText("Critical! Help is on the way.");
        }
    }

    private void handleEmergencyActions(int level) {
        if (level == 3) {
            showEmergencyNotification("Critical condition detected. Immediate action required.");
            handleSOSButtonClick();
        } else if (level == 2) {
            showEmergencyNotification("Moderate emergency. Monitor condition and prepare to act.");
        }
    }

    private void speakRecommendations() {
        String text = recommendationText.getText().toString();
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private void handleSOSButtonClick() {
        triggerCall();
        requestSmsPermissionIfNeeded();
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

    private void checkLocationSettingsThenFetch() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
                .addOnSuccessListener(locationSettingsResponse -> fetchUserLocation())
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

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 102);
            return;
        }

        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat = location.getLatitude();
                        double lng = location.getLongitude();
                        userLocation = "https://www.google.com/maps/search/?api=1&query=" + String.format(Locale.US, "%.6f,%.6f", lat, lng);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (handler != null && monitorTask != null) {
            handler.removeCallbacks(monitorTask);
        }
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            triggerCall();
        } else if (requestCode == SMS_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSmsToEmergencyContact();
        } else if (requestCode == 102 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchUserLocation();
        }
    }





    private void sendSmsToEmergencyContact() {
        String message = "🚨 Emergency detected\nCondition: " + getConditionMessage() +
                "\nHR: " + heartRate + "\nSpO₂: " + spo2 + "\nLocation: " + userLocation;

        String phone = emergencyContact.replace(" ", "");

        try {
            SmsManager smsManager = SmsManager.getDefault();
            ArrayList<String> parts = smsManager.divideMessage(message);
            smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
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




    private void showEmergencyNotification(String recommendation) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_announcement)
                .setContentTitle("🚨 Emergency Recommendation")
                .setContentText(recommendation)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .build();

        manager.notify(2001, notification);
    }



    private void requestSmsPermissionIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_REQUEST);
        } else {
            sendSmsToEmergencyContact();
        }
    }

    private String getConditionMessage() {
        int level = evaluatePersonalizedEmergencyLevel(medicalProfile, heartRate, spo2);
        switch (level) {
            case 1:
                return "Low-level emergency";
            case 2:
                return "Medium-level emergency";
            case 3:
                return "High-level emergency";
            default:
                return "Unknown";
        }
    }



    private void fetchMedicalProfile() {
        FirestoreService firestoreService = new FirestoreService();
        firestoreService.fetchMedicalProfile(new FirestoreService.OnProfileFetchedListener() {

            @Override



            public void onProfileFetched(MedicalProfile profile) {
                if (profile == null) {
                    Toast.makeText(EmergencyActivity.this, "❌ Profile is null", Toast.LENGTH_SHORT).show();
                    return;
                }

                medicalProfile = profile;
                HealthTrackerState.isProfileReady = true;

                Toast.makeText(EmergencyActivity.this, "✅ Profile loaded", Toast.LENGTH_SHORT).show();
           continueEmergencyFlow();
            }


            @Override
            public void onError(String error) {
                Toast.makeText(EmergencyActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
