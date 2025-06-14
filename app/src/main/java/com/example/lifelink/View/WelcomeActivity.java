package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.lifelink.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class WelcomeActivity extends AppCompatActivity {
    private static final String TAG = "WelcomeActivity";
    private ViewPager2 welcomeViewPager;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();

            // Initialize views
            welcomeViewPager = findViewById(R.id.welcomeViewPager);
            loginButton = findViewById(R.id.loginButton);
            registerButton = findViewById(R.id.registerButton);

            // Set up ViewPager2 with adapter
            WelcomeSlideAdapter adapter = new WelcomeSlideAdapter();
            welcomeViewPager.setAdapter(adapter);

            // Set up button click listeners
            setupButtonListeners();

            // Check if user is already logged in
            checkUserLogin();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupButtonListeners() {
        loginButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting LoginActivity: " + e.getMessage(), e);
                Toast.makeText(this, "Error starting login. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });

        registerButton.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error starting RegisterActivity: " + e.getMessage(), e);
                Toast.makeText(this, "Error starting registration. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkUserLogin() {
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                // User is already logged in, redirect to HomeActivity
                Intent intent = new Intent(WelcomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking user login: " + e.getMessage(), e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
    }
}
