package com.example.lifelink.View;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.lifelink.Model.Tip;
import com.example.lifelink.R;
import com.example.lifelink.View.TipRepository;
import com.example.lifelink.View.WellnessDashboardActivity;

import java.util.List;
import java.util.Random;

public class TipWorker extends Worker {

    private static final String CHANNEL_ID = "wellness_tips_channel";

    public TipWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        List<Tip> tips = TipRepository.getTipsPool(); // this must be preloaded and static
        if (tips == null || tips.isEmpty()) return Result.retry();

        // Pick a random tip
        Tip tip = tips.get(new Random().nextInt(tips.size()));

        // Save it for Today
        TipRepository.addTip(tip);

        // Show notification
        sendNotification(tip);

        return Result.success();
    }

    private void sendNotification(Tip tip) {
        Context context = getApplicationContext();
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Wellness Tips",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, WellnessDashboardActivity.class);
        intent.putExtra("EXTRA_TAB_INDEX", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_health_tracker)
                .setContentTitle(tip.title)
                .setContentText(tip.text + " " + System.currentTimeMillis() % 1000)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
