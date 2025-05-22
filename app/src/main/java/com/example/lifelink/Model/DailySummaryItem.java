package com.example.lifelink.Model;

public class DailySummaryItem {
    private String time;
    private int heartRate;
    private int spo2;

    public DailySummaryItem(String time, int heartRate, int spo2) {
        this.time = time;
        this.heartRate = heartRate;
        this.spo2 = spo2;
    }

    public String getTime() {
        return time;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getSpo2() {
        return spo2;
    }
}
