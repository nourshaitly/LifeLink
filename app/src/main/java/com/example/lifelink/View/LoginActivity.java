package com.example.lifelink.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ImageView eyeIcon;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText, signUpText;
    private boolean isPasswordVisible = false;
    private String userIdentifier; // Stores user email or phone

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize UI components
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        eyeIcon = findViewById(R.id.eyeIcon);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signUpText = findViewById(R.id.signUpText);

        // Eye icon click listener to show/hide password
        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        // Login button click listener
        loginButton.setOnClickListener(v -> handleLogin());

        // Forgot Password click listener (Added AlertDialog)
        forgotPasswordText.setOnClickListener(v -> showVerificationChoiceDialog());

        // SignUp click listener to navigate to Registration Activity
        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Toggle password visibility
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.baseline_add_24);  // Change to closed eye
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.baseline_add_box_24);  // Change to open eye
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    // Handle Login action
    private void handleLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        // Perform validation
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save credentials for "Remember Me" functionality (this is just a simple placeholder)
        if (rememberMeCheckBox.isChecked()) {
            // Save credentials (You can use SharedPreferences or a database here)
        }

        // After successful login, navigate to Home Activity
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    // Show Verification Method Dialog
    private void showVerificationChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Verification Method");

        // Options
        String[] options = {"Email", "SMS"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                userIdentifier = "example@email.com"; // Fetch actual email from DB
                sendVerificationCode("email");
            } else {
                userIdentifier = "+961 123 456"; // Fetch actual phone number from DB
                sendVerificationCode("sms");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Show Verification Code Input Dialog
    private void sendVerificationCode(String method) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");

        // Layout container
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);

        // Create 5 EditTexts for the code
        EditText[] codeInputs = new EditText[5];
        for (int i = 0; i < 5; i++) {
            codeInputs[i] = new EditText(this);
            codeInputs[i].setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));
            codeInputs[i].setGravity(Gravity.CENTER);
            codeInputs[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            codeInputs[i].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            layout.addView(codeInputs[i]);

            // Auto-focus next EditText after each input
            final int index = i;
            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                    if (charSequence.length() == 1 && index < codeInputs.length - 1) {
                        codeInputs[index + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {}
            });
        }

        builder.setView(layout);

        // Submit button
        builder.setPositiveButton("Submit", (dialog, which) -> {
            StringBuilder code = new StringBuilder();
            for (EditText input : codeInputs) {
                code.append(input.getText().toString());
            }

            // Validate the entered code
            if (code.length() == 5) {
                String enteredCode = code.toString();
                if (isValidVerificationCode(enteredCode)) {
                    openResetPasswordScreen();
                } else {
                    Toast.makeText(this, "Incorrect verification code. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter all 5 digits.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Dummy method to validate the entered verification code
    private boolean isValidVerificationCode(String enteredCode) {
        String correctCode = "12345"; // The correct code (this should be dynamically generated and sent to the user)
        return correctCode.equals(enteredCode);
    }

    // Open Reset Password Screen
    private void openResetPasswordScreen() {
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        intent.putExtra("userIdentifier", userIdentifier);
        startActivity(intent);
    }
}
