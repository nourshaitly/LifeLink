// =======================
// Bluetooth.java
// =======================

package com.example.lifelink.Controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Bluetooth {

    private static final String TAG = "Bluetooth";
    private static final String DEVICE_ADDRESS = "D0:EF:76:34:54:36"; // Replace with your ESP32 MAC address
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
    }

    private BluetoothDataListener dataListener;

    public void setBluetoothDataListener(BluetoothDataListener listener) {
        this.dataListener = listener;
    }

    public Bluetooth(Context context) {
        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connect() {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Bluetooth connect permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            bluetoothSocket.connect();
            inputStream = bluetoothSocket.getInputStream();
            startReading();
        } catch (IOException e) {
            Log.e(TAG, "Connection failed", e);
        }
    }

    public void disconnect() {
        isReading = false;
        try {
            if (inputStream != null) inputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "Disconnection failed", e);
        }
    }

    private void startReading() {
        isReading = true;
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder sb = new StringBuilder();

            while (isReading) {
                try {
                    bytes = inputStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    sb.append(readMessage);

                    int endIndex;
                    while ((endIndex = sb.indexOf("\n")) >= 0) {
                        String line = sb.substring(0, endIndex).trim();
                        sb.delete(0, endIndex + 1);

                        Log.d(TAG, "Received line: " + line);

                        if (line.contains("Heart Rate")) {
                            if (line.contains("Invalid")) {
                                lastHeartRate = "0";
                            } else {
                                try {
                                    lastHeartRate = line.split(":")[1].replace("BPM", "").trim();
                                } catch (Exception e) {
                                    lastHeartRate = "0";
                                }
                            }
                        }

                        if (line.contains("SpO2")) {
                            if (line.contains("Invalid")) {
                                lastSpO2 = "0";
                            } else {
                                try {
                                    lastSpO2 = line.split(":")[1].replace("%", "").trim();
                                } catch (Exception e) {
                                    lastSpO2 = "0";
                                }
                            }
                        }

                        if (lastHeartRate != null && lastSpO2 != null) {
                            if (dataListener != null) {
                                dataListener.onDataReceived(lastHeartRate, lastSpO2);
                            }
                            lastHeartRate = null;
                            lastSpO2 = null;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error reading from InputStream", e);
                    break;
                }
            }
        }).start();
    }
}