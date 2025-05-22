package com.example.lifelink.Controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Bluetooth {

    private static final String TAG = "Bluetooth";
    private static final String DEVICE_ADDRESS = "D0:EF:76:34:54:36"; // Your ESP32 MAC
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private boolean isReading = false;

    private String lastHeartRate = null;
    private String lastSpO2 = null;

    public interface BluetoothDataListener {
        void onDataReceived(String heartRate, String spo2);
        void onSosTriggered();
    }

    private BluetoothDataListener dataListener;

    public void setBluetoothDataListener(BluetoothDataListener listener) {
        this.dataListener = listener;
    }

    public Bluetooth(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        log("Bluetooth adapter initialized.");
    }

    public void connect() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            toast("Bluetooth connect permission not granted");
            log("Permission BLUETOOTH_CONNECT not granted.");
            return;
        }

        if (bluetoothAdapter == null) {
            toast("Bluetooth not supported on this device.");
            log("Device does not support Bluetooth.");
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
            log("Attempting to connect to device: " + DEVICE_ADDRESS);

            bluetoothSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            bluetoothSocket.connect();

            inputStream = bluetoothSocket.getInputStream();

            toast("Bluetooth connected!");
            log("Bluetooth socket connected successfully. Input stream opened.");
            startReading();

        } catch (IOException e) {
            toast("Bluetooth connection failed");
            log("Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void disconnect() {
        isReading = false;
        try {
            if (inputStream != null) {
                inputStream.close();
                log("Input stream closed.");
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                log("Bluetooth socket closed.");
            }
        } catch (IOException e) {
            log("Disconnection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void startReading() {
        isReading = true;
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (isReading) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes == -1) break;

                    String readMessage = new String(buffer, 0, bytes).trim();
                    String[] lines = readMessage.split("\\r?\\n");

                    for (String line : lines) {
                        line = line.trim();
                        if (line.equals("SOS triggered")) {
                            toast("🚨 SOS Trigger received");
                            if (dataListener != null) {
                                toast("datalistner");
                                dataListener.onSosTriggered();  // ⬅️ SOS is passed to app
                            }
                            continue;
                        }

                        if (line.startsWith("HR:")) {
                            lastHeartRate = line.substring(3).trim();
                            toast("✅ HR: " + lastHeartRate);
                        }  if (line.startsWith("SPO2:")) {
                            lastSpO2 = line.substring(5).trim();
                            toast("✅ SpO2: " + lastSpO2);
                        }

                        if (lastHeartRate != null || lastSpO2 != null) {
                            if (dataListener != null) {
                                dataListener.onDataReceived(lastHeartRate, lastSpO2);
                                toast("📤 Sent ➜ HR: " + lastHeartRate + " | SpO₂: " + lastSpO2);
                            } else {
                                toast("⚠️ No data listener.");
                            }
                            lastHeartRate = null;
                            lastSpO2 = null;
                        }
                    }

                } catch (IOException e) {
                    toast("❌ Read error: " + e.getMessage());
                    break;
                }
            }
        }).start();
    }


    // 🔹 Utility: Show Toast from any thread
    private void toast(String message) {
        new Handler(context.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    // 🔹 Utility: Log to Logcat and also Toast for debugging
    private void log(String message) {
        Log.d(TAG, message);
        // Uncomment below line if you want toast for every log (optional)
        // toast("[LOG] " + message);
    }
}
