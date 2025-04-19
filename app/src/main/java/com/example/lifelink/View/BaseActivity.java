package com.example.lifelink.View;

import android.content.Intent;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.lifelink.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupToolbar(String title) {
        Toolbar toolbar = findViewById(R.id.lifelinkToolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        ImageView profileIcon = findViewById(R.id.profileIcon);

        toolbarTitle.setText(title);

        profileIcon.setOnClickListener(v -> {
            Log.d("ToolbarClick", "Launching ProfileActivity...");
            Toast.makeText(this, "Opening ProfileActivity", Toast.LENGTH_SHORT).show();

            try {
                Intent intent = new Intent(profileIcon.getContext(), ProfileActivity.class);
                profileIcon.getContext().startActivity(intent);
            } catch (Exception e) {
                Log.e("ToolbarClick", "Failed to start ProfileActivity: " + e.getMessage(), e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }}
