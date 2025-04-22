package com.example.lifelink.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.lifelink.R;

public class AppointmentNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "appointment_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String doctorName = intent.getStringExtra("doctorName");
        if (doctorName == null) doctorName = "Your Doctor";

        // Create notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Appointment Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Doctor appointment reminders");
            channel.setShowBadge(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            channel.enableVibration(true);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }

        // Create notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_calendar)
                .setContentTitle("Doctor Appointment")
                .setContentText("Reminder: Appointment with Dr. " + doctorName)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Reminder: Appointment with Dr. " + doctorName + " — tap to view or dismiss"))
                .setPriority(NotificationCompat.PRIORITY_HIGH) // for pre-Android 8
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // includes sound, vibrate, lights
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
