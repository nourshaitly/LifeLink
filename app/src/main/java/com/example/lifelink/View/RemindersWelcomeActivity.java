package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;

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
    }
}
