package com.example.lifelink.View;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        textView = findViewById(R.id.textView);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Toast.makeText(this, "✅ User is logged in", Toast.LENGTH_SHORT).show();

            String uid = currentUser.getUid();
            Toast.makeText(this, "🔑 UID: " + uid, Toast.LENGTH_SHORT).show();

            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Toast.makeText(this, "📥 Data fetched!", Toast.LENGTH_SHORT).show();

                        if (documentSnapshot.exists()) {
                            String firstName = documentSnapshot.getString("firstName");
                            String lastName = documentSnapshot.getString("lastName");

                            if (firstName != null && lastName != null) {
                                String fullName = firstName + " " + lastName;
                                textView.setText("Welcome, " + fullName + "!");
                                Toast.makeText(this, "🎉 Welcome " + fullName, Toast.LENGTH_LONG).show();
                            } else {
                                textView.setText("Welcome (missing name)");
                                Toast.makeText(this, "⚠️ Name fields missing in DB", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            textView.setText("Welcome!");
                            Toast.makeText(this, "❌ No document found!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        textView.setText("Welcome!");
                        Toast.makeText(this, "🔥 Failed to fetch: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            textView.setText("Welcome!");
            Toast.makeText(this, "⚠️ No logged-in user", Toast.LENGTH_SHORT).show();
        }
    }
}
