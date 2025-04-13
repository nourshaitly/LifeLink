package com.example.lifelink.View;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.lifelink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.button.MaterialButton;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Random;

public class HealthTrackerActivity extends AppCompatActivity {
    private LineChart spo2Chart, heartRateChart;
    private TextView spo2Value, heartRateValue;
    private MaterialButton toggleButton;
    private Handler handler;
    private boolean isMeasuring = false;
    private Random random = new Random();
    private int timeCounter = 0;
    private ArrayList<Entry> spo2Entries = new ArrayList<>();
    private ArrayList<Entry> heartRateEntries = new ArrayList<>();
    private static final int MEASUREMENT_DURATION = 30000;
    private static final int UPDATE_INTERVAL = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_tracker);

        // Initialize views
        initializeViews();
        
        // Setup charts in a separate thread to avoid UI blocking
        new Handler(Looper.getMainLooper()).post(() -> {
            setupCharts();
            setupButtonListener();
        });
    }

    private void initializeViews() {
        spo2Chart = findViewById(R.id.spo2Chart);
        heartRateChart = findViewById(R.id.heartRateChart);
        spo2Value = findViewById(R.id.spo2Value);
        heartRateValue = findViewById(R.id.heartRateValue);
        toggleButton = findViewById(R.id.toggleButton);
        handler = new Handler(Looper.getMainLooper());
    }

    private void setupCharts() {
        // Basic chart setup
        setupChart(spo2Chart, "SPO2", "%");
        setupChart(heartRateChart, "Heart Rate", "BPM");
        
        // Initial data points
        for (int i = 0; i < 5; i++) {
            spo2Entries.add(new Entry(i, 95 + random.nextFloat() * 5));
            heartRateEntries.add(new Entry(i, 60 + random.nextFloat() * 40));
        }
        updateChart(spo2Chart, spo2Entries);
        updateChart(heartRateChart, heartRateEntries);
        timeCounter = 5;
    }

    private void setupChart(LineChart chart, String label, String unit) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);
        chart.setBackgroundColor(getResources().getColor(R.color.white));

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(getResources().getColor(R.color.purpleLight));

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(1f);
        leftAxis.setTextColor(getResources().getColor(R.color.purpleLight));
        chart.getAxisRight().setEnabled(false);

        LineDataSet dataSet = new LineDataSet(new ArrayList<>(), label);
        dataSet.setColor(getResources().getColor(R.color.purpleLight));
        dataSet.setCircleColor(getResources().getColor(R.color.purpleLight));
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setDrawCircles(true);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(getResources().getColor(R.color.purpleLight));
        dataSet.setFillAlpha(50);

        chart.setData(new LineData(dataSet));
        chart.invalidate();
    }

    private void setupButtonListener() {
        toggleButton.setOnClickListener(v -> {
            if (!isMeasuring) {
                startMeasurement();
            } else {
                stopMeasurement();
            }
        });
    }

    private void startMeasurement() {
        isMeasuring = true;
        toggleButton.setText("Stop Measurement");
        toggleButton.setEnabled(false);
        Toast.makeText(this, "Starting measurement...", Toast.LENGTH_SHORT).show();
        
        // Clear previous data
        spo2Entries.clear();
        heartRateEntries.clear();
        timeCounter = 0;
        
        // Start measurement process
        handler.postDelayed(() -> {
            toggleButton.setEnabled(true);
            handler.post(updateRunnable);
        }, 2000); // Wait 2 seconds before starting updates
    }

    private void stopMeasurement() {
        isMeasuring = false;
        toggleButton.setText("Start Measurement");
        handler.removeCallbacks(updateRunnable);
        Toast.makeText(this, "Measurement stopped", Toast.LENGTH_SHORT).show();
    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMeasuring) {
                updateData();
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        }
    };

    private void updateData() {
        // Simulate real measurement data
        float spo2 = 95 + random.nextFloat() * 5; // Normal range: 95-100%
        float heartRate = 60 + random.nextFloat() * 40; // Normal range: 60-100 BPM

        // Add new entries
        spo2Entries.add(new Entry(timeCounter, spo2));
        heartRateEntries.add(new Entry(timeCounter, heartRate));

        // Update text views
        spo2Value.setText(String.format("%.0f%%", spo2));
        heartRateValue.setText(String.format("%.0f BPM", heartRate));

        // Update charts
        updateChart(spo2Chart, spo2Entries);
        updateChart(heartRateChart, heartRateEntries);

        timeCounter++;

        // Stop measurement after duration
        if (timeCounter * UPDATE_INTERVAL >= MEASUREMENT_DURATION) {
            stopMeasurement();
            Toast.makeText(this, "Measurement complete", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateChart(LineChart chart, ArrayList<Entry> entries) {
        LineData data = chart.getData();
        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            set.setValues(entries);
            data.notifyDataChanged();
            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(10);
            chart.moveViewToX(entries.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
} 