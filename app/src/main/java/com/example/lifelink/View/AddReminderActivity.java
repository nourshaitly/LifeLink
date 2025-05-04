package com.example.lifelink.View;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lifelink.Model.Reminder;
import com.example.lifelink.R;
import com.google.android.material.chip.Chip;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddReminderActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    private EditText medicineNameInput;
    private Button timePickerButton, saveReminderButton;
    private TextView selectedTimeText;

    private Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun; // ✅ Day chips

    private int selectedHour = -1;
    private int selectedMinute = -1;

    private Reminder existingReminder;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }

        medicineNameInput = findViewById(R.id.medicineNameInput);
        timePickerButton = findViewById(R.id.timePickerButton);
        saveReminderButton = findViewById(R.id.saveReminderButton);
        selectedTimeText = findViewById(R.id.selectedTimeText);

        chipMon = findViewById(R.id.chipMon);
        chipTue = findViewById(R.id.chipTue);
        chipWed = findViewById(R.id.chipWed);
        chipThu = findViewById(R.id.chipThu);
        chipFri = findViewById(R.id.chipFri);
        chipSat = findViewById(R.id.chipSat);
        chipSun = findViewById(R.id.chipSun);

        if (getIntent().hasExtra("reminder")) {
            existingReminder = (Reminder) getIntent().getSerializableExtra("reminder");
            if (existingReminder != null) {
                medicineNameInput.setText(existingReminder.getName());

                String[] timeParts = existingReminder.getTime().split(":");
                if (timeParts.length == 2) {
                    selectedHour = Integer.parseInt(timeParts[0]);
                    selectedMinute = Integer.parseInt(timeParts[1]);
                    selectedTimeText.setText("Time: " + existingReminder.getTime());
                }

                // ✅ Load selected days if editing
                if (existingReminder.getDays() != null) {
                    for (String day : existingReminder.getDays()) {
                        switch (day) {
                            case "Mon": chipMon.setChecked(true); break;
                            case "Tue": chipTue.setChecked(true); break;
                            case "Wed": chipWed.setChecked(true); break;
                            case "Thu": chipThu.setChecked(true); break;
                            case "Fri": chipFri.setChecked(true); break;
                            case "Sat": chipSat.setChecked(true); break;
                            case "Sun": chipSun.setChecked(true); break;
                        }
                    }
                }
            }
        }

        timePickerButton.setOnClickListener(v -> openTimePicker());

        saveReminderButton.setOnClickListener(v -> {
            String name = medicineNameInput.getText().toString().trim();

            if (name.isEmpty() || selectedHour == -1) {
                Toast.makeText(this, "Please enter medicine name and pick a time", Toast.LENGTH_SHORT).show();
                return;
            }

            String time = String.format("%02d:%02d", selectedHour, selectedMinute);

            List<String> selectedDays = new ArrayList<>();
            if (chipMon.isChecked()) selectedDays.add("Mon");
            if (chipTue.isChecked()) selectedDays.add("Tue");
            if (chipWed.isChecked()) selectedDays.add("Wed");
            if (chipThu.isChecked()) selectedDays.add("Thu");
            if (chipFri.isChecked()) selectedDays.add("Fri");
            if (chipSat.isChecked()) selectedDays.add("Sat");
            if (chipSun.isChecked()) selectedDays.add("Sun");

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            CollectionReference remindersRef = db.collection("users")
                    .document(userId)
                    .collection("reminders");

            Map<String, Object> reminderData = new HashMap<>();
            reminderData.put("name", name);
            reminderData.put("time", time);
            reminderData.put("taken", false);
            reminderData.put("days", selectedDays);

            if (existingReminder != null && existingReminder.getId() != null) {
                remindersRef.document(existingReminder.getId())
                        .set(reminderData)
                        .addOnSuccessListener(aVoid -> {
                            Reminder updatedReminder = new Reminder();
                            updatedReminder.setId(existingReminder.getId());
                            updatedReminder.setName(name);
                            updatedReminder.setTime(time);
                            updatedReminder.setTaken(false);
                            updatedReminder.setDays(selectedDays);

                            scheduleAlarm(updatedReminder, selectedHour, selectedMinute);
                            Toast.makeText(this, "Reminder updated", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                remindersRef.add(reminderData)
                        .addOnSuccessListener(documentReference -> {
                            Reminder newReminder = new Reminder();
                            newReminder.setId(documentReference.getId());
                            newReminder.setName(name);
                            newReminder.setTime(time);
                            newReminder.setTaken(false);
                            newReminder.setDays(selectedDays);

                            scheduleAlarm(newReminder, selectedHour, selectedMinute);
                            Toast.makeText(this, "Reminder added", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add reminder: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void openTimePicker() {
        MaterialTimePicker picker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(selectedHour != -1 ? selectedHour : 8)
                .setMinute(selectedMinute != -1 ? selectedMinute : 0)
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

    private void scheduleAlarm(Reminder reminder, int hour, int minute) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                Toast.makeText(this, "Please grant exact alarm permission.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (reminder.getDays() != null && !reminder.getDays().isEmpty()) {
            for (String day : reminder.getDays()) {
                Calendar calendar = Calendar.getInstance();
                switch (day) {
                    case "Mon": calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); break;
                    case "Tue": calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY); break;
                    case "Wed": calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY); break;
                    case "Thu": calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY); break;
                    case "Fri": calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY); break;
                    case "Sat": calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY); break;
                    case "Sun": calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY); break;
                }

                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                    calendar.add(Calendar.WEEK_OF_YEAR, 1);
                }

                Intent intent = new Intent(this, ReminderAlarmReceiver.class);
                intent.putExtra("medicineName", reminder.getName());
                intent.putExtra("reminderId", reminder.getId());

                int requestCode = (reminder.getId() + day).hashCode();
                PendingIntent pendingIntent = PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                );

                if (alarmManager != null) {
                    alarmManager.setRepeating(
                            AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(),
                            AlarmManager.INTERVAL_DAY * 7,
                            pendingIntent
                    );
                }
            }
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }

            Intent intent = new Intent(this, ReminderAlarmReceiver.class);
            intent.putExtra("medicineName", reminder.getName());
            intent.putExtra("reminderId", reminder.getId());

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    reminder.getId().hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            if (alarmManager != null) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "Exact alarm permission not granted.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders won't trigger notifications.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
