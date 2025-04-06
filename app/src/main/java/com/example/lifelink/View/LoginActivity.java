package com.example.lifelink.View;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private ImageView eyeIcon;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;
    private TextView forgotPasswordText, signUpText;
    private boolean isPasswordVisible = false;
    private String userIdentifier;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        eyeIcon = findViewById(R.id.eyeIcon);
        loginButton = findViewById(R.id.loginButton);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signUpText = findViewById(R.id.signUpText);

        eyeIcon.setOnClickListener(v -> togglePasswordVisibility());

        loginButton.setOnClickListener(v -> handleLogin());

        forgotPasswordText.setOnClickListener(v -> showVerificationChoiceDialog());

        signUpText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            eyeIcon.setImageResource(R.drawable.baseline_add_24);
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            eyeIcon.setImageResource(R.drawable.baseline_add_box_24);
        }
        passwordEditText.setSelection(passwordEditText.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showVerificationChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Verification Method");

        String[] options = {"Email", "SMS"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                userIdentifier = emailEditText.getText().toString();
                sendVerificationCode("email");
            } else {
                userIdentifier = "+961123456";
                sendVerificationCode("sms");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void sendVerificationCode(String method) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Verification Code");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(20, 20, 20, 20);

        EditText[] codeInputs = new EditText[5];
        for (int i = 0; i < 5; i++) {
            codeInputs[i] = new EditText(this);
            codeInputs[i].setLayoutParams(new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT));
            codeInputs[i].setGravity(Gravity.CENTER);
            codeInputs[i].setInputType(InputType.TYPE_CLASS_NUMBER);
            codeInputs[i].setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
            layout.addView(codeInputs[i]);

            final int index = i;
            codeInputs[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < codeInputs.length - 1) {
                        codeInputs[index + 1].requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        builder.setView(layout);
        builder.setPositiveButton("Submit", (dialog, which) -> {
            StringBuilder code = new StringBuilder();
            for (EditText input : codeInputs) {
                code.append(input.getText().toString());
            }

            if (code.length() == 5) {
                if (isValidVerificationCode(code.toString())) {
                    openResetPasswordScreen();
                } else {
                    Toast.makeText(this, "Incorrect code. Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter all 5 digits.", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private boolean isValidVerificationCode(String enteredCode) {
        return "12345".equals(enteredCode);
    }

    private void openResetPasswordScreen() {
        Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        intent.putExtra("userIdentifier", userIdentifier);
        startActivity(intent);
    }
}
