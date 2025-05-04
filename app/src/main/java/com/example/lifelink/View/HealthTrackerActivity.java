package com.example.lifelink.View;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.Controller.LiveHealthDataHolder;
import com.example.lifelink.Model.HealthData;
import com.example.lifelink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HealthTrackerActivity extends AppCompatActivity {

    private CircularProgressIndicator wellnessScoreIndicator;
    private TextView wellnessScoreValue, lastMeasuredTime;
    private LineChart heartRateChart, spo2Chart;
    private MaterialCardView heartRateCardMaterial, spo2CardMaterial;
    private TextView heartRateValue, spo2Value;

    private ArrayList<Entry> heartRateEntries = new ArrayList<>();
    private ArrayList<Entry> spo2Entries = new ArrayList<>();
    private int timeCounter = 0;

    private Handler handler;
    private Runnable updateTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_tracker);

        initializeViews();
        setupCharts();
        setupClickListeners();
        updateLastMeasuredTime();

        startMonitoringHealthData();
    }

    private void initializeViews() {
        wellnessScoreIndicator = findViewById(R.id.wellnessScoreIndicator);
        wellnessScoreValue = findViewById(R.id.wellnessScoreValue);
        lastMeasuredTime = findViewById(R.id.lastMeasuredTime);

        heartRateChart = findViewById(R.id.heartRateChart);
        spo2Chart = findViewById(R.id.spo2Chart);

        heartRateCardMaterial = findViewById(R.id.heartRateCardMaterial);
        spo2CardMaterial = findViewById(R.id.spo2CardMaterial);

        heartRateValue = findViewById(R.id.heartRateValue);
        spo2Value = findViewById(R.id.spo2Value);
    }

    private void setupClickListeners() {
        heartRateCardMaterial.setOnClickListener(v -> {
            HealthDetailDialog dialog = new HealthDetailDialog();
            dialog.setType("heartRate");
            dialog.show(getSupportFragmentManager(), "HeartRateDialog");
        });

        spo2CardMaterial.setOnClickListener(v -> {
            HealthDetailDialog dialog = new HealthDetailDialog();
            dialog.setType("spo2");
            dialog.show(getSupportFragmentManager(), "SPO2Dialog");
        });
    }

    private void setupCharts() {
        setupMiniChart(heartRateChart, heartRateEntries, "Heart Rate");
        setupMiniChart(spo2Chart, spo2Entries, "SPO2");
    }

    private void setupMiniChart(LineChart chart, ArrayList<Entry> entries, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(getResources().getColor(R.color.teal_200));
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawFilled(true);
        set.setFillColor(getResources().getColor(R.color.teal_200));
        set.setFillAlpha(50);

        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawLabels(false);

        chart.getAxisLeft().setEnabled(false);
        chart.getAxisRight().setEnabled(false);

        chart.setData(new LineData(set));
        chart.invalidate();
    }

    private void startMonitoringHealthData() {
        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                updateUIWithLatestHealthData();
                handler.postDelayed(this, 3000); // Refresh every 3 seconds
            }
        };
        handler.post(updateTask);
    }

    private void updateUIWithLatestHealthData() {
        HealthData healthData = LiveHealthDataHolder.getHealthData();
        if (healthData != null) {
            int heartRate = healthData.getHeartRate();
            int spo2 = healthData.getSpo2();

            heartRateEntries.add(new Entry(timeCounter, heartRate));
            spo2Entries.add(new Entry(timeCounter, spo2));

            if (heartRateEntries.size() > 10) {
                heartRateEntries.remove(0);
                spo2Entries.remove(0);
            }

            heartRateValue.setText(String.format(Locale.getDefault(), "%d BPM", heartRate));
            spo2Value.setText(String.format(Locale.getDefault(), "%d %%", spo2));

            setupMiniChart(heartRateChart, heartRateEntries, "Heart Rate");
            setupMiniChart(spo2Chart, spo2Entries, "SPO2");

            updateWellnessScore(heartRate, spo2);
            updateLastMeasuredTime();
            timeCounter++;
        } else {
            Toast.makeText(this, "Waiting for live health data...", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateWellnessScore(float heartRate, float spo2) {
        int score = 0;
        if (heartRate >= 60 && heartRate <= 100) score += 50;
        if (spo2 >= 95 && spo2 <= 100) score += 50;
        wellnessScoreIndicator.setProgress(score);
        wellnessScoreValue.setText(String.valueOf(score));
        animateWellnessScore(score);
    }

    private void animateWellnessScore(int score) {
        int currentProgress = wellnessScoreIndicator.getProgress();
        ObjectAnimator animation = ObjectAnimator.ofInt(
                wellnessScoreIndicator,
                "progress",
                currentProgress,
                score
        );
        animation.setDuration(1000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void updateLastMeasuredTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy | h:mm a", Locale.getDefault());
        lastMeasuredTime.setText(sdf.format(new Date()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && updateTask != null) {
            handler.removeCallbacks(updateTask);
        }
    }
}
