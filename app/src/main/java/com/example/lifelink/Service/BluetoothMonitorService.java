package com.example.lifelink.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.lifelink.Controller.Bluetooth;
import com.example.lifelink.Controller.LiveHealthDataHolder;
import com.example.lifelink.Model.HealthData;
import com.example.lifelink.R;
import com.example.lifelink.View.CallEmergencyActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * A foreground service that continuously monitors heart rate and SpO₂
 * using a Bluetooth device (e.g., ESP32 + MAX30102).
 *
 * - Keeps running in the background with a persistent notification
 * - Stores real-time data to Firestore
 * - Triggers emergency logic when SOS is detected
 */
public class BluetoothMonitorService extends Service {

    private Bluetooth bluetoothController; // Custom Bluetooth class to manage connection
    private FirebaseFirestore db; // Reference to Firestore

    @Override
    public void onCreate() {
        super.onCreate();

        // Create the required notification channel for Android 8.0+
        createNotificationChannel();

        // Start the service in the foreground with a persistent notification
        startForegroundNotification();

        // Initialize Firestore instance
        db = FirebaseFirestore.getInstance();

        // Delay Bluetooth setup slightly (to avoid early context issues)
        new Handler(Looper.getMainLooper()).postDelayed(this::setupBluetoothConnection, 1000);
    }

    /**
     * Creates a notification channel for foreground service.
     * Required by Android 8.0+ to show persistent service notifications.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "bluetooth_monitor_channel", // Channel ID
                    "Bluetooth Monitoring",      // User-visible name
                    NotificationManager.IMPORTANCE_LOW // No sound/vibration
            );
            channel.setDescription("Monitoring Heart Rate and SpO₂");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel); // Register the channel
            }
        }
    }

    /**
     * Starts the service as a foreground service using a persistent notification.
     */
    private void startForegroundNotification() {
        Notification notification = new NotificationCompat.Builder(this, "bluetooth_monitor_channel")
                .setContentTitle("LifeLink Monitoring") // Title of the notification
                .setContentText("Monitoring Heart Rate and SpO₂...") // Content
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Temporary icon
                .setPriority(NotificationCompat.PRIORITY_LOW) // No sound
                .build();

        startForeground(1, notification); // Notification ID = 1
    }

    /**
     * Initializes the Bluetooth controller and sets the data listener
     * to receive heart rate, SpO₂, and SOS signals.
     */
    private void setupBluetoothConnection() {
        bluetoothController = new Bluetooth(getApplicationContext());

        bluetoothController.setBluetoothDataListener(new Bluetooth.BluetoothDataListener() {

            @Override
            public void onDataReceived(String hr, String sp) {
                // Validate and parse incoming heart rate and SpO₂ values
                if (hr != null && sp != null && !hr.trim().isEmpty() && !sp.trim().isEmpty()) {
                    try {
                        float heartRate = Float.parseFloat(hr.trim());
                        float spo2 = Float.parseFloat(sp.trim());

                        // Create a HealthData object and hold it globally
                        HealthData healthData = new HealthData((int) heartRate, (int) spo2, Timestamp.now());
                        LiveHealthDataHolder.setHealthData(healthData);

                        // Save to Firestore for live monitoring
                        saveToFirestore(heartRate, spo2);
                    } catch (NumberFormatException ignored) {
                        // Ignore invalid data strings (e.g., "Invalid")
                    }
                }
            }

            @Override
            public void onSosTriggered() {
                // Show a toast for confirmation
                Toast.makeText(BluetoothMonitorService.this, "FINALLY", Toast.LENGTH_SHORT).show();

                // Start the emergency activity from a service context
                Intent intent = new Intent(getApplicationContext(), CallEmergencyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Required for starting activity from a service
                startActivity(intent);
            }
        });

        // Attempt to connect to the Bluetooth device
        bluetoothController.connect();
    }

    /**
     * Saves the heart rate and SpO₂ data to Firestore
     * under: users/{uid}/LiveHealthData/{autoDocId}
     */
    private void saveToFirestore(float heartRate, float spo2) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Create a map of data to save
            Map<String, Object> data = new HashMap<>();
            data.put("heartRate", heartRate);
            data.put("oxygenLevel", spo2);
            data.put("updatedTime", FieldValue.serverTimestamp());

            // Store data inside user's LiveHealthData subcollection
            db.collection("users")
                    .document(uid)
                    .collection("LiveHealthData")
                    .add(data); // Add a new document with auto-generated ID
        }
    }

    /**
     * Called when the service is started with startService().
     * START_STICKY ensures the service is restarted if killed by the system.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; // Ensures service restarts if killed
    }

    /**
     * Called when the service is stopped or killed.
     * We disconnect the Bluetooth to clean up resources.
     */
    @Override
    public void onDestroy() {
        if (bluetoothController != null) {
            bluetoothController.disconnect(); // Disconnect Bluetooth safely
        }
        super.onDestroy();
    }

    /**
     * This service is not meant to be bound, so return null.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
