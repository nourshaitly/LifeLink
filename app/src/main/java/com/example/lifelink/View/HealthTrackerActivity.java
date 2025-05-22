package com.example.lifelink.View;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.Controller.BluetoothManager;
import com.example.lifelink.Controller.FirestoreService;
import com.example.lifelink.Controller.HealthTrackerState;
import com.example.lifelink.Controller.LiveHealthDataHolder;
import com.example.lifelink.Model.HealthData;
import com.example.lifelink.Model.MedicalProfile;
import com.example.lifelink.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HealthTrackerActivity extends AppCompatActivity {

    private CircularProgressIndicator wellnessScoreIndicator;
    private TextView wellnessScoreValue, lastMeasuredTime;
    private LineChart heartRateChart, spo2Chart;
    private MaterialCardView heartRateCardMaterial, spo2CardMaterial;
    private TextView heartRateValue, spo2Value, wellnessdescription;

    private final ArrayList<Entry> heartRateEntries = new ArrayList<>();
    private final ArrayList<Entry> spo2Entries = new ArrayList<>();
    private final ArrayList<Long> timeStamps = new ArrayList<>();

    private Handler handler;
    private Runnable updateTask;
    private MedicalProfile medicalProfile;

    private int heartRateLevel = -1;
    private int spo2Level = -1;
    private long lastSavedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_tracker);
        BluetoothManager.init(this);

        initializeViews();
        setupCharts();
        updateLastMeasuredTime();
        startMonitoringHealthData();
        fetchMedicalProfile();
        MaterialButton viewDailyReportButton = findViewById(R.id.viewDailyReportButton);
        viewDailyReportButton.setOnClickListener(v -> {
            String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Intent intent = new Intent(this, DailyHealthReportActivity.class);
            intent.putExtra("reportDate", todayDate);
            startActivity(intent);
        });
        findViewById(R.id.viewHistoryButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, DailyReportHistoryActivity.class);
            startActivity(intent);
        });

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
        wellnessdescription = findViewById(R.id.wellnessDescription);
        spo2Value = findViewById(R.id.spo2Value);


    }

    private void fetchMedicalProfile() {
        FirestoreService firestoreService = new FirestoreService();
        firestoreService.fetchMedicalProfile(new FirestoreService.OnProfileFetchedListener() {
            @Override
            public void onProfileFetched(MedicalProfile profile) {
                if (profile == null) {
                    Toast.makeText(HealthTrackerActivity.this, "❌ Profile is null", Toast.LENGTH_SHORT).show();
                    return;
                }
                medicalProfile = profile;
                HealthTrackerState.isProfileReady = true;
                Toast.makeText(HealthTrackerActivity.this, "✅ Profile loaded", Toast.LENGTH_SHORT).show();
                trySetupClickListeners();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HealthTrackerActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupCharts() {
        setupLineChart(heartRateChart);
        setupLineChart(spo2Chart);
    }

    private void setupLineChart(LineChart chart) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getLegend().setEnabled(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(true);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setLabelRotationAngle(-25f);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < timeStamps.size()) {
                    return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(timeStamps.get(index)));
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
    }

    private void updateCharts() {
        LineDataSet heartRateSet = new LineDataSet(heartRateEntries, "Heart Rate");
        heartRateSet.setColor(getResources().getColor(R.color.teal_200));
        heartRateSet.setDrawCircles(true);
        heartRateSet.setCircleRadius(3f);
        heartRateSet.setDrawValues(false);

        LineDataSet spo2Set = new LineDataSet(spo2Entries, "SpO2");
        spo2Set.setColor(getResources().getColor(R.color.purple_200));
        spo2Set.setDrawCircles(true);
        spo2Set.setCircleRadius(3f);
        spo2Set.setDrawValues(false);

        heartRateChart.setData(new LineData(heartRateSet));
        spo2Chart.setData(new LineData(spo2Set));

        heartRateChart.invalidate();
        spo2Chart.invalidate();
    }

    private void startMonitoringHealthData() {
        handler = new Handler();
        updateTask = new Runnable() {
            @Override
            public void run() {
                updateUIWithLatestHealthData();
                handler.postDelayed(this, 3000);
            }
        };
        handler.post(updateTask);
    }

    private void updateUIWithLatestHealthData() {
        HealthData healthData = LiveHealthDataHolder.getHealthData();
        if (healthData != null) {
            int heartRate = healthData.getHeartRate();
            int spo2 = healthData.getSpo2();
            long currentTime = System.currentTimeMillis();

            heartRateEntries.add(new Entry(timeStamps.size(), heartRate));
            spo2Entries.add(new Entry(timeStamps.size(), spo2));
            timeStamps.add(currentTime);

            if (heartRateEntries.size() > 20) {
                heartRateEntries.remove(0);
                spo2Entries.remove(0);
                timeStamps.remove(0);
            }

            heartRateValue.setText(String.format(Locale.getDefault(), "%d BPM", heartRate));
            spo2Value.setText(String.format(Locale.getDefault(), "%d %%", spo2));

            updateCharts();
            updateWellnessScore(heartRate, spo2);
            updateLastMeasuredTime();

            if (System.currentTimeMillis() - lastSavedTime >= 5 * 60 * 1000) {
                saveLiveHealthToFirestore(heartRate, spo2);
                lastSavedTime = System.currentTimeMillis();
            }
        } else {
            Toast.makeText(this, "Waiting for live health data...", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLiveHealthToFirestore(int heartRate, int spo2) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) return;

        String uid = auth.getCurrentUser().getUid();
        String date = new SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> data = new HashMap<>();
        data.put("heartRate", heartRate);
        data.put("spo2", spo2);
        data.put("timestamp", time);

        // ✅ Save the health entry to entries subcollection
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("daily_health_data")
                .document(date)
                .collection("entries")
                .add(data);

        // ✅ Ensure the parent date document is counted by Firestore
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("daily_health_data")
                .document(date)
                .set(Collections.singletonMap("exists", true), SetOptions.merge());
    }

    private void updateWellnessScore(float heartRate, float spo2) {
        int score = 0;
        if (heartRate >= 60 && heartRate <= 100) score += 50;
        if (spo2 >= 95 && spo2 <= 100) score += 50;

        wellnessScoreIndicator.setProgress(score);
        wellnessScoreValue.setText(String.valueOf(score));
        animateWellnessScore(score);
    }

    private int calculateOverallWellnessScore(int hrLevel, int spo2Level) {
        int hrScore = 100 - (hrLevel - 1) * 40;
        int spo2Score = 100 - (spo2Level - 1) * 40;
        int averageScore = (hrScore + spo2Score) / 2;

        if (averageScore >= 90) {
            wellnessdescription.setText("Your vital signs are excellent. Keep maintaining a healthy lifestyle.");
        } else if (averageScore >= 70) {
            wellnessdescription.setText("Your health is generally stable, but minor irregularities were detected.");
        } else if (averageScore >= 40) {
            wellnessdescription.setText("Some concerning signs were identified. Monitoring or lifestyle adjustment is recommended.");
        } else {
            wellnessdescription.setText("Serious health irregularities detected. Please seek medical attention immediately.");
        }

        return averageScore;
    }

    private void setupClickListeners() {
        HealthData healthData = LiveHealthDataHolder.getHealthData();
        if (healthData == null || medicalProfile == null) {
            Toast.makeText(this, "⚠️ Still waiting for health data or profile", Toast.LENGTH_SHORT).show();
            return;
        }

        int heartRate = healthData.getHeartRate();
        int spo2 = healthData.getSpo2();

        heartRateCardMaterial.setOnClickListener(v -> {
            HealthDetailDialog dialog = HealthDetailDialog.newInstance("heartRate", heartRate, spo2, medicalProfile);
            dialog.setOnEmergencyLevelEvaluatedListener((type, level) -> {
                heartRateLevel = level;
                checkAndUpdateWellnessScore();
            });
            dialog.show(getSupportFragmentManager(), "HeartDialog");
        });

        spo2CardMaterial.setOnClickListener(v -> {
            HealthDetailDialog dialog = HealthDetailDialog.newInstance("spo2", heartRate, spo2, medicalProfile);
            dialog.setOnEmergencyLevelEvaluatedListener((type, level) -> {
                spo2Level = level;
                checkAndUpdateWellnessScore();
            });
            dialog.show(getSupportFragmentManager(), "Spo2Dialog");
        });
    }

    private void checkAndUpdateWellnessScore() {
        if (heartRateLevel != -1 && spo2Level != -1) {
            int score = calculateOverallWellnessScore(heartRateLevel, spo2Level);
            wellnessScoreIndicator.setProgress(score);
            wellnessScoreValue.setText(String.valueOf(score));
        }
    }

    private void animateWellnessScore(int score) {
        int currentProgress = wellnessScoreIndicator.getProgress();
        ObjectAnimator animation = ObjectAnimator.ofInt(wellnessScoreIndicator, "progress", currentProgress, score);
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
        if (handler != null && updateTask != null) handler.removeCallbacks(updateTask);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        BluetoothManager.handlePermissionResult(this, requestCode, grantResults);
    }

    public void trySetupClickListeners() {
        if (HealthTrackerState.isProfileReady && HealthTrackerState.isHealthDataReady) {
            setupClickListeners();
        }
    }
}
