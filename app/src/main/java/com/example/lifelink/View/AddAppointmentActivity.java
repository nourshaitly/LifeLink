package com.example.lifelink.View;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.example.lifelink.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.slider.Slider;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddAppointmentActivity extends AppCompatActivity {

    private EditText editDoctorName, editLocation;
    private Button dateTimePickerButton, saveAppointmentButton, notificationTimeButton;
    private TextView selectedDateTimeText;
    private SwitchCompat switchEnableReminder;
    private Slider reminderTimeSlider;
    private ChipGroup timeUnitChipGroup;
    private Spinner repeatReminderSpinner;
    private MaterialCardView notificationCard;
    private Calendar appointmentCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

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
            String dateTime = selectedDateTimeText.getText().toString().trim();

            if (doctor.isEmpty() || location.isEmpty() || dateTime.equals("No date/time selected")) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (switchEnableReminder.isChecked()) {
                int reminderValue = (int) reminderTimeSlider.getValue();
                int repeatTimes = repeatReminderSpinner.getSelectedItemPosition() + 1;

                String timeUnit = "minutes";
                int selectedChipId = timeUnitChipGroup.getCheckedChipId();
                if (selectedChipId == R.id.chipMinutes) timeUnit = "minutes";
                else if (selectedChipId == R.id.chipHours) timeUnit = "hours";
                else if (selectedChipId == R.id.chipDays) timeUnit = "days";

                StringBuilder summary = new StringBuilder("🔔 Notifications scheduled:\n");

                for (int i = 1; i <= repeatTimes; i++) {
                    long offsetMillis = convertToMillis(reminderValue * i, timeUnit);
                    long triggerAt = appointmentCalendar.getTimeInMillis() - offsetMillis;

                    if (triggerAt > System.currentTimeMillis()) {
                        scheduleReminder(triggerAt, doctor, i);
                        summary.append("• ").append(formatTime(triggerAt)).append("\n");
                    }
                }

                Toast.makeText(this, summary.toString(), Toast.LENGTH_LONG).show();
            }

            Toast.makeText(this, "Appointment saved ✅", Toast.LENGTH_SHORT).show();
            finish();
        });


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
        if (alarmManager != null) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }


    private String formatTime(long millis) {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(millis);
    }






}
