package com.example.lifelink.View;



import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPassword, confirmNewPassword;
    private Button cancelButton, nextButton;
    private TextView userInfoText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        userInfoText = findViewById(R.id.userInfoText);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);
        cancelButton = findViewById(R.id.cancelButton);
        nextButton = findViewById(R.id.nextButton);

        // Get user email/phone from intent
        String userIdentifier = getIntent().getStringExtra("userIdentifier");
        userInfoText.setText(userIdentifier);

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        nextButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
