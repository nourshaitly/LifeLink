package com.example.lifelink.View;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.lifelink.Model.Appointment;
import com.example.lifelink.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddAppointmentActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    private EditText editDoctorName, editLocation;
    private Button dateTimePickerButton, saveAppointmentButton, notificationTimeButton;
    private TextView selectedDateTimeText;
    private SwitchCompat switchEnableReminder;
    private Slider reminderTimeSlider;
    private ChipGroup timeUnitChipGroup;
    private Spinner repeatReminderSpinner;
    private MaterialCardView notificationCard;
    private Calendar appointmentCalendar;

    private Appointment existingAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        requestNotificationPermission(); // ✅ Correct call

        editDoctorName = findViewById(R.id.editDoctorName);
        editLocation = findViewById(R.id.editLocation);
        dateTimePickerButton = findViewById(R.id.dateTimePickerButton);
        selectedDateTimeText = findViewById(R.id.selectedDateTimeText);
        saveAppointmentButton = findViewById(R.id.saveAppointmentButton);
        notificationTimeButton = findViewById(R.id.notificationTimeButton);
        switchEnableReminder = findViewById(R.id.switchEnableReminder);
        reminderTimeSlider = findViewById(R.id.reminderTimeSlider);
        timeUnitChipGroup = findViewById(R.id.timeUnitChipGroup);
        repeatReminderSpinner = findViewById(R.id.repeatReminderSpinner);
        notificationCard = findViewById(R.id.notificationCard);

        appointmentCalendar = Calendar.getInstance();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                new String[]{"1", "2", "3", "4", "5"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatReminderSpinner.setAdapter(adapter);

        if (getIntent().hasExtra("appointment")) {
            existingAppointment = (Appointment) getIntent().getSerializableExtra("appointment");
            if (existingAppointment != null) {
                editDoctorName.setText(existingAppointment.getDoctorName());
                editLocation.setText(existingAppointment.getLocation());
                selectedDateTimeText.setText(existingAppointment.getDate() + ", " + existingAppointment.getTime());
            }
        }

        notificationTimeButton.setOnClickListener(v -> {
            if (notificationCard.getVisibility() == View.GONE) {
                notificationCard.setVisibility(View.VISIBLE);
                notificationTimeButton.setText("Hide Notification Settings");
            } else {
                notificationCard.setVisibility(View.GONE);
                notificationTimeButton.setText("Set Notification Time (optional)");
            }
        });

        dateTimePickerButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Appointment Date")
                    .build();

            datePicker.addOnPositiveButtonClickListener(selection -> {
                appointmentCalendar.setTimeInMillis(selection);

                MaterialTimePicker timePicker = new MaterialTimePicker.Builder()
                        .setTimeFormat(TimeFormat.CLOCK_12H)
                        .setHour(9)
                        .setMinute(0)
                        .setTitleText("Select Appointment Time")
                        .build();

                timePicker.addOnPositiveButtonClickListener(dialog -> {
                    appointmentCalendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
                    appointmentCalendar.set(Calendar.MINUTE, timePicker.getMinute());

                    SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
                    selectedDateTimeText.setText(formatter.format(appointmentCalendar.getTime()));
                });

                timePicker.show(getSupportFragmentManager(), "TIME_PICKER");
            });

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
        });

        saveAppointmentButton.setOnClickListener(v -> {
            String doctor = editDoctorName.getText().toString().trim();
            String location = editLocation.getText().toString().trim();
            String dateTimeText = selectedDateTimeText.getText().toString().trim();

            if (doctor.isEmpty() || location.isEmpty() || dateTimeText.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] parts = dateTimeText.split(", ");
            if (parts.length < 2) {
                Toast.makeText(this, "Invalid date/time format", Toast.LENGTH_SHORT).show();
                return;
            }
            String date = parts[0];
            String time = parts[1];

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth auth = FirebaseAuth.getInstance();

            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = auth.getCurrentUser().getUid();
            CollectionReference appointmentsRef = db.collection("users").document(userId).collection("appointments");

            Map<String, Object> appointmentData = new HashMap<>();
            appointmentData.put("doctorName", doctor);
            appointmentData.put("date", date);
            appointmentData.put("time", time);
            appointmentData.put("location", location);

            if (existingAppointment != null && existingAppointment.getId() != null) {
                appointmentsRef.document(existingAppointment.getId())
                        .set(appointmentData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Appointment updated ✅", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                appointmentsRef.add(appointmentData)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "Appointment added ✅", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to add: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            if (switchEnableReminder.isChecked()) {
                int reminderValue = (int) reminderTimeSlider.getValue();
                int repeatTimes = repeatReminderSpinner.getSelectedItemPosition() + 1;

                String timeUnit = "minutes";
                int selectedChipId = timeUnitChipGroup.getCheckedChipId();
                if (selectedChipId == R.id.chipMinutes) timeUnit = "minutes";
                else if (selectedChipId == R.id.chipHours) timeUnit = "hours";
                else if (selectedChipId == R.id.chipDays) timeUnit = "days";

                for (int i = 1; i <= repeatTimes; i++) {
                    long offsetMillis = convertToMillis(reminderValue * i, timeUnit);
                    long triggerAt = appointmentCalendar.getTimeInMillis() - offsetMillis;

                    if (triggerAt > System.currentTimeMillis()) {
                        scheduleReminder(triggerAt, doctor, i);
                    }
                }
            }
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. Reminders won't trigger notifications.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private long convertToMillis(int value, String unit) {
        switch (unit) {
            case "hours": return value * 60L * 60L * 1000;
            case "days": return value * 24L * 60L * 60L * 1000;
            default: return value * 60L * 1000;
        }
    }

    private void scheduleReminder(long triggerTime, String doctor, int requestCode) {
        Intent intent = new Intent(this, AppointmentNotificationReceiver.class);
        intent.putExtra("doctorName", doctor);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                Intent settingsIntent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(settingsIntent);

                Toast.makeText(this, "Please allow exact alarm permission.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        try {
            if (alarmManager != null) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Cannot set exact alarm: Permission denied.", Toast.LENGTH_SHORT).show();
        }
    }
}
