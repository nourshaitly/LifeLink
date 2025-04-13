package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelink.R;
import com.google.android.material.button.MaterialButton;

public class HeartConditionOptionsActivity extends AppCompatActivity {
    private MaterialButton healthTrackerButton;
    private MaterialButton heartRateButton;
    private MaterialButton bloodPressureButton;
    private MaterialButton medicationButton;
    private MaterialButton exerciseButton;
    private MaterialButton dietButton;
    private MaterialButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_condition_options);

        // Initialize buttons
        healthTrackerButton = findViewById(R.id.healthTrackerButton);
        heartRateButton = findViewById(R.id.heartRateButton);
        bloodPressureButton = findViewById(R.id.bloodPressureButton);
        medicationButton = findViewById(R.id.medicationButton);
        exerciseButton = findViewById(R.id.exerciseButton);
        dietButton = findViewById(R.id.dietButton);
        backButton = findViewById(R.id.backButton);

        // Set up button click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Health Tracker
        healthTrackerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HealthTrackerActivity.class);
            startActivity(intent);
        });

        // Other buttons show coming soon messages
        heartRateButton.setOnClickListener(v -> 
            Toast.makeText(this, "Heart Rate Monitor coming soon!", Toast.LENGTH_SHORT).show());
        
        bloodPressureButton.setOnClickListener(v -> 
            Toast.makeText(this, "Blood Pressure Tracker coming soon!", Toast.LENGTH_SHORT).show());
        
        medicationButton.setOnClickListener(v -> 
            Toast.makeText(this, "Medication Reminder coming soon!", Toast.LENGTH_SHORT).show());
        
        exerciseButton.setOnClickListener(v -> 
            Toast.makeText(this, "Exercise Guide coming soon!", Toast.LENGTH_SHORT).show());
        
        dietButton.setOnClickListener(v -> 
            Toast.makeText(this, "Diet Recommendations coming soon!", Toast.LENGTH_SHORT).show());

        // Back Button
        backButton.setOnClickListener(v -> finish());
    }
} 