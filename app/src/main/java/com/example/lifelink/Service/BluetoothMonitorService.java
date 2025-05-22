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
import com.example.lifelink.View.EmergencyActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class BluetoothMonitorService extends Service {

    private Bluetooth bluetoothController;
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
        startForegroundNotification();

        db = FirebaseFirestore.getInstance();

        new Handler(Looper.getMainLooper()).postDelayed(this::setupBluetoothConnection, 1000);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "bluetooth_monitor_channel",
                    "Bluetooth Monitoring",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Monitoring Heart Rate and SpO₂");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void startForegroundNotification() {
        Notification notification = new NotificationCompat.Builder(this, "bluetooth_monitor_channel")
                .setContentTitle("LifeLink Monitoring")
                .setContentText("Monitoring Heart Rate and SpO₂...")
                .setSmallIcon(android.R.drawable.ic_dialog_info) // ✅ use system icon for test
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(1, notification);

    }

    private void setupBluetoothConnection() {
        bluetoothController = new Bluetooth(getApplicationContext());

        bluetoothController.setBluetoothDataListener(new Bluetooth.BluetoothDataListener() {
            @Override
            public void onDataReceived(String hr, String sp) {
                if (hr != null && sp != null && !hr.trim().isEmpty() && !sp.trim().isEmpty()) {
                    try {
                        float heartRate = Float.parseFloat(hr.trim());
                        float spo2 = Float.parseFloat(sp.trim());

                        HealthData healthData = new HealthData((int) heartRate, (int) spo2, Timestamp.now());
                        LiveHealthDataHolder.setHealthData(healthData);

                        saveToFirestore(heartRate, spo2);
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            @Override

            public void onSosTriggered() {


                Toast.makeText(BluetoothMonitorService.this, "FINALLY", Toast.LENGTH_SHORT).show();




                Intent intent = new Intent(getApplicationContext(), CallEmergencyActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }



        });

        bluetoothController.connect();
    }


    private void saveToFirestore(float heartRate, float spo2) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            Map<String, Object> data = new HashMap<>();
            data.put("heartRate", heartRate);
            data.put("oxygenLevel", spo2);
            data.put("updatedTime", FieldValue.serverTimestamp());

            db.collection("users")
                    .document(uid)
                    .collection("LiveHealthData")
                    .add(data);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (bluetoothController != null) {
            bluetoothController.disconnect();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}