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

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Service account credentials
    private static final String SERVICE_EMAIL = "forgot@lifelink.app";
    private static final String SERVICE_PASSWORD = "StrongTempPass123"; // Store securely in production

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        emailEditText = findViewById(R.id.emailEditText);
        sendResetButton = findViewById(R.id.sendResetButton);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        sendResetButton.setOnClickListener(v -> handleResetPassword());
    }

    private void handleResetPassword() {
        String email = emailEditText.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sign in as service account to access Firestore
        mAuth.signInWithEmailAndPassword(SERVICE_EMAIL, SERVICE_PASSWORD)
                .addOnSuccessListener(authResult -> checkEmailInFirestore(email))
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Internal service login failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }

    private void checkEmailInFirestore(String userEmail) {
        db.collection("users")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        // ✅ Found user in Firestore, send reset email
                        sendResetEmail(userEmail);
                    } else {
                        Toast.makeText(this, "This email is not registered in our system.", Toast.LENGTH_SHORT).show();
                    }

                    // Optional: sign out the service account
                    FirebaseAuth.getInstance().signOut();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Firestore access error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    FirebaseAuth.getInstance().signOut();
                });
    }

    private void sendResetEmail(String email) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Reset link sent! Please check your inbox.", Toast.LENGTH_LONG).show();

                        // ✅ Redirect to LoginActivity
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Prevent going back to forgot password


                } else {
                        Toast.makeText(this, "Failed to send reset email. Try again later.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error sending reset email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                });
    }
}
