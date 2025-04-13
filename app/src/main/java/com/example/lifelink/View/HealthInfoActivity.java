package com.example.lifelink.View;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class HealthInfoActivity extends AppCompatActivity {

    LinearLayout welcomeLayout, birthLayout, lifestyleQuestionLayout, chronicConditionsLayout;
    Button imReadyButton, birthNextButton;
    EditText birthdateEditText;
    RadioGroup genderRadioGroup;
    Spinner bloodGroupSpinner, rhFactorSpinner;
    NumberPicker heightPicker, weightPicker;

    TextView lifestyleQuestionText;
    LinearLayout option1, option2, option3, option4;
    TextView optionEmoji1, optionEmoji2, optionEmoji3, optionEmoji4;
    TextView optionText1, optionText2, optionText3, optionText4;

    String[] lifestyleQuestions = {
            "🍺 Do you drink alcohol?",
            "🚬 Do you smoke?",
            "🏃‍♂️ How often do you exercise?",
            "🛌 How many hours do you sleep?"
    };

    String[][] lifestyleOptions = {
            {"Never", "Rarely", "Sometimes", "Often"},
            {"No", "Used to", "Occasionally", "Frequently"},
            {"None", "Occasionally", "Regularly", "Daily"},
            {"<4 hrs", "5–6 hrs", "7–8 hrs", "9+ hrs"}
    };

    String[][] lifestyleEmojis = {
            {"🚫", "🍷", "🍺", "🍻"},
            {"🚫", "💨", "🚬", "🌫️"},
            {"🛌", "🚶", "🏃", "🏋️"},
            {"😴", "⏰", "💤", "🛏️"}
    };

    int currentQuestionIndex = 0;
    Map<String, String> lifestyleAnswers = new LinkedHashMap<>();

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_info);

        db = FirebaseFirestore.getInstance();

        // Get user's name from intent
        String userName = getIntent().getStringExtra("userName");
        if (userName != null) {
            TextView welcomeMessageText = findViewById(R.id.welcomeMessageText);
            welcomeMessageText.setText("👋 Welcome, " + userName + "!\nLet's build your health profile.");
        }

        // UI References
        welcomeLayout = findViewById(R.id.welcomeLayout);
        birthLayout = findViewById(R.id.birthLayout);
        lifestyleQuestionLayout = findViewById(R.id.lifestyleQuestionLayout);
        chronicConditionsLayout = findViewById(R.id.chronicConditionsLayout);

        imReadyButton = findViewById(R.id.imReadyButton);
        birthNextButton = findViewById(R.id.birthNextButton);

        birthdateEditText = findViewById(R.id.birthdateEditText);
        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        bloodGroupSpinner = findViewById(R.id.bloodGroupSpinner);
        rhFactorSpinner = findViewById(R.id.rhFactorSpinner);
        heightPicker = findViewById(R.id.heightPicker);
        weightPicker = findViewById(R.id.weightPicker);

        lifestyleQuestionText = findViewById(R.id.lifestyleQuestionText);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);

        optionEmoji1 = findViewById(R.id.optionEmoji1);
        optionEmoji2 = findViewById(R.id.optionEmoji2);
        optionEmoji3 = findViewById(R.id.optionEmoji3);
        optionEmoji4 = findViewById(R.id.optionEmoji4);

        optionText1 = findViewById(R.id.optionText1);
        optionText2 = findViewById(R.id.optionText2);
        optionText3 = findViewById(R.id.optionText3);
        optionText4 = findViewById(R.id.optionText4);

        imReadyButton.setOnClickListener(v -> {
            animateSlideOut(welcomeLayout);
            welcomeLayout.setVisibility(View.GONE);
            birthLayout.setVisibility(View.VISIBLE);
            animateSlideIn(birthLayout);
        });

        birthNextButton.setOnClickListener(v -> {
            if (!validateBirthInfo()) return;
            animateSlideOut(birthLayout);
            birthLayout.setVisibility(View.GONE);
            lifestyleQuestionLayout.setVisibility(View.VISIBLE);
            showNextLifestyleQuestion(true);
            animateSlideIn(lifestyleQuestionLayout);
        });

        birthdateEditText.setOnClickListener(v -> showDatePicker());

        setupSpinners();
        setupNumberPickers();

        View[] options = {option1, option2, option3, option4};
        for (int i = 0; i < options.length; i++) {
            final int index = i;
            options[i].setOnClickListener(v -> {
                for (View o : options) {
                    o.setBackgroundResource(R.drawable.lifestyle_option_box);
                }
                v.setBackgroundResource(R.drawable.lifestyle_option_box_selected);
                lifestyleAnswers.put(lifestyleQuestions[currentQuestionIndex], lifestyleOptions[currentQuestionIndex][index]);

                v.postDelayed(() -> {
                    currentQuestionIndex++;
                    if (currentQuestionIndex < lifestyleQuestions.length) {
                        showNextLifestyleQuestion(false);
                    } else {
                        lifestyleQuestionLayout.setVisibility(View.GONE);
                        chronicConditionsLayout.setVisibility(View.VISIBLE);
                        saveToFirestore();
                    }
                }, 200);
            });
        }
    }

    private void showNextLifestyleQuestion(boolean isFirst) {
        lifestyleQuestionText.setText(lifestyleQuestions[currentQuestionIndex]);
        optionEmoji1.setText(lifestyleEmojis[currentQuestionIndex][0]);
        optionEmoji2.setText(lifestyleEmojis[currentQuestionIndex][1]);
        optionEmoji3.setText(lifestyleEmojis[currentQuestionIndex][2]);
        optionEmoji4.setText(lifestyleEmojis[currentQuestionIndex][3]);

        optionText1.setText(lifestyleOptions[currentQuestionIndex][0]);
        optionText2.setText(lifestyleOptions[currentQuestionIndex][1]);
        optionText3.setText(lifestyleOptions[currentQuestionIndex][2]);
        optionText4.setText(lifestyleOptions[currentQuestionIndex][3]);

        if (!isFirst) animateSlideIn(lifestyleQuestionLayout);
    }

    private void saveToFirestore() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        Map<String, Object> generalInfo = new HashMap<>();
        generalInfo.put("birthdate", birthdateEditText.getText().toString().trim());

        int genderId = genderRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedGender = findViewById(genderId);
        generalInfo.put("gender", selectedGender.getText().toString());

        generalInfo.put("blood_group", bloodGroupSpinner.getSelectedItem().toString());
        generalInfo.put("rh_factor", rhFactorSpinner.getSelectedItem().toString());
        generalInfo.put("height_cm", heightPicker.getValue());
        generalInfo.put("weight_kg", weightPicker.getValue());
        generalInfo.put("lifestyle", lifestyleAnswers);

        db.collection("general_info")
                .document(uid)
                .set(generalInfo)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Health info saved!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> bloodAdapter = ArrayAdapter.createFromResource(this, R.array.blood_groups, android.R.layout.simple_spinner_item);
        bloodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bloodGroupSpinner.setAdapter(bloodAdapter);

        ArrayAdapter<CharSequence> rhAdapter = ArrayAdapter.createFromResource(this, R.array.rh_factors, android.R.layout.simple_spinner_item);
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

        DatePickerDialog dialog = new DatePickerDialog(this, (view, y, m, d) -> {
            birthdateEditText.setText(String.format("%02d/%02d/%04d", d, m + 1, y));
        }, year, month, day);

        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    private boolean validateBirthInfo() {
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

    private void animateSlideIn(View view) {
        Animation slideIn = new TranslateAnimation(300, 0, 0, 0);
        slideIn.setDuration(300);
        view.startAnimation(slideIn);
    }

    private void animateSlideOut(View view) {
        Animation slideOut = new TranslateAnimation(0, -300, 0, 0);
        slideOut.setDuration(300);
        view.startAnimation(slideOut);
    }

    private void animateFadeIn(View view) {
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);
        view.startAnimation(fadeIn);
    }
}
