package com.example.lifelink.View;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.lifelink.Model.MedicalProfile;
import com.example.lifelink.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HealthDetailDialog extends DialogFragment {

    private static final String ARG_TYPE = "type";
    private static final String ARG_HEART_RATE = "heartRate";
    private static final String ARG_SPO2 = "spo2";
    private static final String ARG_PROFILE = "profile";

    private String type;
    private int heartRate;
    private int spo2;
    private MedicalProfile medicalProfile;

    private OnEmergencyLevelEvaluatedListener listener;

    // === Listener Interface ===
    public interface OnEmergencyLevelEvaluatedListener {
        void onEmergencyLevelEvaluated(String type, int level);
    }

    public void setOnEmergencyLevelEvaluatedListener(OnEmergencyLevelEvaluatedListener listener) {
        this.listener = listener;
    }

    public static HealthDetailDialog newInstance(String type, int heartRate, int spo2, MedicalProfile profile) {
        HealthDetailDialog dialog = new HealthDetailDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putInt(ARG_HEART_RATE, heartRate);
        args.putInt(ARG_SPO2, spo2);
        args.putSerializable(ARG_PROFILE, profile);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_TYPE);
            heartRate = getArguments().getInt(ARG_HEART_RATE);
            spo2 = getArguments().getInt(ARG_SPO2);
            medicalProfile = (MedicalProfile) getArguments().getSerializable(ARG_PROFILE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_card_rounded);
            dialog.getWindow().setGravity(Gravity.CENTER);
        }
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_health_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        TextView value = view.findViewById(R.id.detailValue);
        TextView unit = view.findViewById(R.id.detailUnit);
        TextView status = view.findViewById(R.id.detailStatus);
        TextView description = view.findViewById(R.id.detailDescription);
        CircularProgressIndicator progressIndicator = view.findViewById(R.id.detailProgressIndicator);
        ImageView closeBtn = view.findViewById(R.id.closeButton);
        TextView emergencyLevel = view.findViewById(R.id.emergencyLevel);

        closeBtn.setOnClickListener(v -> dismiss());

        int emergencyLevelScore = "heartRate".equals(type)
                ? evaluateHeartRateEmergency(medicalProfile, heartRate)
                : evaluateSpO2Emergency(medicalProfile, spo2);
       

        // Notify listener of the result
        if (listener != null) {
            listener.onEmergencyLevelEvaluated(type, emergencyLevelScore);
        }

        emergencyLevel.setText(getEmergencyLevelDescription(emergencyLevelScore));

        if ("heartRate".equals(type)) {
            value.setText(String.valueOf(heartRate));
            unit.setText("bpm");
            progressIndicator.setProgress(heartRate);

            String[] result = getHeartRateStatusAndAdvice(emergencyLevelScore);
            status.setText(result[0]);
            description.setText(result[1]);

        } else {
            value.setText(String.valueOf(spo2));
            unit.setText("%");
            progressIndicator.setProgress(spo2);

            String[] result = getSpO2StatusAndAdvice(emergencyLevelScore);
            status.setText(result[0]);
            description.setText(result[1]);
        }
    }

    // === HEART RATE Emergency Scoring ===
    private int evaluateHeartRateEmergency(MedicalProfile profile, int hr) {
        int score = 0;
        int age = profile.getAge();
        double bmi = calculateBMI(profile.getHeightCm(), profile.getWeightKg());

        if (age >= 65) score += 2;
        else if (age >= 50) score += 1;

        if (bmi >= 30) score += 2;
        else if (bmi >= 25) score += 1;

        if (profile.isSmoker()) score += 2;
        if (profile.isAlcoholic()) score += 1;

        if (profile.getChronicDiseases().contains("Hypertension") ||
                profile.getChronicDiseases().contains("Heart Disease")) score += 2;

        if (profile.getFamilyHistory().contains("Heart disease")) score += 1;

        if (hr > 150) score += 4;
        else if (hr > 130) score += 3;
        else if (hr > 110) score += 2;

        return score >= 9 ? 3 : score >= 5 ? 2 : 1;
    }

    // === SpO2 Emergency Scoring ===
    private int evaluateSpO2Emergency(MedicalProfile profile, int spo2) {
        int score = 0;
        int age = profile.getAge();
        double bmi = calculateBMI(profile.getHeightCm(), profile.getWeightKg());

        if (age >= 65) score += 2;
        else if (age >= 50) score += 1;

        if (bmi >= 30) score += 2;
        else if (bmi >= 25) score += 1;

        if (profile.isSmoker()) score += 2;
        if (profile.getChronicDiseases().contains("Asthma")) score += 2;
        if (profile.getSymptoms().contains("Shortness of breath")) score += 2;

        if (spo2 < 88) score += 4;
        else if (spo2 < 92) score += 3;
        else if (spo2 < 95) score += 2;

        return score >= 9 ? 3 : score >= 5 ? 2 : 1;
    }

    private String[] getHeartRateStatusAndAdvice(int level) {
        if (level == 3) {
            return new String[]{
                    "⚠️ Critical Heart Rate",
                    "Your heart rate and profile indicate a critical condition. Seek urgent care if symptoms are present."
            };
        } else if (level == 2) {
            return new String[]{
                    "⚠️ Elevated Heart Rate",
                    "Your heart rate is elevated. Rest, hydrate, and monitor symptoms. Seek care if persistent."
            };
        } else {
            return new String[]{
                    "✅ Heart Rate is Stable",
                    "Your heart rate is healthy. Maintain good lifestyle habits."
            };
        }
    }

    private String[] getSpO2StatusAndAdvice(int level) {
        if (level == 3) {
            return new String[]{
                    "🚨 Dangerously Low Oxygen",
                    "Your oxygen level is critically low. Seek emergency medical help immediately."
            };
        } else if (level == 2) {
            return new String[]{
                    "⚠️ Reduced Oxygen Level",
                    "Your oxygen level is below optimal. Rest, avoid smoke, and monitor breathing closely."
            };
        } else {
            return new String[]{
                    "✅ Oxygen Level is Healthy",
                    "Your SpO₂ is normal. Keep active and avoid polluted environments."
            };
        }
    }

    private String getEmergencyLevelDescription(int level) {
        switch (level) {
            case 3: return "🚨 High Emergency: Immediate action required";
            case 2: return "⚠️ Moderate Emergency: Monitor carefully";
            default: return "✅ Low Emergency: You're currently stable";
        }
    }

    private double calculateBMI(double heightCm, double weightKg) {
        double heightM = heightCm / 100.0;
        return weightKg / (heightM * heightM);
    }
}
