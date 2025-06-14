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

    private static final int REQUEST_BLUETOOTH_PERMISSION = 101;
    private static Bluetooth bluetooth;
    private static boolean isInitialized = false;

    // ✅ Call this once in your MainActivity or HealthTrackerActivity
    public static void init(Activity activity) {
        if (isInitialized) return;

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    REQUEST_BLUETOOTH_PERMISSION);

            Toast.makeText(activity, "Bluetooth permission requested", Toast.LENGTH_SHORT).show();
            return;
        }

        connect(activity);
    }

    // ✅ Call this inside onRequestPermissionsResult of the activity
    public static void handlePermissionResult(Activity activity, int requestCode, int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connect(activity);
            } else {
                Toast.makeText(activity, "Bluetooth permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private static String latestHR = null;
    private static String latestSPO2 = null;

    private static void connect(Activity activity) {
        bluetooth = new Bluetooth(activity);

        bluetooth.setBluetoothDataListener(new Bluetooth.BluetoothDataListener() {
            @Override
            public void onDataReceived(String heartRate, String spo2) {
                if (heartRate != null && !heartRate.isEmpty()) {
                    latestHR = heartRate;
                    try {
                        int hr = Integer.parseInt(latestHR);
                      /*  new Handler(activity.getMainLooper()).post(() ->
                              Toast.makeText(activity, "❤️ HR updated: " + hr, Toast.LENGTH_SHORT).show()
                        );*/
                    } catch (NumberFormatException e) {
                        latestHR = null;
                    }
                }

                if (spo2 != null && !spo2.isEmpty()) {
                    latestSPO2 = spo2;
                    try {
                        int sp = Integer.parseInt(latestSPO2);
                      /*  new Handler(activity.getMainLooper()).post(() ->
                               Toast.makeText(activity, "🩸 SpO₂ updated: " + sp, Toast.LENGTH_SHORT).show()
                        );*/
                    } catch (NumberFormatException e) {
                        latestSPO2 = null;
                    }
                }

                // ✅ When both are available, set LiveHealthData + ready flag
                if (latestHR != null && latestSPO2 != null) {
                    try {
                        int hr = Integer.parseInt(latestHR);
                        int sp = Integer.parseInt(latestSPO2);

                        LiveHealthDataHolder.setHealthData(new HealthData(hr, sp));
                        HealthTrackerState.isHealthDataReady = true;

                        new Handler(activity.getMainLooper()).post(() -> {
                            if (activity instanceof HealthTrackerActivity) {
                                ((HealthTrackerActivity) activity).trySetupClickListeners();
                            }
                        });

                        latestHR = null;
                        latestSPO2 = null;
                    } catch (Exception e) {
                        // Ignore parse errors
                    }
                }
            }

            @Override
            public void onSosTriggered() {
                Intent intent = new Intent(activity.getApplicationContext(), CallEmergencyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);

            }
        });

        bluetooth.connect();
        Toast.makeText(activity, "Trying to connect to ESP32...", Toast.LENGTH_SHORT).show();
        isInitialized = true;
    }




    public static Bluetooth getBluetooth() {
        return bluetooth;
    }
}