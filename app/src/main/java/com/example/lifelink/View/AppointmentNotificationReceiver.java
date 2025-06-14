package com.example.lifelink.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.lifelink.R;

public class AppointmentNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "appointment_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
      //  Toast.makeText(context, "📅 Appointment Broadcast Received", Toast.LENGTH_SHORT).show();

        String doctorName = intent.getStringExtra("doctorName");
        String location=intent.getStringExtra("location");
        if (doctorName == null) doctorName = "Your Doctor";

        // Open main dashboard on tap (or change to another activity if needed)
        Intent openIntent = new Intent(context, DashboardActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ✅ Create notification channel if needed (Android 8+)
        NotificationManager notificationManager = ContextCompat.getSystemService(context, NotificationManager.class);
        if (notificationManager == null) {
            Toast.makeText(context, "❌ NotificationManager is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = nm.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Appointment Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Doctor appointment reminders");
                channel.enableVibration(true);
                channel.setShowBadge(true);
                channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                nm.createNotificationChannel(channel);
            }
        }


        // ✅ Safe icon to avoid crashes (for test use android built-in icon)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_notification_clear_all)

                .setContentTitle("Doctor Appointment")
                .setContentText("Reminder: Appointment with Dr. " + doctorName+"at"+location)


                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Reminder: Appointment with Dr. " + doctorName +"at"+location+ " — tap to open app"))

                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        // ✅ Final trigger
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
       // Toast.makeText(context, "✅ Notification built and triggered", Toast.LENGTH_SHORT).show();
    }
}