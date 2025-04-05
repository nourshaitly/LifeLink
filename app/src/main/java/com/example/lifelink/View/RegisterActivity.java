package com.example.lifelink.View;
import static android.content.ContentValues.TAG;

import com.example.lifelink.Model.User;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class RegisterActivity extends AppCompatActivity {

    private EditText firstName, middleName, lastName, phoneNumber, email, password, confirmPassword;
    private Button registerButton;
    private Button signInButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        // Initialize Firebase Authentication and Firestore
        auth = FirebaseAuth.getInstance();
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








        // Set listener for the Register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegistration();
            }
        });
    }

    // Registration method with email/password authentication
    private void handleRegistration() {
        String firstNameText = firstName.getText().toString().trim();
        String middleNameText = middleName.getText().toString().trim();
        String lastNameText = lastName.getText().toString().trim();
        String phoneNumberText = phoneNumber.getText().toString().trim();
        String emailText = email.getText().toString().trim();
        String passwordText = password.getText().toString().trim();
        String confirmPasswordText = confirmPassword.getText().toString().trim();

        // 1. Validate that all fields are filled
        if (firstNameText.isEmpty() || middleNameText.isEmpty() || lastNameText.isEmpty() || phoneNumberText.isEmpty() ||
                emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validate email format
        if (!emailText.contains("@") || !emailText.contains(".")) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Validate password length and strength
        if (passwordText.length() < 8) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters long.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passwordText.matches(".*[A-Z].*") || !passwordText.matches(".*[a-z].*") || !passwordText.matches(".*[0-9].*")) {
            Toast.makeText(RegisterActivity.this, "Password must contain at least one uppercase letter, one lowercase letter, and one number.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: Validate password for special characters (e.g., !@#$%^&*)
        if (!passwordText.matches(".*[!@#$%^&*].*")) {
            Toast.makeText(RegisterActivity.this, "Password must contain at least one special character (e.g., !@#$%^&*).", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Validate that password and confirm password match
        if (!passwordText.equals(confirmPasswordText)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Validate Lebanese phone number format
        String phoneRegex = "^\\+961(70|71|76|78|79|81)\\d{6}$";
        if (!phoneNumberText.matches(phoneRegex)) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid Lebanese phone number (+961 70 123 456).", Toast.LENGTH_SHORT).show();
            return;
        }

        // Optional: Validate name fields to prevent special characters
        if (!firstNameText.matches("[a-zA-Z ]+")) {
            Toast.makeText(RegisterActivity.this, "First name should only contain letters and spaces.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!middleNameText.matches("[a-zA-Z ]*")) {
            Toast.makeText(RegisterActivity.this, "Middle name should only contain letters and spaces.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!lastNameText.matches("[a-zA-Z ]+")) {
            Toast.makeText(RegisterActivity.this, "Last name should only contain letters and spaces.", Toast.LENGTH_SHORT).show();
            return;
        }


        signInButton = findViewById(R.id.googleSignUpButton);

        if (signInButton == null) {
            Log.e(TAG, "Button not found!");
            Toast.makeText(RegisterActivity.this, "yes", Toast.LENGTH_SHORT);
        } else {
            Log.d(TAG, "Button found!");
            signInButton.setOnClickListener(v -> {
                Log.d(TAG, "Button clicked");
                Toast.makeText(RegisterActivity.this, "Not", Toast.LENGTH_SHORT).show();

            });
        }













        // If all validations pass, proceed with Firebase Authentication
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // If registration is successful, save user data to Firestore
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Create a User object with the data
                                User newUser = new User(firstNameText, middleNameText, lastNameText, phoneNumberText, emailText);

                                // Save the user data to Firestore
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                db.collection("users")
                                        .document(user.getUid()) // Use the user ID as the document ID
                                        .set(newUser) // Store the User object
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Data was saved successfully
                                                Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                                                // Navigate to the Home page
                                                startActivity(new Intent(RegisterActivity.this, HealthInfoActivity.class));
                                                finish();  // Close the RegisterActivity
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle Firestore saving error
                                                Toast.makeText(RegisterActivity.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        } else {
                            // If registration fails, show a message to the user
                            Toast.makeText(RegisterActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }}
