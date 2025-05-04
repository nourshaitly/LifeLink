package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelink.R;
import com.example.lifelink.Service.BluetoothMonitorService;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeMessage;
    private MaterialButton logoutButton;
    private MaterialButton buildProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "HomePage Launched", Toast.LENGTH_SHORT).show(); // ✅ For debug
        setContentView(R.layout.home);



        try {
            // Try starting Bluetooth Monitor Service
            Intent serviceIntent = new Intent(this, BluetoothMonitorService.class);
            startService(serviceIntent);
            Toast.makeText(this, "✅ Bluetooth Monitoring Started", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "❌ Failed to Start Monitoring: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

















        try {
            // Initialize Firebase Auth
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Initialize views
            welcomeMessage = findViewById(R.id.welcomeMessage);
            logoutButton = findViewById(R.id.logoutButton);
            buildProfileButton = findViewById(R.id.buildProfileButton);

            // Get current user
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {


                String uid = currentUser.getUid();
                Toast.makeText(this, "🔑 UID: " + uid, Toast.LENGTH_SHORT).show();

                db.collection("users").document(uid)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            Toast.makeText(this, "📥 Data fetched!", Toast.LENGTH_SHORT).show();

                            if (documentSnapshot.exists()) {
                                String firstName = documentSnapshot.getString("firstName");
                                String lastName = documentSnapshot.getString("lastName");

                                if (firstName != null && lastName != null) {
                                    String fullName = firstName + " " + lastName;
                                    welcomeMessage.setText("Welcome, " + fullName + "!");
                                    Toast.makeText(this, "🎉 Welcome " + fullName, Toast.LENGTH_LONG).show();
                                } else {
                                    welcomeMessage.setText("Welcome (missing name)");
                                    Toast.makeText(this, "⚠️ Name fields missing in DB", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                welcomeMessage.setText("Welcome!");
                                Toast.makeText(this, "❌ No document found!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            welcomeMessage.setText("Welcome!");
                            Toast.makeText(this, "🔥 Failed to fetch: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            } else {
                welcomeMessage.setText("Welcome!");
                Toast.makeText(this, "⚠️ No logged-in user", Toast.LENGTH_SHORT).show();
            }

            // Set up logout button
            logoutButton.setOnClickListener(v -> {
                try {
                    mAuth.signOut();
                    Toast.makeText(HomeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    
                    // Go back to WelcomeActivity
                    Intent intent = new Intent(HomeActivity.this, WelcomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } catch (Exception e) {
                    Log.e(TAG, "Error during logout: " + e.getMessage(), e);
                    Toast.makeText(HomeActivity.this, "Error during logout: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Set up build profile button
            buildProfileButton.setOnClickListener(v -> {
                try {
                    // Navigate to HeartConditionOptionsActivity
                    Intent intent = new Intent(HomeActivity.this, HeartConditionOptionsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting HeartConditionOptionsActivity: " + e.getMessage(), e);
                    Toast.makeText(this, "Error starting health profile. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "An error occurred. Please try again.", Toast.LENGTH_SHORT).show();
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
