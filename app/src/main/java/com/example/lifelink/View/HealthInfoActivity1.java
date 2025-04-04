package com.example.lifelink.View;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;

import java.util.Calendar;

public class HealthInfoActivity1 extends AppCompatActivity {

    TextView welcomeTextView;
    EditText birthdateEditText;
    RadioGroup genderRadioGroup;
    Spinner bloodGroupSpinner, rhFactorSpinner;
    TextView heightTextView, weightTextView;
    Button nextButton;

    String userFirstName = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_info1);

        // Get user's first name from previous activity
        if (getIntent().hasExtra("firstName")) {
            userFirstName = getIntent().getStringExtra("firstName");
        }

        welcomeTextView = findViewById(R.id.welcomeTextView);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner);
        rhFactorSpinner = findViewById(R.id.rhFactorSpinner);
        heightTextView = findViewById(R.id.heightTextView);
        weightTextView = findViewById(R.id.weightTextView);
        nextButton = findViewById(R.id.nextButton);

        // Set welcome text dynamically
        welcomeTextView.setText("Welcome to LifeLink, " + userFirstName + "!\nPlease fill in your medical details below.");
        animateWelcomeMessage(welcomeTextView);

        // Blood group spinner
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(
                this, R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        // Rh factor spinner
        ArrayAdapter<CharSequence> rhAdapter = ArrayAdapter.createFromResource(
                this, R.array.rh_factors, android.R.layout.simple_spinner_item);
        rhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rhFactorSpinner.setAdapter(rhAdapter);

        // Date picker
        birthdateEditText.setOnClickListener(v -> showDatePicker());

        // Height & Weight pickers
        heightTextView.setOnClickListener(view -> showHeightPicker());
        weightTextView.setOnClickListener(view -> showWeightPicker());

        nextButton.setOnClickListener(view -> {
            String birthdate = birthdateEditText.getText().toString().trim();
            int selectedGenderId = genderRadioGroup.getCheckedRadioButtonId();
            String gender = selectedGenderId == R.id.maleRadioButton ? "Male" : selectedGenderId == R.id.femaleRadioButton ? "Female" : "";
            String bloodType = bloodGroupSpinner.getSelectedItem().toString() + rhFactorSpinner.getSelectedItem().toString();
            String heightText = heightTextView.getText().toString().replace(" cm", "").trim();
            String weightText = weightTextView.getText().toString().replace(" kg", "").trim();

            if (birthdate.isEmpty()) {
                birthdateEditText.setError("Select your birthdate");
                return;
            }

            if (selectedGenderId == -1) {
                Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
                return;
            }

            if (bloodGroupSpinner.getSelectedItemPosition() == 0 || rhFactorSpinner.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (heightText.equals("Tap to select height") || weightText.equals("Tap to select weight")) {
                Toast.makeText(this, "Please select height and weight", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(this, HealthInfoActivity2.class);
            intent.putExtra("firstName", userFirstName);
            intent.putExtra("birthdate", birthdate);
            intent.putExtra("gender", gender);
            intent.putExtra("bloodType", bloodType);
            intent.putExtra("height", heightText);
            intent.putExtra("weight", weightText);
            startActivity(intent);
        });
    }

    private void animateWelcomeMessage(TextView textView) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1500);
        textView.startAnimation(fadeIn);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", month1 + 1, dayOfMonth, year1);
                    birthdateEditText.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.show();
    }

    private void showHeightPicker() {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(100);
        numberPicker.setMaxValue(250);
        numberPicker.setValue(170);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Height (cm)");
        builder.setView(numberPicker);
        builder.setPositiveButton("OK", (dialog, which) ->
                heightTextView.setText(numberPicker.getValue() + " cm"));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showWeightPicker() {
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(30);
        numberPicker.setMaxValue(200);
        numberPicker.setValue(70);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Weight (kg)");
        builder.setView(numberPicker);
        builder.setPositiveButton("OK", (dialog, which) ->
                weightTextView.setText(numberPicker.getValue() + " kg"));
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
}
