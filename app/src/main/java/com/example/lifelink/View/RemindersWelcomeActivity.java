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

public class RemindersWelcomeActivity extends AppCompatActivity {

    private Button startButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders_welcome); // your XML file name

        // Find the button
        startButton = findViewById(R.id.startButton);

        // Set click listener
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the dashboard activity
                Intent intent = new Intent(RemindersWelcomeActivity.this,DashboardActivity.class);
                startActivity(intent);
                finish(); // Optional: finish welcome screen so user can't go back to it
            }
        });

        // ----- initialize all shared dashboard UI (toolbar, SOS, bottom nav) -----
        DashboardUtils.init(this, R.id.nav_reminder);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // let DashboardUtils handle the Up/Home click
        if (DashboardUtils.onHomeClicked(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
