package com.example.lifelink.View;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.MainActivity;
import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;

import androidx.annotation.NonNull;


public class MainPageActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST = 100 ;
    // Declare the cards
    private CardView healthTrackerCard, nearbyMedicalCentersCard, reminderCard,
            emergencyCard, aiFeatureCard, extraCard;
    private Button sosButton;
    private String sosnum = "+96171010584";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainPageActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });



        // Initialize the cards
        healthTrackerCard = findViewById(R.id.healthTrackerCard);
        nearbyMedicalCentersCard = findViewById(R.id.nearbyMedicalCentersCard);
        reminderCard = findViewById(R.id.reminderCard);
        emergencyCard = findViewById(R.id.emergencyCard);
        aiFeatureCard = findViewById(R.id.aiFeatureCard);
        extraCard = findViewById(R.id.extraCard);

        sosButton=findViewById(R.id.sosButton);
        // Set OnClickListener for each card
        healthTrackerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Health Tracker Activity
                Intent intent = new Intent(MainPageActivity.this, HealthTrackerActivity.class);
                startActivity(intent);
            }
        });

        nearbyMedicalCentersCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Nearby Medical Centers Activity
                Intent intent = new Intent(MainPageActivity.this, MapIntroActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        reminderCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Reminder Activity
                Intent intent = new Intent(MainPageActivity.this, RemindersWelcomeActivity.class);
                startActivity(intent);
            }
        });

        emergencyCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Emergency Activity
                Intent intent = new Intent(MainPageActivity.this, EmergencyActivity.class);
                startActivity(intent);
            }
        });

        aiFeatureCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AI Feature Activity
                Intent intent = new Intent(MainPageActivity.this, AIChatActivity.class);
                startActivity(intent);
            }
        });

        extraCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open AI Feature Activity
                Intent intent = new Intent(MainPageActivity.this, WellnessWelcomeActivity.class);
                startActivity(intent);
            }
        });
        sosButton.setOnClickListener(v -> handleSOSButtonClick());




    }

    private void handleSOSButtonClick() {
        triggerCall();
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




}
