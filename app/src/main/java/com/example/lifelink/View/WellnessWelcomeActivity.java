package com.example.lifelink.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

public class WellnessWelcomeActivity extends AppCompatActivity {

    private Button startButton;

    private final String sosnum = "+96171010584";  // SOS Number
    private static final int CALL_PERMISSION_REQUEST = 100;
    private static final SparseArray<Class<?>> NAV_MAP = new SparseArray<>();
    static {
        NAV_MAP.put(R.id.nav_nearby, MapIntroActivity.class);
        NAV_MAP.put(R.id.nav_reminder, RemindersWelcomeActivity.class);
        NAV_MAP.put(R.id.nav_home, MainPageActivity.class);
        NAV_MAP.put(R.id.nav_emergency, EmergencyActivity.class);
        NAV_MAP.put(R.id.nav_health, HealthTrackerActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wellness_welcome); // your XML file name

        // Find the button
        startButton = findViewById(R.id.startButton);

        // Set click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the dashboard activity
                Intent intent = new Intent(WellnessWelcomeActivity.this,WellnessDashboardActivity.class);
                startActivity(intent);
                finish(); // Optional: finish welcome screen so user can't go back to it
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView profileIcon = findViewById(R.id.profileIcon);
        profileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Display back button

        // Set up SOS Floating Action Button
        setupSOSFab();
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

    private void setupSOSFab() {
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_sos); // Make sure to have this FAB in your layout
        fab.setOnClickListener(v -> triggerCall());  // Trigger the call when the SOS button is clicked
    }
    // Handle the SOS call
    private void triggerCall() {
        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + sosnum));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(callIntent);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, CALL_PERMISSION_REQUEST);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to ReminderFragment
            startActivity(new Intent(this, MainPageActivity.class));
            finish(); // Finish the current activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
