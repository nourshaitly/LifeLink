package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelink.R;
import com.google.android.material.button.MaterialButton;

public class HeartConditionOptionsActivity extends AppCompatActivity {
    private static final String TAG = "HeartConditionOptions";
    private MaterialButton healthTrackerButton;
    private MaterialButton heartRateButton;
    private MaterialButton bloodPressureButton;
    private MaterialButton medicationButton;
    private MaterialButton exerciseButton;
    private MaterialButton dietButton;
    private MaterialButton backButton;
    private Handler handler;
    private boolean isFinishing = false;
    private Toast currentToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_condition_options);
        handler = new Handler(Looper.getMainLooper());
        Log.d(TAG, "onCreate called");

        try {
            initializeButtons();
            setupButtonListeners();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            showToast("Error initializing the screen");
        }
    }

    private void initializeButtons() {
        try {
            healthTrackerButton = findViewById(R.id.healthTrackerButton);
            heartRateButton = findViewById(R.id.heartRateButton);
            bloodPressureButton = findViewById(R.id.bloodPressureButton);
            medicationButton = findViewById(R.id.medicationButton);
            exerciseButton = findViewById(R.id.exerciseButton);
            dietButton = findViewById(R.id.dietButton);
            backButton = findViewById(R.id.backButton);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing buttons: " + e.getMessage(), e);
        }
    }

    private void setupButtonListeners() {
        healthTrackerButton.setOnClickListener(v -> {
            if (isFinishing) return;
            
            try {
                cancelCurrentToast();
                Log.d(TAG, "Starting HealthTrackerActivity");
                
                // Create intent with proper flags
                Intent intent = new Intent(HeartConditionOptionsActivity.this, HealthTrackerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                
                // Start activity
                startActivity(intent);
                Log.d(TAG, "HealthTrackerActivity started successfully");
                
                // Mark that we're finishing
                isFinishing = true;
                
                // Post the finish call with a delay
                handler.postDelayed(() -> {
                    try {
                        if (!isFinishing) return;
                        Log.d(TAG, "Finishing HeartConditionOptionsActivity");
                        finish();
                    } catch (Exception e) {
                        Log.e(TAG, "Error finishing activity: " + e.getMessage(), e);
                    }
                }, 1000); // Increased delay to 1000ms
                
            } catch (Exception e) {
                Log.e(TAG, "Error starting HealthTrackerActivity: " + e.getMessage(), e);
                showToast("Error opening Health Tracker: " + e.getMessage());
            }
        });

        heartRateButton.setOnClickListener(v -> showToast("Heart Rate Monitor coming soon!"));
        bloodPressureButton.setOnClickListener(v -> showToast("Blood Pressure Tracker coming soon!"));
        medicationButton.setOnClickListener(v -> showToast("Medication Reminder coming soon!"));
        exerciseButton.setOnClickListener(v -> showToast("Exercise Guide coming soon!"));
        dietButton.setOnClickListener(v -> showToast("Diet Recommendations coming soon!"));
        backButton.setOnClickListener(v -> {
            cancelCurrentToast();
            finish();
        });
    }

    private void showToast(String message) {
        if (isFinishing) return;
        try {
            cancelCurrentToast();
            currentToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            currentToast.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing toast: " + e.getMessage(), e);
        }
    }

    private void cancelCurrentToast() {
        try {
            if (currentToast != null) {
                currentToast.cancel();
                currentToast = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error canceling toast: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isFinishing = false;
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelCurrentToast();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isFinishing = true;
        cancelCurrentToast();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        Log.d(TAG, "onDestroy called");
    }
} 