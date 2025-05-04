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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    public static MediaPlayer mediaPlayer;

    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final String CHANNEL_NAME = "Medicine Reminders";
    private static final String ACTION_MARK_AS_TAKEN = "MARK_AS_TAKEN";
    private static final String ACTION_STOP_ALARM = "STOP_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicineName");
        String reminderId = intent.getStringExtra("reminderId"); // ✅ important
        if (medicineName == null) medicineName = "your medicine";

        // ✅ Handle Mark as Taken
        if (ACTION_MARK_AS_TAKEN.equals(intent.getAction())) {
            stopAlarmSound();

            if (reminderId != null) {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                FirebaseAuth auth = FirebaseAuth.getInstance();

                if (auth.getCurrentUser() != null) {
                    String userId = auth.getCurrentUser().getUid();

                    db.collection("users")
                            .document(userId)
                            .collection("reminders")
                            .document(reminderId)
                            .update("taken", true)
                            .addOnSuccessListener(aVoid -> {
                            //    Toast.makeText(context, "Marked as taken: " + medicineName, Toast.LENGTH_SHORT).show();
                                cancelNotification(context);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to mark as taken: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Reminder ID not found", Toast.LENGTH_SHORT).show();
            }

            return;
        }

        // ✅ Handle Stop Alarm
        if (ACTION_STOP_ALARM.equals(intent.getAction())) {
            stopAlarmSound();
            cancelNotification(context);
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

        // Intent to open Reminder screen
        Intent dashboardIntent = new Intent(context, DashboardActivity.class); // open dashboard or reminder fragment
        dashboardIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, 0, dashboardIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // "Mark as Taken" action
        Intent markIntent = new Intent(context, ReminderAlarmReceiver.class);
        markIntent.setAction(ACTION_MARK_AS_TAKEN);
        markIntent.putExtra("medicineName", medicineName);
        markIntent.putExtra("reminderId", reminderId); // ✅ pass id
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

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_check)
                .setContentTitle("Medicine Reminder")
                .setContentText("Time to take: " + medicineName)
                .setContentIntent(contentIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 200, 300})
                .addAction(R.drawable.ic_check, "Mark as Taken", markPendingIntent)
                .addAction(R.drawable.ic_stop, "Stop Alarm", stopPendingIntent);

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

    private void cancelNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll(); // ✅ cancel all after mark or stop
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
                channel.setSound(null, null); // MediaPlayer used
                manager.createNotificationChannel(channel);
            }
        }
    }
}
