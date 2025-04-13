// HealthDetailDialog.java
package com.example.lifelink.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.lifelink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.progressindicator.CircularProgressIndicator;

public class HealthDetailDialog extends DialogFragment {

    private String type;

    public HealthDetailDialog() {
        // Required empty public constructor
    }

    public void setType(String type) {
        this.type = type;
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
        LineChart chart = view.findViewById(R.id.detailChart);
        ImageView closeBtn = view.findViewById(R.id.closeButton);

        closeBtn.setOnClickListener(v -> dismiss());

        if ("heartRate".equals(type)) {
            value.setText("67");
            unit.setText("bpm");
            status.setText("Your Heart Rate is Normal");
            description.setText("Your heart rate indicates how many times your heart beats per minute. A normal resting heart rate for adults ranges from 60 to 100 bpm.");
            progressIndicator.setProgress(67);
        } else if ("spo2".equals(type)) {
            value.setText("98");
            unit.setText("%");
            status.setText("Your Oxygen Level is Excellent");
            description.setText("Blood oxygen level (SpO₂) is a measure of how much oxygen your blood is carrying. Normal SpO₂ is 95–100%.");
            progressIndicator.setProgress(98);
        }

        // TODO: Populate chart if needed
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
