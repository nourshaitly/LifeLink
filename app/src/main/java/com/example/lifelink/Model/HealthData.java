package com.example.lifelink.Model;

import com.google.firebase.Timestamp;

public class HealthData {
    private int heartRate;
    private int spo2;
    private Timestamp timestamp;

    // Default constructor (required by Firestore)
    public HealthData() {}

    // Full constructor
    public HealthData(int heartRate, int spo2, Timestamp timestamp) {
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.timestamp = timestamp;
    }

    // Convenience constructor for live usage
    public HealthData(int heartRate, int spo2) {
        this.heartRate = heartRate;
        this.spo2 = spo2;
        this.timestamp = Timestamp.now(); // Optional
    }

    // Getters and Setters
    public int getHeartRate() { return heartRate; }
    public void setHeartRate(int heartRate) { this.heartRate = heartRate; }

    public int getSpo2() { return spo2; }
    public void setSpo2(int spo2) { this.spo2 = spo2; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
