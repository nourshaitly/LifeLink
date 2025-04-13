  /* package com.example.lifelink.Controller;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class Bluetooth extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private static final String TAG = "BluetoothApp";

    // Bluetooth constants
    private static final String DEVICE_ADDRESS = "D0:EF:76:34:54:36"; // Replace with ESP32 MAC Address
    private static final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Bluetooth components
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;

    // Data variables for heart rate and SpO2
    public String heartRate = "N/A"; // Default values
    public String spO2 = "N/A";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // Request permissions if required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestBluetoothPermissions();
        } else {
            connectToBluetoothDevice();
        }
    }

    // Request Bluetooth permissions for Android 12+ (API 31+)
    private void requestBluetoothPermissions() {
        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            connectToBluetoothDevice();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToBluetoothDevice();
            } else {
                Log.e(TAG, "Bluetooth permissions denied. Cannot connect to device.");
            }
        }
    }

    private void connectToBluetoothDevice() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled!");
            return;
        }

        try {
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(DEVICE_ADDRESS);

            // 🔹 Check permissions before attempting Bluetooth operations
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "Missing Bluetooth permissions.");
                requestBluetoothPermissions(); // Request permissions
                return;
            }

            // Create and connect to Bluetooth socket
            bluetoothSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
            bluetoothSocket.connect(); // Connect to the socket
            Log.d(TAG, "Connected to ESP32 successfully!");
            inputStream = bluetoothSocket.getInputStream();

            startReadingData(); // Begin data reading and processing

        } catch (IOException e) {
            Log.e(TAG, "Failed to connect: " + e.getMessage());
        }
    }

    private void startReadingData() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                StringBuilder messageBuffer = new StringBuilder();

                int bytes;
                while ((bytes = inputStream.read(buffer)) > 0) {
                    // Accumulate incoming data
                    messageBuffer.append(new String(buffer, 0, bytes).trim());

                    String accumulatedData = messageBuffer.toString();
                    if (accumulatedData.contains("Heart Rate") && accumulatedData.contains("SpO2")) {
                        Log.d(TAG, "Complete Data: " + accumulatedData);

                        // Parse the data
                        String[] lines = accumulatedData.split("\\r?\\n");

                        for (String line : lines) {
                            if (line.contains("Heart Rate")) {
                                heartRate = line.split(":")[1].replace("BPM", "").trim(); // Extract numerical Heart Rate
                            }
                            if (line.contains("SpO2")) {
                                spO2 = line.split(":")[1].replace("%", "").trim(); // Extract numerical SpO2
                            }
                        }

                        // Log data for verification
                        Log.d("test", "Heart Rate: " + heartRate + " BPM, SpO2: " + spO2 + " %");

                        // Send data to Firebase (Example)
                        sendDataToFirebase(heartRate, spO2);

                        // Clear the buffer after processing
                        messageBuffer.setLength(0);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading data: " + e.getMessage());
            }
        }).start();
    }

    private void sendDataToFirebase(String heartRate, String spO2) {
        // Code to send data to Firebase
        // Assuming you have Firebase setup in your project
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("LiveHealthData");

        // Using current timestamp for updating
        long timestamp = System.currentTimeMillis();

        ref.child("heartRate").setValue(heartRate);
        ref.child("oxygenLevel").setValue(spO2);
        ref.child("timestamp").setValue(timestamp);

        Log.d(TAG, "Data sent to Firebase - Heart Rate: " + heartRate + ", SpO2: " + spO2 + ", Timestamp: " + timestamp);
    }
}
*/