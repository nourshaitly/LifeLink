package com.example.lifelink.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LiveHealthDataUtils {

    private static int heartRate = -1;
    private static int spo2 = -1;
    private static List<String> chronicDiseases = null;
    private static String userLocation = "Location not available";

    private static String userFullName = "Unknown";
    private static String emergencyContactPhone = "N/A";
    private static String emergencyContactRelation = "N/A";

    private LiveHealthDataUtils() {
        // Prevent instantiation
    }

    public static void updateData(int newHeartRate, int newSpo2) {
        heartRate = newHeartRate;
        spo2 = newSpo2;
    }

    public static void setChronicDiseases(List<String> diseases) {
        chronicDiseases = diseases;
    }

    public static void setUserLocation(double lat, double lng) {
        userLocation = "https://www.google.com/maps/search/?api=1&query=" +
                String.format(Locale.US, "%.6f,%.6f", lat, lng);
    }

    public static void setUserInfo(String fullName) {
        userFullName = fullName;
    }

    public static void setEmergencyContact(String phone, String relation) {
        emergencyContactPhone = phone;
        emergencyContactRelation = relation;
    }

    public static int getHeartRate() {
        return heartRate;
    }

    public static int getSpo2() {
        return spo2;
    }

    public static List<String> getChronicDiseases() {
        return chronicDiseases;
    }

    public static String getUserLocation() {
        return userLocation;
    }
   public static String getUserFullName(){
        return userFullName;
   }

    public static boolean isDataValid() {
        return heartRate > 0 && spo2 > 0;
    }

    public static String getConditionLevel() {
        if (heartRate > 150 || spo2 < 88) {
            return "High-level emergency";
        } else if (heartRate > 120 || spo2 < 92) {
            return "Medium-level emergency";
        } else {
            return "Low-level emergency";
        }
    }

    public static String getEmergencyMessage() {
        StringBuilder message = new StringBuilder();

        message.append("🚨 Emergency Alert from LifeLink 🚨\n\n");

        message.append("User: ").append(userFullName).append("\n");
        message.append("Emergency Contact: ").append(emergencyContactPhone)
                .append(" (").append(emergencyContactRelation).append(")\n");
        message.append("Location: ").append(userLocation).append("\n\n");

        message.append("Status: ").append(getConditionLevel()).append("\n");
        message.append("HR: ").append(heartRate).append(" BPM | ");
        message.append("SpO₂: ").append(spo2).append("%\n");

        if (chronicDiseases != null && !chronicDiseases.isEmpty()) {
            message.append("Conditions: ").append(String.join(", ", chronicDiseases)).append("\n\n");
        } else {
            message.append("Conditions: None\n\n");
        }

        String timestamp = new SimpleDateFormat("dd MMM yyyy | hh:mm a", Locale.getDefault()).format(new Date());
        message.append("🕒 Time: ").append(timestamp).append("\n\n");

        message.append("Sent automatically via LifeLink emergency system.\n");
        message.append("Please respond immediately.");

        return message.toString();
    }

}
