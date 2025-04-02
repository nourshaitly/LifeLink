package com.example.lifelink.Model;

import com.google.firebase.Timestamp;

public class HealthData {
    private int heartRate;
    private int spo2;

    private Timestamp timestamp;

    // Constructor
    public HealthData() {}

    public HealthData(int heartRate, int spo2, Timestamp timestamp) {
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getSpo2() { return spo2; }
    public void setSpo2(int spo2) { this.spo2 = spo2; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
