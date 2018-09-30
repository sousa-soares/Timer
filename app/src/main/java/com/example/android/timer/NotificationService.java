package com.example.android.timer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationService extends Service {
    private long endTime = 0;
    private boolean bStarted;
    private boolean destroy = false;

    /**
     * notification method
     */
    public void notifyTimer(){
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        endTime = preferences.getLong("endTime", endTime);
        Log.e("Service", "endTime: " + endTime);

        final int NOTIFICATION_ID = 1910;
        final String CHANNEL_ID = "1910";
        final CharSequence CHANNEL_NAME = "ZG_Channel";
        final String CHANNEL_DESCRIPTION = "ZomGenius Channel";
        final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_LOW;

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, CHANNEL_IMPORTANCE);
            notificationChannel.setDescription(CHANNEL_DESCRIPTION);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.WHITE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200});
            notificationChannel.setShowBadge(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_timer)
                .setChannelId(CHANNEL_ID)
                .setAutoCancel(true);

        //building notification
        Intent intent = new Intent(this, TimerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pendingIntent);

        //notification
        notificationBuilder
                .setTicker("Tempo esgotado")
                .setContentTitle("Tempo esgotado");

        //sending notification
        Log.e("Service", "Loop");
        if (endTime <= System.currentTimeMillis()) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
            Log.e("Service", "Notification");
            destroy = true;
        }
        //SystemClock.sleep(1000);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopService(new Intent(this, NotificationService.class));
    }

    public void destroy() {
        stopService(new Intent(this, NotificationService.class));
        destroy = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        bStarted = preferences.getBoolean("bStarted", bStarted);
        Log.e("Service", "Service running");
        if (bStarted)
            notifyTimer();
        else
            destroy();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service", "Service destroyed");
        if (!TimerActivity.isOpen && !destroy)
            startService(new Intent(this, NotificationService.class));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
