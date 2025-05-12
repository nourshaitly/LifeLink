package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
    private TextView loginText;
    private ProgressBar loadingProgressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String first, middle, last, phone, emailText, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        firstName = findViewById(R.id.firstName);
        middleName = findViewById(R.id.middleName);
        lastName = findViewById(R.id.lastName);
        phoneNumber = findViewById(R.id.phoneNumber);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);
      //  loadingProgressBar = findViewById(R.id.loadingProgressBar); // ✅ Fixed here (uncommented)

        registerButton.setOnClickListener(v -> handleRegistration());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void handleRegistration() {
        first = firstName.getText().toString().trim();
        middle = middleName.getText().toString().trim();
        last = lastName.getText().toString().trim();
        phone = phoneNumber.getText().toString().trim();
        emailText = email.getText().toString().trim();
        pass = password.getText().toString().trim();
        String confirmPassText = confirmPassword.getText().toString().trim();

        if (first.isEmpty() || last.isEmpty() || phone.isEmpty() || emailText.isEmpty() || pass.isEmpty() || confirmPassText.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(emailText)) {
            Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPassText)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

      //  loadingProgressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailText, pass)
                .addOnCompleteListener(this, task -> {
               //     loadingProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            saveUserInFirestore(firebaseUser.getUid());
                                        } else {
                                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserInFirestore(String uid) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", first);
        userMap.put("middleName", middle);
        userMap.put("lastName", last);
        userMap.put("phoneNumber", phone);
        userMap.put("email", emailText);

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Registration successful! Verification email sent.\nPlease check your inbox or spam folder.", Toast.LENGTH_LONG).show();

                    // Open VerificationActivity
                    Intent intent = new Intent(RegisterActivity.this, VerificationActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidEmail(String email) {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
