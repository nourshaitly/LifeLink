package com.example.lifelink.Controller;

import com.example.lifelink.Model.HealthData;

public class LiveHealthDataHolder {

    private static HealthData healthData;

    public static void setHealthData(HealthData data) {
        healthData = data;
    }

    public static HealthData getHealthData() {
        return healthData;
    }
}
