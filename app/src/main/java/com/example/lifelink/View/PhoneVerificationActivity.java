package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {

    private EditText otpInput;
    private Button verifyOtpButton, resendOtpButton;
    private TextView infoTextView;
    private ProgressBar progressBar;
    private String verificationId;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String firstName, middleName, lastName, phoneNumber, email, password;
    private CountDownTimer countDownTimer;
    private static final long RESEND_TIMEOUT = 30000; // 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        otpInput = findViewById(R.id.otpInput);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        resendOtpButton = findViewById(R.id.resendOtpButton);
        infoTextView = findViewById(R.id.infoTextView);
        progressBar = findViewById(R.id.progressBar);

        firstName = getIntent().getStringExtra("firstName");
        middleName = getIntent().getStringExtra("middleName");
        lastName = getIntent().getStringExtra("lastName");
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

        sendOtp(phoneNumber);

        verifyOtpButton.setOnClickListener(v -> {
            String code = otpInput.getText().toString().trim();
            if (code.isEmpty() || code.length() < 6) {
                otpInput.setError("Enter valid 6-digit code");
                otpInput.requestFocus();
                return;
            }
            verifyOtp(code);
        });

        resendOtpButton.setOnClickListener(v -> sendOtp(phoneNumber));
    }

    private void sendOtp(String phoneNumber) {
        progressBar.setVisibility(View.VISIBLE);
        infoTextView.setText("Sending verification code...");

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+961" + phoneNumber.substring(1)) // Lebanon
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);

        startResendCountdown();
    }

    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            String code = credential.getSmsCode();
            if (code != null) {
                otpInput.setText(code);
                verifyOtp(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            progressBar.setVisibility(View.GONE);
            infoTextView.setText("Verification failed: " + e.getMessage());
            Toast.makeText(PhoneVerificationActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(verId, token);
            progressBar.setVisibility(View.GONE);
            verificationId = verId;
            infoTextView.setText("Enter the code manually if not auto-detected.");
            Toast.makeText(PhoneVerificationActivity.this, "OTP sent. Please check SMS.", Toast.LENGTH_SHORT).show();
        }
    };

    private void verifyOtp(String code) {
        if (verificationId != null) {
            progressBar.setVisibility(View.VISIBLE);
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        checkEmailVerification();
                    } else {
                        infoTextView.setText("Verification failed. Please try again.");
                        Toast.makeText(this, "OTP Verification failed. Try again.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkEmailVerification() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    createRealEmailUser();
                } else {
                    Toast.makeText(this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void createRealEmailUser() {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserInDatabase();
                    } else {
                        Toast.makeText(this, "Failed to create account: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserInDatabase() {
        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(this, "Error: No authenticated user.", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseUser.getUid();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("firstName", firstName);
        userMap.put("middleName", middleName);
        userMap.put("lastName", lastName);
        userMap.put("phoneNumber", phoneNumber);
        userMap.put("email", email);

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    showSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void showSuccessDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("🎉 Verification Successful")
                .setMessage("Welcome, your account has been verified successfully!")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    Intent intent = new Intent(PhoneVerificationActivity.this, HealthInfoActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private void startResendCountdown() {
        resendOtpButton.setEnabled(false);
        resendOtpButton.setText("Resend in 30s");

        countDownTimer = new CountDownTimer(RESEND_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                resendOtpButton.setText("Resend in " + millisUntilFinished / 1000 + "s");
            }

            public void onFinish() {
                resendOtpButton.setEnabled(true);
                resendOtpButton.setText("Resend OTP");
            }
        }.start();
    }
}
