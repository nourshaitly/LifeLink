package com.example.lifelink.View;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);  // Use the correct layout XML file

        // Find the TextView in the layout
        TextView textView = findViewById(R.id.textView);

        // Set text for testing
        textView.setText("Welcome to Home Activity!");
    }
}
