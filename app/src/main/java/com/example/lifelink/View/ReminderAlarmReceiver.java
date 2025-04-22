package com.example.lifelink.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.lifelink.R;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    public static MediaPlayer mediaPlayer;

    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    private static final String ACTION_MARK_AS_TAKEN = "MARK_AS_TAKEN";
    private static final String ACTION_STOP_ALARM = "STOP_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicineName");
        if (medicineName == null) medicineName = "your medicine";

        // ✅ Handle Mark as Taken
        if (ACTION_MARK_AS_TAKEN.equals(intent.getAction())) {
            stopAlarmSound();
            Toast.makeText(context, "Marked as taken: " + medicineName, Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Handle Stop Alarm
        if (ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarmSound();
            Toast.makeText(context, "Alarm stopped.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Start alarm sound
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.alarm_sound);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }

        createNotificationChannel(context);

        // Open Dashboard on tap
        Intent dashboardIntent = new Intent(context, ReminderFragment.class);
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, dashboardIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // "Mark as Taken" action
        Intent markIntent = new Intent(context, ReminderAlarmReceiver.class);
        markIntent.setAction(ACTION_MARK_AS_TAKEN);
        markIntent.putExtra("medicineName", medicineName);
        PendingIntent markPendingIntent = PendingIntent.getBroadcast(
                context, 1, markIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // "Stop Alarm" action
        Intent stopIntent = new Intent(context, ReminderAlarmReceiver.class);
        stopIntent.setAction(ACTION_STOP_ALARM);
        stopIntent.putExtra("medicineName", medicineName);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                context, 2, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("Medicine Reminder")
                .setContentText("Time to take: " + medicineName)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300})
                .addAction(R.drawable.ic_check, "Mark as Taken", markPendingIntent)
                .addAction(R.drawable.ic_stop, "Stop Alarm", stopPendingIntent); // ✅ NEW BUTTON

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void stopAlarmSound() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Medicine reminders with looping alarm sound");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 300, 200, 300});
                channel.setSound(null, null); // Disable system sound (MediaPlayer used)
                manager.createNotificationChannel(channel);
            }
        }
    }
}
