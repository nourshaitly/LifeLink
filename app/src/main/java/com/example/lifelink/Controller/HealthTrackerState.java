package com.example.lifelink.Controller;

public class HealthTrackerState {
    public static boolean isHealthDataReady = false;
    public static boolean isProfileReady = false;

    // Optional: Reset all flags at once
    public static void reset() {
        isHealthDataReady = false;
        isProfileReady = false;
    }
}
