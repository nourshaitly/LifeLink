package com.example.lifelink.View;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;


public class MedicalProfileActivity extends AppCompatActivity {

    private TextInputEditText birthdateEditText, conditionDetailInput, medicationInput, deviceInput, otherDiseaseInput, emergencyNameInput, emergencyPhoneInput, otherRelationInput;
    private TextInputLayout birthdateInputLayout, conditionDetailLayout, medicationInputLayout, deviceInputLayout, otherDiseaseLayout, emergencyNameLayout, emergencyPhoneLayout, otherRelationLayout;
    private RadioGroup conditionGroup, medicationsGroup, deviceGroup;
    RadioButton radioFamily, radioFriend, radioOtherRelation;
    private CheckBox checkOther;
    private Button nextToQuestionsButton,MedicalProfileButton, finishProfileButton, backToPhy, backToMP, submitProfileButton;

    private RadioGroup genderRadioGroup,  relationshipRadioGroup;
    private String selectedGender = "";
    private ChipGroup bloodTypeChipGroup, rhFactorChipGroup;
    private String selectedBloodType = "";
    private String selectedRhFactor = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_profile);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        // Initialize views
        birthdateEditText = findViewById(R.id.birthdateEditText);
        birthdateInputLayout = findViewById(R.id.birthdateInputLayout);
        conditionDetailInput = findViewById(R.id.conditionDetailInput);
        conditionDetailLayout = findViewById(R.id.conditionDetailLayout);
        medicationInput = findViewById(R.id.medicationInput);
        medicationInputLayout = findViewById(R.id.medicationInputLayout);
        deviceInput = findViewById(R.id.deviceInput);
        deviceInputLayout = findViewById(R.id.deviceInputLayout);
        otherDiseaseInput = findViewById(R.id.otherDiseaseInput);
        otherDiseaseLayout = findViewById(R.id.otherDiseaseLayout);

        conditionGroup = findViewById(R.id.conditionGroup);
        medicationsGroup = findViewById(R.id.medicationsGroup);
        deviceGroup = findViewById(R.id.deviceGroup);

        checkOther = findViewById(R.id.checkOther);

        nextToQuestionsButton = findViewById(R.id.nextToQuestionsButton);
        MedicalProfileButton = findViewById(R.id.MedicalProfileButton);
        backToPhy =findViewById(R.id.backToPhy);
        backToMP =findViewById(R.id.backToMP);

        genderRadioGroup = findViewById(R.id.genderRadioGroup);
        bloodTypeChipGroup = findViewById(R.id.bloodTypeChipGroup);
        rhFactorChipGroup = findViewById(R.id.rhFactorChipGroup);

        // Gender selection
        genderRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.genderMale) {
                selectedGender = "Male";
            } else if (checkedId == R.id.genderFemale) {
                selectedGender = "Female";
            }
        });


        // Blood type chip selection

       bloodTypeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip selectedChip = findViewById(checkedId);
                selectedBloodType = selectedChip.getText().toString();
            }
        });


        // Rh factor chip selection
        rhFactorChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != View.NO_ID) {
                Chip selectedChip = findViewById(checkedId);
                selectedRhFactor = selectedChip.getText().toString(); // Rh+ or Rh-
            }
        });

        // Show date picker when calendar icon is clicked
        birthdateInputLayout.setEndIconOnClickListener(v -> showDatePickerDialog());

        // Show/hide condition input
        conditionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.conditionYes) {
                conditionDetailLayout.setVisibility(View.VISIBLE);
            } else {
                conditionDetailLayout.setVisibility(View.GONE);
                conditionDetailInput.setText("");
            }
        });

        // Show/hide medication input
        medicationsGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.medsYes) {
                medicationInputLayout.setVisibility(View.VISIBLE);
            } else {
                medicationInputLayout.setVisibility(View.GONE);
                medicationInput.setText("");
            }
        });

        // Show/hide device input
        deviceGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.deviceYes) {
                deviceInputLayout.setVisibility(View.VISIBLE);
            } else {
                deviceInputLayout.setVisibility(View.GONE);
                deviceInput.setText("");
            }
        });

        // Show/hide custom disease input
        checkOther.setOnCheckedChangeListener((buttonView, isChecked) -> {
            otherDiseaseLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) otherDiseaseInput.setText("");
        });

        birthdateEditText.addTextChangedListener(new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "MMDDYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override

            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d]", "");
                    String cleanC = current.replaceAll("[^\\d]", "");

                    int sel = clean.length();
                    int cl = clean.length();

                    // Fix deletion handling
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }

                    if (clean.equals(cleanC)) sel--;

                    if (cl < 8) {
                        clean += "MMDDYYYY".substring(cl);
                    } else {
                        int mon = Integer.parseInt(clean.substring(0, 2));
                        int day = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        mon = Math.min(mon, 12);
                        year = Math.max(1900, Math.min(year, Calendar.getInstance().get(Calendar.YEAR)));

                        cal.set(Calendar.MONTH, mon - 1);
                        cal.set(Calendar.YEAR, year);
                        day = Math.min(day, cal.getActualMaximum(Calendar.DAY_OF_MONTH));

                        clean = String.format("%02d%02d%04d", mon, day, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;

                    birthdateEditText.removeTextChangedListener(this);
                    birthdateEditText.setText(clean);
                    birthdateEditText.setSelection(Math.min(sel, clean.length()));
                    birthdateEditText.addTextChangedListener(this);
                }
            }

        });

        NumberPicker heightPicker = findViewById(R.id.heightPicker);
        NumberPicker weightPicker = findViewById(R.id.weightPicker);
        int heightValue = heightPicker.getValue();
        int weightValue = weightPicker.getValue();


        // Height (in cm)
        heightPicker.setMinValue(60);
        heightPicker.setMaxValue(220);
        heightPicker.setValue(150); // Default

        // Weight (in kg)
        weightPicker.setMinValue(20);
        weightPicker.setMaxValue(200);
        weightPicker.setValue(50); // Default

        // Optional: Hide the separator lines
        heightPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        weightPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

       /* nextToQuestionsButton.setOnClickListener(v -> {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // Not logged in
                return;
            }

            String uid = currentUser.getUid();

            // Get height & weight values

            int heightValue = heightPicker.getValue();
            int weightValue = weightPicker.getValue();

            // Prepare full blood type
            String fullBloodType = selectedBloodType + (selectedRhFactor.equals("Rh+") ? "+" : "-");

            Map<String, Object> data = new HashMap<>();
            data.put("birthdate", birthdateEditText.getText().toString());
            data.put("gender", selectedGender);
            data.put("blood_type", selectedBloodType);
            data.put("rh_factor", selectedRhFactor);
            data.put("full_blood_type", fullBloodType);
            data.put("height_cm", heightValue);
            data.put("weight_kg", weightValue);

            // Save to Firestore: users/{uid}/general_info
            db.collection("general_info").document(uid)
                    .set(data)
                    .addOnSuccessListener(aVoid -> {
                        // ✅ Move to Step 2
                        findViewById(R.id.stepPhysiological).setVisibility(View.GONE);
                        findViewById(R.id.stepMedicalQuestions).setVisibility(View.VISIBLE);

                        Log.d("Firestore", "Physiological info saved successfully.");
                        Toast.makeText(this, "Step 1 saved!", Toast.LENGTH_SHORT).show();

                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error saving step 1", e);
                        Toast.makeText(this, "Failed to save data!", Toast.LENGTH_SHORT).show();

                    });
        });*/

        nextToQuestionsButton.setOnClickListener(v -> {

            if (birthdateEditText.getText().toString().isEmpty()
                    || selectedGender.isEmpty()
                    || selectedBloodType.isEmpty()
                    || selectedRhFactor.isEmpty()
            || heightValue==20 || weightValue==60) {
                Toast.makeText(this, "Please complete all fields before continuing.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Hide Step 1
            findViewById(R.id.stepPhysiological).setVisibility(View.GONE);

            // Show Step 2
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.VISIBLE);
        });
        backToPhy.setOnClickListener(v -> {
            // Show Step 1
            findViewById(R.id.stepPhysiological).setVisibility(View.VISIBLE);

            // Hide Step 2
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.GONE);
        });


        MedicalProfileButton.setOnClickListener(v -> {
            boolean isValid = true;

            // --- Validate 1: Conditions ---
            int conditionSelection = conditionGroup.getCheckedRadioButtonId();
            if (conditionSelection == -1) {
                Toast.makeText(this, "Please answer the heart/respiratory condition question.", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            boolean hasCondition = conditionSelection == R.id.conditionYes;
            String conditionDetail = conditionDetailInput.getText().toString().trim();
            if (hasCondition && (conditionDetail.length() < 3 || !conditionDetail.matches(".*[a-zA-Z].*"))) {
                conditionDetailLayout.setError("Please describe your condition properly");
                isValid = false;
            } else {
                conditionDetailLayout.setError(null);
            }

            // --- Validate 2: Medications ---
            int medicationSelection = medicationsGroup.getCheckedRadioButtonId();
            if (medicationSelection == -1) {
                Toast.makeText(this, "Please answer the medication question.", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            boolean takesMedication = medicationSelection == R.id.medsYes;
            String medicationDetail = medicationInput.getText().toString().trim();
            if (takesMedication && (medicationDetail.length() < 3 || !medicationDetail.matches(".*[a-zA-Z].*"))) {
                medicationInputLayout.setError("Please list your medication");
                isValid = false;
            } else {
                medicationInputLayout.setError(null);
            }

            // --- Validate 3: Devices ---
            int deviceSelection = deviceGroup.getCheckedRadioButtonId();
            if (deviceSelection == -1) {
                Toast.makeText(this, "Please answer the device question.", Toast.LENGTH_SHORT).show();
                isValid = false;
            }
            boolean hasDevice = deviceSelection == R.id.deviceYes;
            String deviceDetail = deviceInput.getText().toString().trim();
            if (hasDevice && (deviceDetail.length() < 3 || !deviceDetail.matches(".*[a-zA-Z].*"))) {
                deviceInputLayout.setError("Please describe the device");
                isValid = false;
            } else {
                deviceInputLayout.setError(null);
            }

            // --- Validate 4: Other chronic disease input if checked ---
            boolean hasOtherDisease = checkOther.isChecked();
            String otherDiseaseDetail = otherDiseaseInput.getText().toString().trim();
            if (hasOtherDisease && (otherDiseaseDetail.length() < 3 || !otherDiseaseDetail.matches(".*[a-zA-Z].*"))) {
                otherDiseaseLayout.setError("Please specify the disease");
                isValid = false;
            } else {
                otherDiseaseLayout.setError(null);
            }

            // ✅ All checks passed
            if (isValid) {
                findViewById(R.id.stepMedicalQuestions).setVisibility(View.GONE);
                findViewById(R.id.stepEmergencyContact).setVisibility(View.VISIBLE);
            }
        });

        // Step 3: Emergency Contact Fields
       emergencyNameInput = findViewById(R.id.emergencyNameInput);
       emergencyPhoneInput = findViewById(R.id.emergencyPhoneInput);
       otherRelationInput = findViewById(R.id.otherRelationInput);

       emergencyNameLayout = findViewById(R.id.emergencyNameLayout);
       emergencyPhoneLayout = findViewById(R.id.emergencyPhoneLayout);
       otherRelationLayout = findViewById(R.id.otherRelationLayout);

       relationshipRadioGroup = findViewById(R.id.relationshipRadioGroup);
       radioOtherRelation = findViewById(R.id.radioOtherRelation);

       submitProfileButton = findViewById(R.id.submitProfileButton);

       // Show/hide custom relation input
        relationshipRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioOtherRelation) {
                otherRelationLayout.setVisibility(View.VISIBLE);
            } else {
                otherRelationLayout.setVisibility(View.GONE);
                otherRelationInput.setText("");
            }
        });

        // Back button
        backToMP.setOnClickListener(v -> {
            findViewById(R.id.stepEmergencyContact).setVisibility(View.GONE);
            findViewById(R.id.stepMedicalQuestions).setVisibility(View.VISIBLE);
        });

        // Submit profile button
        submitProfileButton.setOnClickListener(v -> {
            boolean isValid = true;

            String name = emergencyNameInput.getText().toString().trim();
            String phone = emergencyPhoneInput.getText().toString().trim();
            int selectedRelationId = relationshipRadioGroup.getCheckedRadioButtonId();

            String relation = "";
            if (selectedRelationId == -1) {
                Toast.makeText(this, "Please select a relationship.", Toast.LENGTH_SHORT).show();
                isValid = false;
            } else if (selectedRelationId == R.id.radioOtherRelation) {
                relation = otherRelationInput.getText().toString().trim();
                if (relation.length() < 2 || !relation.matches(".*[a-zA-Z].*")) {
                    otherRelationLayout.setError("Please specify relation");
                    isValid = false;
                } else {
                    otherRelationLayout.setError(null);
                }
            } else {
                relation = ((RadioButton) findViewById(selectedRelationId)).getText().toString();
            }

            if (name.isEmpty()) {
                emergencyNameLayout.setError("Enter contact name");
                isValid = false;
            } else {
                emergencyNameLayout.setError(null);
            }

            if (phone.length() != 8 || !phone.matches("\\d+")) {
                emergencyPhoneLayout.setError("Enter valid 8-digit number");
                isValid = false;
            } else {
                emergencyPhoneLayout.setError(null);
            }

            if (isValid) {
                // All data collected successfully
                String summary = "Name: " + name + "\nPhone: +961 " + phone + "\nRelation: " + relation;
                Toast.makeText(this, "Profile complete!\n" + summary, Toast.LENGTH_LONG).show();

                // TODO: Save to Firestore or move to Dashboard
            } else {
                Toast.makeText(this, "Please correct the highlighted fields.", Toast.LENGTH_SHORT).show();
            }
        });








    }




    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d",
                            selectedMonth + 1, selectedDay, selectedYear);
                    birthdateEditText.setText(formattedDate);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


}
