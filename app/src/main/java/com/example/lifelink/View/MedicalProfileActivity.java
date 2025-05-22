package com.example.lifelink.View;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.MainActivity;
import com.example.lifelink.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.*;

public class MedicalProfileActivity extends AppCompatActivity {

    private TextInputEditText birthdateEditText, emergencyNameInput, emergencyPhoneInput, otherRelationInput;
    private TextInputEditText medicationListInput, allergyInput;
    private TextInputLayout birthdateInputLayout, medicationListLayout, allergyInputLayout;
    private TextInputLayout emergencyNameLayout, emergencyPhoneLayout, otherRelationLayout;

    private RadioGroup genderRadioGroup, medicationUsageGroup, allergyGroup, relationshipRadioGroup, smokingGroup, alcoholGroup;
    private ChipGroup bloodTypeChipGroup, rhFactorChipGroup;

    private CheckBox checkChestPain, checkBreathless, checkDizziness, checkFatigue, checkPalpitations, checkNoSymptoms;
    private CheckBox familyHeartDisease, familyDiabetes, familyHypertension, familyStroke, familyOther, checkNofamily;
    private CheckBox mentalDepression, mentalAnxiety, mentalPTSD, checkNoMental;
    private CheckBox checkDiabetes, checkHypertension, checkAsthma, checkNoChronic, checkAnemia;

    private Button nextToQuestionsButton, medicalProfileButton, backToPhy, backToMP, submitProfileButton,submit;

