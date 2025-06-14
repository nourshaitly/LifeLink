package com.example.lifelink.View;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText inputEmail, inputFirstName, inputMiddleName, inputLastName, inputBirthdate;
    private Spinner spinnerGender, spinnerBloodType;
    private EditText inputContactName, inputContactRelation, inputContactPhone;
    private TextView textLastSync, textAppVersion, textDeviceModel;
    private Button buttonEditSave, buttonLogout;

    private boolean isEditMode = false;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        initializeViews();

        textAppVersion.setText(getAppVersion());
        textDeviceModel.setText(android.os.Build.MODEL);
        textLastSync.setText("Not Synced");

        setupSpinners();

        loadProfile();

        setEditMode(false);

        buttonEditSave.setOnClickListener(v -> {
            isEditMode = !isEditMode;
            if (isEditMode) {
                setEditMode(true);
                buttonEditSave.setText("Save");
            } else {
                saveProfile();
            }
        });

        inputBirthdate.setOnClickListener(v -> {
            if (isEditMode) showDatePicker();
        });

        buttonLogout.setOnClickListener(v -> showLogoutDialog());

        // 1) Find and set the toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }

    private void initializeViews() {
        inputEmail = findViewById(R.id.inputEmail);
        inputFirstName = findViewById(R.id.inputFirstName);
        inputMiddleName = findViewById(R.id.inputMiddleName);
        inputLastName = findViewById(R.id.inputLastName);
        inputBirthdate = findViewById(R.id.inputBirthdate);
        spinnerGender = findViewById(R.id.spinnerGender);
        spinnerBloodType = findViewById(R.id.spinnerBloodType);
        inputContactName = findViewById(R.id.inputContactName);
        inputContactRelation = findViewById(R.id.inputContactRelation);
        inputContactPhone = findViewById(R.id.inputContactPhone);

        textLastSync = findViewById(R.id.textLastSync);
        textAppVersion = findViewById(R.id.textAppVersion);
        textDeviceModel = findViewById(R.id.textDeviceModel);

        buttonEditSave = findViewById(R.id.buttonEditSave);
        buttonLogout = findViewById(R.id.buttonLogout);
    }

    private void setEditMode(boolean enable) {
        inputEmail.setEnabled(enable);
        inputFirstName.setEnabled(enable);
        inputMiddleName.setEnabled(enable);
        inputLastName.setEnabled(enable);
        inputBirthdate.setEnabled(enable);
        spinnerGender.setEnabled(enable);
        spinnerBloodType.setEnabled(enable);
        inputContactName.setEnabled(enable);
        inputContactRelation.setEnabled(enable);
        inputContactPhone.setEnabled(enable);
    }

    private void setupSpinners() {
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Male", "Female", "Other"});
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        ArrayAdapter<String> bloodAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"A", "B", "AB", "O"});
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBloodType.setAdapter(bloodAdapter);
    }

    // Load profile data from both users and medical_profile collections
    private void loadProfile() {
        // Load personal info from users collection
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(userSnapshot -> {
                    if (userSnapshot.exists()) {
                        inputEmail.setText(userSnapshot.getString("email"));
                        inputFirstName.setText(userSnapshot.getString("firstName"));
                        inputMiddleName.setText(userSnapshot.getString("middleName"));
                        inputLastName.setText(userSnapshot.getString("lastName"));
                    }

                    // Build full name (fallback if fields missing)
                    String fullName = (userSnapshot.getString("firstName") + " " +
                            userSnapshot.getString("middleName") + " " +
                            userSnapshot.getString("lastName")).trim();

                    // Load medical profile info after personal info
                    db.collection("users").document(uid).collection("medical_profile").document("profile")
                            .get()
                            .addOnSuccessListener(profileSnapshot -> {
                                if (profileSnapshot.exists()) {
                                    inputBirthdate.setText(profileSnapshot.getString("birthdate"));
                                    inputContactName.setText(profileSnapshot.getString("emergency_name"));
                                    inputContactRelation.setText(profileSnapshot.getString("emergency_relation"));
                                    inputContactPhone.setText(profileSnapshot.getString("emergency_phone"));

                                    String gender = profileSnapshot.getString("gender");
                                    String blood = profileSnapshot.getString("blood_type");

                                    if (gender != null) spinnerGender.setSelection(((ArrayAdapter<String>) spinnerGender.getAdapter()).getPosition(gender));
                                    if (blood != null) spinnerBloodType.setSelection(((ArrayAdapter<String>) spinnerBloodType.getAdapter()).getPosition(blood));

                                }
                            });
                });
    }

    private void saveProfile() {

        // ---- User Info (users/{uid})
        Map<String, Object> userInfo = new HashMap<>();
        if (!inputEmail.getText().toString().isEmpty()) userInfo.put("email", inputEmail.getText().toString());
        if (!inputFirstName.getText().toString().isEmpty()) userInfo.put("firstName", inputFirstName.getText().toString());
        if (!inputMiddleName.getText().toString().isEmpty()) userInfo.put("middleName", inputMiddleName.getText().toString());
        if (!inputLastName.getText().toString().isEmpty()) userInfo.put("lastName", inputLastName.getText().toString());

        db.collection("users").document(uid)
                .set(userInfo, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    // Success for user info
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


        // ---- Medical Profile (users/{uid}/medical_profile/profile)
        Map<String, Object> profileData = new HashMap<>();
        if (!inputBirthdate.getText().toString().isEmpty()) profileData.put("birthdate", inputBirthdate.getText().toString());
        profileData.put("gender", spinnerGender.getSelectedItem().toString());
        profileData.put("blood_type", spinnerBloodType.getSelectedItem().toString());
        if (!inputContactName.getText().toString().isEmpty()) profileData.put("emergency_name", inputContactName.getText().toString());
        if (!inputContactRelation.getText().toString().isEmpty()) profileData.put("emergency_relation", inputContactRelation.getText().toString());
        if (!inputContactPhone.getText().toString().isEmpty()) profileData.put("emergency_phone", inputContactPhone.getText().toString());

        db.collection("users").document(uid).collection("medical_profile").document("profile")
                .set(profileData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved successfully!", Toast.LENGTH_SHORT).show();
                    setEditMode(false);
                    buttonEditSave.setText("Edit Profile");

                    String syncTime = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Calendar.getInstance().getTime());
                    textLastSync.setText(syncTime);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            inputBirthdate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePicker.show();
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> finish())
                .setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.LogoRed));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.gray));
    }

    private String getAppVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "1.0";
        }
    }

   @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back to ReminderFragment
            startActivity(new Intent(this, MainPageActivity.class));
            finish(); // Finish the current activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}