package com.example.lifelink.View;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.card.MaterialCardView;
//import android.graphics.ColorStateList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class HealthTrackerActivity extends AppCompatActivity {

    private static final String TAG = "HealthTracker";

    private CircularProgressIndicator wellnessScoreIndicator;
    private TextView wellnessScoreValue, lastMeasuredTime;
    private LineChart heartRateChart, spo2Chart;
    private MaterialCardView heartRateCardMaterial, spo2CardMaterial;
    private TextView heartRateValue, spo2Value;

    private Handler handler;
    private Random random = new Random();
    private ArrayList<Entry> heartRateEntries = new ArrayList<>();
    private ArrayList<Entry> spo2Entries = new ArrayList<>();
    private int timeCounter = 0;
    private boolean isDestroyed = false;
    private Runnable dataUpdateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_tracker);
        Log.d(TAG, "onCreate called");

        initializeViews();
        setupCharts();
        setupClickListeners();
        startDataSimulation();
        updateLastMeasuredTime();
        animateWellnessScore();
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

        handler = new Handler(Looper.getMainLooper());

        // Initialize profile button
        ImageButton profileButton = findViewById(R.id.profileButton);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> openProfile());
        }
    }

    private void setupCharts() {
        setupMiniChart(heartRateChart, "Heart Rate");
        setupMiniChart(spo2Chart, "SPO2");
    }

    private void setupMiniChart(LineChart chart, String label) {
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

        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        dataSet.setColor(getResources().getColor(R.color.teal_200));
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.teal_200));
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));
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



    private void startDataSimulation() {
        dataUpdateRunnable = new Runnable() {
            @Override
            public void run() {
                if (isDestroyed) return;
                updateData();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(dataUpdateRunnable);
    }

    private void updateData() {
        float heartRate = 60 + random.nextFloat() * 40;
        float spo2 = 95 + random.nextFloat() * 5;

        heartRateEntries.add(new Entry(timeCounter, heartRate));
        spo2Entries.add(new Entry(timeCounter, spo2));

        if (heartRateEntries.size() > 10) {
            heartRateEntries.remove(0);
            spo2Entries.remove(0);
        }

        heartRateValue.setText(String.format(Locale.getDefault(), "%.0f", heartRate));
        spo2Value.setText(String.format(Locale.getDefault(), "%.0f", spo2));

        updateChart(heartRateChart, heartRateEntries);
        updateChart(spo2Chart, spo2Entries);

        timeCounter++;
    }

    private void updateChart(LineChart chart, ArrayList<Entry> entries) {
        LineDataSet set = (LineDataSet) chart.getData().getDataSetByIndex(0);
        set.setValues(entries);
        chart.getData().notifyDataChanged();
        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private void updateLastMeasuredTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy | h:mm a", Locale.getDefault());
        lastMeasuredTime.setText(sdf.format(new Date()));
    }

    private void animateWellnessScore() {
        ObjectAnimator animation = ObjectAnimator.ofInt(wellnessScoreIndicator, "progress", 0, 71);
        animation.setDuration(1000);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isDestroyed = false;
        if (handler != null && dataUpdateRunnable != null) {
            handler.post(dataUpdateRunnable);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && dataUpdateRunnable != null) {
            handler.removeCallbacks(dataUpdateRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