    private String selectedGender = "", selectedBloodType = "", selectedRhFactor = "";

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_profile);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        initViews();
        setupGenderSelection();
        setupChipGroups();
        setupBirthdatePicker();

        NumberPicker heightPicker = findViewById(R.id.heightPicker);
        NumberPicker weightPicker = findViewById(R.id.weightPicker);
        setupNumberPickers(heightPicker, weightPicker);

        nextToQuestionsButton.setOnClickListener(v -> {
            if (!validatePhysiologicalInfo(heightPicker, weightPicker)) return;
            findViewById(R.id.stepPhysiological).setVisibility(View.GONE);
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.VISIBLE);
        });

        backToPhy.setOnClickListener(v -> {
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.GONE);
            findViewById(R.id.stepPhysiological).setVisibility(View.VISIBLE);
        });

        medicalProfileButton.setOnClickListener(v -> {
            if (!validateMedicalQuestions()) return;
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.GONE);
            findViewById(R.id.stepEmergencyContact).setVisibility(View.VISIBLE);
        });

        backToMP.setOnClickListener(v -> {
            findViewById(R.id.stepEmergencyContact).setVisibility(View.GONE);
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.VISIBLE);
        });

        relationshipRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            otherRelationLayout.setVisibility(checkedId == R.id.radioOtherRelation ? View.VISIBLE : View.GONE);
            if (checkedId != R.id.radioOtherRelation) otherRelationInput.setText("");
        });

        medicationUsageGroup.setOnCheckedChangeListener((group, checkedId) -> {
            medicationListLayout.setVisibility(checkedId == R.id.medicationYes ? View.VISIBLE : View.GONE);
            if (checkedId == R.id.medicationNo) medicationListInput.setText("");
        });

        allergyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            allergyInputLayout.setVisibility(checkedId == R.id.allergyYes ? View.VISIBLE : View.GONE);
            if (checkedId == R.id.allergyNo) allergyInput.setText("");
        });



        submit.setOnClickListener(v -> {
            if (!validateEmergencyContact()) return;
            saveProfile(heightPicker, weightPicker);
            Intent intent = new Intent(MedicalProfileActivity.this, MainPageActivity.class);
            startActivity(intent);
            finish(); // Optional: finish current activity if you don't want the user to return to it
        });
    }

    private void initViews() {
        birthdateEditText = findViewById(R.id.birthdateEditText);
        birthdateInputLayout = findViewById(R.id.birthdateInputLayout);

        emergencyNameInput = findViewById(R.id.emergencyNameInput);
        emergencyPhoneInput = findViewById(R.id.emergencyPhoneInput);
        otherRelationInput = findViewById(R.id.otherRelationInput);

        medicationListInput = findViewById(R.id.medicationListInput);
        allergyInput = findViewById(R.id.allergyInput);

        medicationListLayout = findViewById(R.id.medicationListLayout);
        allergyInputLayout = findViewById(R.id.allergyInputLayout);

        emergencyNameLayout = findViewById(R.id.emergencyNameLayout);
        emergencyPhoneLayout = findViewById(R.id.emergencyPhoneLayout);
        otherRelationLayout = findViewById(R.id.otherRelationLayout);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        medicationUsageGroup = findViewById(R.id.medicationUsageGroup);
        allergyGroup = findViewById(R.id.allergyGroup);
        relationshipRadioGroup = findViewById(R.id.relationshipRadioGroup);

        smokingGroup = findViewById(R.id.smokingGroup);
        alcoholGroup = findViewById(R.id.alcoholGroup);

        bloodTypeChipGroup = findViewById(R.id.bloodTypeChipGroup);
        rhFactorChipGroup = findViewById(R.id.rhFactorChipGroup);

        checkChestPain = findViewById(R.id.checkChestPain);
        checkBreathless = findViewById(R.id.checkBreathless);
        checkDizziness = findViewById(R.id.checkDizziness);
        checkFatigue = findViewById(R.id.checkFatigue);
        checkPalpitations = findViewById(R.id.checkPalpitations);
        checkNoSymptoms = findViewById(R.id.checkNoSymptoms);

        familyHeartDisease = findViewById(R.id.familyHeartDisease);
        familyDiabetes = findViewById(R.id.familyDiabetes);
        familyHypertension = findViewById(R.id.familyHypertension);
        familyStroke = findViewById(R.id.familyStroke);
      //  familyOther = findViewById(R.id.familyOther);
        checkNofamily = findViewById(R.id.checkNofamily);

        mentalDepression = findViewById(R.id.mentalDepression);
        mentalAnxiety = findViewById(R.id.mentalAnxiety);
        mentalPTSD = findViewById(R.id.mentalPTSD);
        //mentalOther = findViewById(R.id.mentalOther);
        checkNoMental = findViewById(R.id.checkNoMental);

        checkDiabetes = findViewById(R.id.checkDiabetes);
        checkHypertension = findViewById(R.id.checkHypertension);
        checkAsthma = findViewById(R.id.checkAsthma);
        checkAnemia = findViewById(R.id.checkAnemia);
        checkNoChronic = findViewById(R.id.checkNoChronic);

        nextToQuestionsButton = findViewById(R.id.nextToQuestionsButton);
        medicalProfileButton = findViewById(R.id.MedicalProfileButton);
        backToPhy = findViewById(R.id.backToPhy);
        backToMP = findViewById(R.id.backToMP);

        submit =  findViewById(R.id.submitProfileButton);
    }










    private boolean validateMedicalQuestions() {
        // Medication
        if (medicationUsageGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please answer the medication usage question.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Allergy
        if (allergyGroup.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please answer the allergy question.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Symptoms
        if (!isAnyChecked(checkChestPain, checkBreathless, checkDizziness, checkFatigue, checkPalpitations, checkNoSymptoms)) {
            Toast.makeText(this, "Please select at least one symptom or 'None'.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Family history
        if (!isAnyChecked(familyHeartDisease, familyDiabetes, familyHypertension, familyStroke,  checkNofamily)) {
            Toast.makeText(this, "Please select at least one family history item or 'None'.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Chronic diseases
        if (!isAnyChecked(checkDiabetes, checkHypertension, checkAsthma, checkAnemia, checkNoChronic)) {
            Toast.makeText(this, "Please select at least one chronic condition or 'None'.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Mental health
        if (!isAnyChecked(mentalDepression, mentalAnxiety, mentalPTSD, checkNoMental)) {
            Toast.makeText(this, "Please select at least one mental health condition or 'None'.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }



    private boolean isAnyChecked(CheckBox... boxes) {
        for (CheckBox box : boxes) {
            if (box.isChecked()) return true;
        }
        return false;
    }





    private void setupGenderSelection() {
        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedGender = checkedId == R.id.genderMale ? "Male" : "Female";
        });
    }

    private void setupChipGroups() {
        bloodTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedBloodType = (checkedId != View.NO_ID) ? ((Chip) findViewById(checkedId)).getText().toString() : "";
        });

        rhFactorChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedRhFactor = (checkedId != View.NO_ID) ? ((Chip) findViewById(checkedId)).getText().toString() : "";
        });
    }

    private void setupBirthdatePicker() {
        birthdateInputLayout.setEndIconOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> birthdateEditText.setText(
                            String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedMonth + 1, selectedDay, selectedYear)),
                    year, month, day);
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            datePickerDialog.show();
        });
    }

    private void setupNumberPickers(NumberPicker heightPicker, NumberPicker weightPicker) {
        heightPicker.setMinValue(60);
        heightPicker.setMaxValue(220);
        heightPicker.setValue(170);

        weightPicker.setMinValue(20);
        weightPicker.setMaxValue(200);
        weightPicker.setValue(60);

        heightPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        weightPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
    }

    private boolean validatePhysiologicalInfo(NumberPicker heightPicker, NumberPicker weightPicker) {
        if (birthdateEditText.getText().toString().isEmpty() ||
                selectedGender.isEmpty() || selectedBloodType.isEmpty() || selectedRhFactor.isEmpty()) {
            Toast.makeText(this, "Please complete all physiological fields.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateEmergencyContact() {
        boolean valid = true;
        String name = emergencyNameInput.getText().toString().trim();
        String phone = emergencyPhoneInput.getText().toString().trim();
        int selectedRelationId = relationshipRadioGroup.getCheckedRadioButtonId();

        if (name.isEmpty()) {
            emergencyNameLayout.setError("Enter contact name");
            valid = false;
        } else emergencyNameLayout.setError(null);

        if (phone.length() != 8 || !phone.matches("\\d+")) {
            emergencyPhoneLayout.setError("Enter valid 8-digit number");
            valid = false;
        } else emergencyPhoneLayout.setError(null);

        if (selectedRelationId == -1) {
            Toast.makeText(this, "Please select a relationship.", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (selectedRelationId == R.id.radioOtherRelation &&
                otherRelationInput.getText().toString().trim().length() < 2) {
            otherRelationLayout.setError("Please specify relation");
            valid = false;
        } else {
            otherRelationLayout.setError(null);
        }

        return valid;
    }


















    private void saveProfile(NumberPicker heightPicker, NumberPicker weightPicker) {
        final String LOG_TAG = "MedicalProfileDebug";
        Toast.makeText(this, "🟡 saveProfile() triggered", Toast.LENGTH_SHORT).show();
        Log.d(LOG_TAG, "saveProfile() method entered");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "❌ User not logged in", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "User is null. Aborting save.");
            return;
        }

        String uid = user.getUid();
        Log.d(LOG_TAG, "✅ Current UID: " + uid);
        Toast.makeText(this, "✅ UID OK", Toast.LENGTH_SHORT).show();

        // ✅ Step 1: Start with a simple test map
        Map<String, Object> profile = new HashMap<>();
        profile.put("birthdate", birthdateEditText.getText() != null ? birthdateEditText.getText().toString() : "Unknown");
        profile.put("gender", selectedGender.isEmpty() ? "Unspecified" : selectedGender);

        // ✅ Step 2: Add only if not empty or null
        if (!selectedBloodType.isEmpty()) profile.put("blood_type", selectedBloodType);
        if (!selectedRhFactor.isEmpty()) profile.put("rh_factor", selectedRhFactor);

        profile.put("height_cm", heightPicker.getValue());
        profile.put("weight_kg", weightPicker.getValue());

        // ✅ Optional lists with checks
        List<String> symptoms = new ArrayList<>();
        if (checkChestPain.isChecked()) symptoms.add("Chest pain or tightness");
        if (checkBreathless.isChecked()) symptoms.add("Shortness of breath");
        if (checkDizziness.isChecked()) symptoms.add("Dizziness or fainting");
        if (checkFatigue.isChecked()) symptoms.add("Unusual fatigue or weakness");
        if (checkPalpitations.isChecked()) symptoms.add("Irregular heartbeat or palpitations");
        if (checkNoSymptoms.isChecked()) symptoms.add("None");
        if (!symptoms.isEmpty()) profile.put("symptoms", symptoms);




        int medId = medicationUsageGroup.getCheckedRadioButtonId();
        if (medId == R.id.medicationYes) {
            String meds = medicationListInput.getText() != null ? medicationListInput.getText().toString().trim() : "";
            profile.put("medication_list", meds.isEmpty() ? "Not specified" : meds);
        } else if (medId == R.id.medicationNo) {
            profile.put("medication_list", "No");
        } else {
            profile.put("medication_list", "Unknown");
        }



        int allergyId = allergyGroup.getCheckedRadioButtonId();
        profile.put("has_allergies", allergyId == R.id.allergyYes);

        if (allergyId == R.id.allergyYes) {
            String allergy = allergyInput.getText() != null ? allergyInput.getText().toString().trim() : "";
            profile.put("allergy_detail", allergy.isEmpty() ? "Not specified" : allergy);
        } else if (allergyId == R.id.allergyNo) {
            profile.put("allergy_detail", "No");
        } else {
            profile.put("allergy_detail", "Unknown");
        }




        List<String> family = new ArrayList<>();
        if (familyHeartDisease.isChecked()) family.add("Heart disease");
        if (familyDiabetes.isChecked()) family.add("Diabetes");
        if (familyHypertension.isChecked()) family.add("Hypertension");
        if (familyStroke.isChecked()) family.add("Stroke");

        if (checkNofamily.isChecked()) family.clear();
        if (family.isEmpty()) family.add("None");
        profile.put("family_history", family);




        List<String> chronic = new ArrayList<>();
        if (checkAnemia.isChecked()) chronic.add("Anemia");
        if (checkAsthma.isChecked())chronic.add("Asthma");
        if (checkHypertension.isChecked()) chronic.add("Hypertension");
        if (checkDiabetes.isChecked()) chronic.add("Diabetes");

        if (checkNoChronic.isChecked()) chronic.clear();
        if (chronic.isEmpty()) chronic.add("None");
        profile.put("chronic", chronic);


        List<String> mental = new ArrayList<>();
        if (mentalDepression.isChecked()) mental.add("Depression");
        if (mentalAnxiety.isChecked()) mental.add("Anxiety");
        if (mentalPTSD.isChecked()) mental.add("PTSD");

        if (checkNoMental.isChecked()) mental.clear();
        if (mental.isEmpty()) mental.add("None");
        profile.put("mental_health", mental);









        int smokingId = smokingGroup.getCheckedRadioButtonId();
        int alcoholId = alcoholGroup.getCheckedRadioButtonId();

        String smokingStatus = smokingId == R.id.smokingYes ? "Yes" : (smokingId == R.id.smokingNo ? "No" : "Unknown");
        String alcoholStatus = alcoholId == R.id.alcoholYes ? "Yes" : (alcoholId == R.id.alcoholNo ? "No" : "Unknown");

        profile.put("smoking", smokingStatus);
        profile.put("alcohol", alcoholStatus);





        // ✅ Emergency contact
        String name = emergencyNameInput.getText() != null ? emergencyNameInput.getText().toString().trim() : "";
        String phone = emergencyPhoneInput.getText() != null ? emergencyPhoneInput.getText().toString().trim() : "";

        if (!name.isEmpty()) profile.put("emergency_name", name);
        if (!phone.isEmpty()) profile.put("emergency_phone", "+961 " + phone);

        int selectedRelId = relationshipRadioGroup.getCheckedRadioButtonId();
        if (selectedRelId != -1) {
            String relation = (selectedRelId == R.id.radioOtherRelation)
                    ? (otherRelationInput.getText() != null ? otherRelationInput.getText().toString().trim() : "Other")
                    : ((RadioButton) findViewById(selectedRelId)).getText().toString();
            profile.put("emergency_relation", relation);
        }

        // ✅ Final debug log
        Log.d(LOG_TAG, "📦 Final Profile Map:");
        for (Map.Entry<String, Object> entry : profile.entrySet()) {
            Log.d(LOG_TAG, "  ➤ " + entry.getKey() + ": " + entry.getValue());
        }

        // ✅ Try writing to Firestore
        db.collection("users").document(uid).collection("medical_profile").document("profile")
                .set(profile)
                .addOnSuccessListener(unused -> {
                    Log.d(LOG_TAG, "✅ Firestore write SUCCESS");
                    Toast.makeText(this, "✅ Medical profile saved!", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(LOG_TAG, "❌ Firestore write FAILED: " + e.getMessage());
                    Toast.makeText(this, "❌ Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

}