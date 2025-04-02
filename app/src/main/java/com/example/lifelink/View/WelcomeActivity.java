package com.example.lifelink.View;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.lifelink.MainActivity;
import com.example.lifelink.R;
import java.util.ArrayList;
import java.util.List;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button btnGetStarted, btnLogin;
    private TextView txtTermsPrivacy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);

        // Initialize UI components
        viewPager = findViewById(R.id.viewPager);
        btnGetStarted = findViewById(R.id.btnGetStarted);
        btnLogin = findViewById(R.id.btnLogin);
        txtTermsPrivacy = findViewById(R.id.txtTermsPrivacy);

        // Check if components exist
        if (viewPager == null || btnGetStarted == null || btnLogin == null || txtTermsPrivacy == null) {
            throw new RuntimeException("UI elements not found in welcome.xml. Check their IDs.");
        }

        // Set up ViewPager slides
        List<slideItem> slides = new ArrayList<>();
        slides.add(new slideItem(R.drawable.lifelinkim));
        slides.add(new slideItem(R.drawable.lifelinkim));
        slides.add(new slideItem(R.drawable.lifelinkim));

        slideAdapter adapter = new slideAdapter(slides);
        viewPager.setAdapter(adapter);

        // Get Started Button
        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomeActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });

        // Login Button
        btnLogin.setOnClickListener(v -> {
            try {
              //  Toast.makeText(this, "This is a Toast message", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
                startActivity(intent);

                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Clickable Terms & Privacy Policy
        String termsText = "By proceeding, you agree to our <a href='https://yourapp.com/terms'>Terms</a> " +
                "and that you have read our <a href='https://yourapp.com/privacy'>Privacy Policy</a>.";
        txtTermsPrivacy.setText(Html.fromHtml(termsText, Html.FROM_HTML_MODE_LEGACY));
        txtTermsPrivacy.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
