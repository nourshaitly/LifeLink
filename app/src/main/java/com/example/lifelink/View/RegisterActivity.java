package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, middleName, lastName, phoneNumber, email, password, confirmPassword;
    private Button registerButton;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize input fields
        firstName = findViewById(R.id.firstName);
        middleName = findViewById(R.id.middleName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);

        registerButton.setOnClickListener(v -> handleRegistration());
    }

    private void handleRegistration() {
        String first = firstName.getText().toString().trim();
        String middle = middleName.getText().toString().trim();
        String last = lastName.getText().toString().trim();
        String phone = phoneNumber.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String pass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || phone.isEmpty() || emailText.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("firstName", first);
                            userMap.put("middleName", middle);
                            userMap.put("lastName", last);
                            userMap.put("phoneNumber", phone);
                            userMap.put("email", emailText);

                            db.collection("users").document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();

                                        // ✅ Add delay before moving to next activity
                                        new Handler().postDelayed(() -> {
                                            Intent intent = new Intent(RegisterActivity.this, HealthInfoActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }, 300); // 300ms delay

                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
