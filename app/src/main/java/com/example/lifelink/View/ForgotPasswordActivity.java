package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendResetButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        sendResetButton = findViewById(R.id.sendResetButton);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();

        // Button click → Send reset email
        sendResetButton.setOnClickListener(v -> handleResetPassword());
    }
    @Override
    protected void onStart() {
        super.onStart();

        // ✅ Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            // 🚨 User is logged out → Go to login page
            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void handleResetPassword() {
        String email = emailEditText.getText().toString().trim();
        db = FirebaseFirestore.getInstance();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email exists in Firebase Auth

        // ✅ Check Firestore users collection if email exists
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ✅ Email exists → Send reset email
                        sendResetButton.setOnClickListener(v ->sendResetEmail(email));;
                    } else {
                        // ❌ Email not registered
                        Toast.makeText(this, "This email is not registered in our system.", Toast.LENGTH_SHORT).show();
                    }

                });
    }

    private void sendResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent! Please check your inbox or spam folder.", Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(this, "Failed to send reset email. Please try again later.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}