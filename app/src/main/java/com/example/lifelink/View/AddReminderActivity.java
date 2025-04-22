package com.example.lifelink.View;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lifelink.View.ReminderAlarmReceiver;
import com.example.lifelink.R;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.util.Calendar;

public class AddReminderActivity extends AppCompatActivity {

    private EditText medicineNameInput;
    private Button timePickerButton, saveReminderButton;
    private TextView selectedTimeText;

    private int selectedHour = -1;
    private int selectedMinute = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);
        Toast.makeText(this, "Button clicked", Toast.LENGTH_SHORT).show();

        // Initialize views
        medicineNameInput = findViewById(R.id.medicineNameInput);
        timePickerButton = findViewById(R.id.timePickerButton);
        saveReminderButton = findViewById(R.id.saveReminderButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        // Time picker open
        timePickerButton.setOnClickListener(v -> openTimePicker());

        // Save reminder
        saveReminderButton.setOnClickListener(v -> {
            String name = medicineNameInput.getText().toString().trim();

            if (name.isEmpty() || selectedHour == -1) {
                Toast.makeText(this, "Please enter medicine name and pick a time", Toast.LENGTH_SHORT).show();
                return;
            }

            scheduleAlarm(name, selectedHour, selectedMinute);

            String amPm = (selectedHour >= 12) ? "PM" : "AM";
            int displayHour = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String time = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);

            Toast.makeText(this, "Reminder set for " + name + " at " + time, Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void openTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(8)
                .setMinute(0)
                .setTitleText("Select Reminder Time")
                .build();

        picker.show(getSupportFragmentManager(), "timePicker");

        picker.addOnPositiveButtonClickListener(v -> {
            selectedHour = picker.getHour();
            selectedMinute = picker.getMinute();

            String amPm = (selectedHour >= 12) ? "PM" : "AM";
            int displayHour = (selectedHour == 0 || selectedHour == 12) ? 12 : selectedHour % 12;
            String formattedTime = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
            selectedTimeText.setText("Time: " + formattedTime);
        });
    }

    private void scheduleAlarm(String medicineName, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long triggerTime = calendar.getTimeInMillis();

        if (triggerTime < System.currentTimeMillis()) {
            triggerTime += AlarmManager.INTERVAL_DAY; // next day
        }

        Intent intent = new Intent(this, ReminderAlarmReceiver.class);
        intent.putExtra("medicineName", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }
}
