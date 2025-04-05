package com.example.lifelink.View;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.example.lifelink.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HealthInfoActivity extends AppCompatActivity {

    LinearLayout welcomeLayout;
    ScrollView healthInfoLayout, chronicConditionsLayout;

    // Step 1 views
    TextView welcomeTextView;
    EditText birthdateEditText, otherConditionEditText;
    RadioGroup genderRadioGroup;
    Spinner bloodGroupSpinner, rhFactorSpinner;
    NumberPicker heightPicker, weightPicker;
    Button imReadyButton, nextButton;

    // Step 2 card views
    CardView cardHypertension, cardHypotension, cardDiabetes, cardHeart, cardAsthma, cardOther;
    EditText otherConditionInput;
    List<CardView> selectedCards = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_info);

        // Layouts
        welcomeLayout = findViewById(R.id.welcomeLayout);
        healthInfoLayout = findViewById(R.id.healthInfoLayout);
        chronicConditionsLayout = findViewById(R.id.chronicConditionsLayout);

        // Step 1 UI
        welcomeTextView = findViewById(R.id.welcomeTextView);
        birthdateEditText = findViewById(R.id.birthdateEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner);
        rhFactorSpinner = findViewById(R.id.rhFactorSpinner);
        heightPicker = findViewById(R.id.heightPicker);
        weightPicker = findViewById(R.id.weightPicker);
        imReadyButton = findViewById(R.id.imReadyButton);

        otherConditionEditText = findViewById(R.id.otherConditionEditText);

        nextButton = findViewById(R.id.nextButton);


        // Step 2 UI
        cardHypertension = findViewById(R.id.cardHypertension);
        cardHypotension = findViewById(R.id.cardHypotension);
        cardDiabetes = findViewById(R.id.cardDiabetes);
        cardHeart = findViewById(R.id.cardHeartDisease);
        cardAsthma = findViewById(R.id.cardAsthma);
        cardOther = findViewById(R.id.cardOther);
        otherConditionInput = findViewById(R.id.otherConditionEditText);

        animateWelcomeMessage(welcomeTextView);
        setupSpinners();
        setupNumberPickers();

        birthdateEditText.setOnClickListener(v -> showDatePicker());

        imReadyButton.setOnClickListener(v -> {
            welcomeLayout.setVisibility(View.GONE);
            healthInfoLayout.setVisibility(View.VISIBLE);
        });

        nextButton.setOnClickListener(v -> {
            if (!validateHealthInfo()) return;
            healthInfoLayout.setVisibility(View.GONE);
            chronicConditionsLayout.setVisibility(View.VISIBLE);
        });

        setupCardSelection();
    }

    private void animateWelcomeMessage(TextView textView) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1200);
        textView.startAnimation(fadeIn);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(
                this, R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        ArrayAdapter<CharSequence> rhAdapter = ArrayAdapter.createFromResource(
                this, R.array.rh_factors, android.R.layout.simple_spinner_item);
        rhAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rhFactorSpinner.setAdapter(rhAdapter);
    }

    private void setupNumberPickers() {
        heightPicker.setMinValue(100);
        heightPicker.setMaxValue(250);
        heightPicker.setValue(170);
        heightPicker.setWrapSelectorWheel(false);

        weightPicker.setMinValue(30);
        weightPicker.setMaxValue(200);
        weightPicker.setValue(70);
        weightPicker.setWrapSelectorWheel(false);
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    birthdateEditText.setText(formattedDate);
                },
                year, month, day
        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateHealthInfo() {
        String birthdate = birthdateEditText.getText().toString().trim();
        if (!birthdate.matches("\\d{2}/\\d{2}/\\d{4}")) {
            birthdateEditText.setError("Enter a valid date (DD/MM/YYYY)");
            return false;
        }

        if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (bloodGroupSpinner.getSelectedItemPosition() == 0 || rhFactorSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void setupCardSelection() {
        setupToggleCard(cardHypertension);
        setupToggleCard(cardHypotension);
        setupToggleCard(cardDiabetes);
        setupToggleCard(cardHeart);
        setupToggleCard(cardAsthma);

        cardOther.setOnClickListener(v -> {
            if (v.isSelected()) {
                v.setSelected(false);
                //v.setCardBackgroundColor(Color.WHITE);
                otherConditionInput.setVisibility(View.GONE);
            } else {
                v.setSelected(true);
                //v.setCardBackgroundColor(ContextCompat.getColor(this, R.color.teal_200));
                otherConditionInput.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupToggleCard(CardView card) {
        card.setOnClickListener(v -> {
            boolean selected = card.isSelected();
            card.setSelected(!selected);
            //card.setCardBackgroundColor(selected ? Color.WHITE : ContextCompat.getColor(this, R.color.teal_200));
        });
    }
}
