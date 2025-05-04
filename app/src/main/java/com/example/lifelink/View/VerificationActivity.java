package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class VerificationActivity extends AppCompatActivity {

    private TextView infoText;
    private Button checkVerificationButton, resendEmailButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Handler handler;
    private Runnable verificationChecker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        infoText = findViewById(R.id.infoText);
        checkVerificationButton = findViewById(R.id.checkVerificationButton);
        resendEmailButton = findViewById(R.id.resendEmailButton);
        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        infoText.setText("A verification email has been sent to your email address.\n\nPlease check your inbox and click the verification link.\nOnce verified, press the 'Continue' button below.");

        checkVerificationButton.setOnClickListener(v -> checkEmailVerification());
        resendEmailButton.setOnClickListener(v -> resendVerificationEmail());

        // Optional: Automatically check every 10 seconds (background check)
        handler = new Handler();
        startAutoCheck();
    }

    private void checkEmailVerification() {
        progressBar.setVisibility(View.VISIBLE);

        if (currentUser != null) {
            currentUser.reload().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.GONE);

                if (task.isSuccessful()) {
                    if (currentUser.isEmailVerified()) {
                        Toast.makeText(VerificationActivity.this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
                        goToHealthInfo();
                    } else {
                        Toast.makeText(VerificationActivity.this, "Email not verified yet. Please check your inbox.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VerificationActivity.this, "Error checking verification status. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void resendVerificationEmail() {
        progressBar.setVisibility(View.VISIBLE);

        if (currentUser != null) {
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Toast.makeText(VerificationActivity.this, "Verification email resent. Please check your inbox.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(VerificationActivity.this, "Failed to resend verification email. Try again later.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void goToHealthInfo() {
        Intent intent = new Intent(VerificationActivity.this, HealthInfoActivity.class);
        startActivity(intent);
        finish();
    }

    private void startAutoCheck() {
        verificationChecker = new Runnable() {
            @Override
            public void run() {
                currentUser.reload().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && currentUser.isEmailVerified()) {
                        Toast.makeText(VerificationActivity.this, "Email verified automatically!", Toast.LENGTH_SHORT).show();
                        goToHealthInfo();
                    } else {
                        handler.postDelayed(verificationChecker, 10000); // Check again in 10 seconds
                    }
                });
            }
        };

        handler.postDelayed(verificationChecker, 10000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && verificationChecker != null) {
            handler.removeCallbacks(verificationChecker);
        }
    }
}
