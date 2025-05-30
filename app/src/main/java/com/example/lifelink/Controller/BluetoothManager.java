package com.example.lifelink.Controller;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lifelink.Model.HealthData;
import com.example.lifelink.View.CallEmergencyActivity;
import com.example.lifelink.View.HealthTrackerActivity;

public class BluetoothManager {

    // Request code used when asking the user for Bluetooth permission
    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;

    // Static instance of the Bluetooth controller
    private static Bluetooth bluetooth;

    // Prevents initializing the Bluetooth connection more than once
    private static boolean isInitialized = false;

    // ✅ Call this in an Activity (like MainActivity or HealthTrackerActivity) to start the Bluetooth setup
    public static void init(Activity activity) {
        // Skip if already initialized
        if (isInitialized) return;

        // Check if Bluetooth connect permission is granted
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            // Request Bluetooth permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSION);

            // Inform user that permission has been requested
            Toast.makeText(activity, "Bluetooth permission requested", Toast.LENGTH_SHORT).show();
            return;
        }

        // If permission is already granted, establish the connection
        connect(activity);
    }

    // ✅ Call this in the activity's onRequestPermissionsResult method
    public static void handlePermissionResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            // If permission was granted, connect to Bluetooth
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connect(activity);
            } else {
                // Inform user that permission was denied
                Toast.makeText(activity, "Bluetooth permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Variables to store the latest received values from the ESP32
    private static String latestHR = null;
    private static String latestSPO2 = null;

    // Handles the connection and data listening
    private static void connect(Activity activity) {
        // Create Bluetooth object and pass in Activity context
        bluetooth = new Bluetooth(activity);

        // Set the listener for receiving HR/SPO2/SOS data
        bluetooth.setBluetoothDataListener(new Bluetooth.BluetoothDataListener() {

            @Override
            public void onDataReceived(String heartRate, String spo2) {
                // 💓 Handle heart rate update
                if (heartRate != null && !heartRate.isEmpty()) {
                    latestHR = heartRate;
                    try {
                        int hr = Integer.parseInt(latestHR);
                        // Run UI update (Toast) on the main thread
                        new Handler(activity.getMainLooper()).post(() ->
                                Toast.makeText(activity, "❤️ HR updated: " + hr, Toast.LENGTH_SHORT).show()
                        );
                    } catch (NumberFormatException e) {
                        // If HR is invalid (e.g., not a number), discard it
                        latestHR = null;
                    }
                }

                // 🩸 Handle SpO2 update
                if (spo2 != null && !spo2.isEmpty()) {
                    latestSPO2 = spo2;
                    try {
                        int sp = Integer.parseInt(latestSPO2);
                        // Show SpO2 Toast safely from background thread
                        new Handler(activity.getMainLooper()).post(() ->
                                Toast.makeText(activity, "🩸 SpO₂ updated: " + sp, Toast.LENGTH_SHORT).show()
                        );
                    } catch (NumberFormatException e) {
                        latestSPO2 = null;
                    }
                }

                // ✅ When both HR and SpO2 are valid and available
                if (latestHR != null && latestSPO2 != null) {
                    try {
                        int hr = Integer.parseInt(latestHR);
                        int sp = Integer.parseInt(latestSPO2);

                        // Store the data in a shared holder class
                        LiveHealthDataHolder.setHealthData(new HealthData(hr, sp));
                        HealthTrackerState.isHealthDataReady = true;

                        // Notify the UI (HealthTrackerActivity) to update buttons or graphs
                        new Handler(activity.getMainLooper()).post(() -> {
                            if (activity instanceof HealthTrackerActivity) {
                                ((HealthTrackerActivity) activity).trySetupClickListeners();
                            }
                        });

                        // Reset values after use
                        latestHR = null;
                        latestSPO2 = null;

                    } catch (Exception e) {
                        // Ignore any parsing or logic errors silently
                    }
                }
            }

            @Override
            public void onSosTriggered() {
                // 🚨 Launch emergency screen if SOS is detected from ESP32
                Intent intent = new Intent(activity.getApplicationContext(), CallEmergencyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        });

        // Start the Bluetooth connection to the ESP32
        bluetooth.connect();

        // Inform the user that connection is being attempted
        Toast.makeText(activity, "Trying to connect to ESP32...", Toast.LENGTH_SHORT).show();

        // Mark as initialized so it doesn't repeat
        isInitialized = true;
    }

    // Public method to allow other classes to access the Bluetooth object if needed
    public static Bluetooth getBluetooth() {
        return bluetooth;
    }
}
