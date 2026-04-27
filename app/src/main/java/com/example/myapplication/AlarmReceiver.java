package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_PRE_ALARM = "com.example.myapplication.PRE_ALARM";
    public static final String ACTION_SKIP_ALARM = "com.example.myapplication.SKIP_ALARM";
    public static final int UPCOMING_NOTIFICATION_ID = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String triggeringTime = intent.getStringExtra("ALARM_TIME");
        String difficulty = intent.getStringExtra("DIFFICULTY_LEVEL");

        if (ACTION_SKIP_ALARM.equals(action)) {
            handleSkipAlarm(context, triggeringTime);
            return;
        }

        if (ACTION_PRE_ALARM.equals(action)) {
            showUpcomingAlarmNotification(context, triggeringTime);
            return;
        }

        // Logika Alarm Utama
        ArrayList<AlarmModel> alarmList = FileHelper.readAlarmList(context);
        boolean shouldPlay = false;

        for (AlarmModel alarm : alarmList) {
            if (normalizeTime(alarm.getTime()).equals(normalizeTime(triggeringTime))) {
                if (alarm.isActive() && !alarm.isSkipped()) {
                    shouldPlay = true;
                }
                alarm.setSkipped(false);
                break;
            }
        }
        
        FileHelper.saveAlarmList(alarmList, context);

        if (!shouldPlay) return;

        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.putExtra("DIFFICULTY_LEVEL", difficulty);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    private void showUpcomingAlarmNotification(Context context, String time) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "UPCOMING_ALARM_CHANNEL";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Notifikasi Alarm", NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        Intent skipIntent = new Intent(context, AlarmReceiver.class);
        skipIntent.setAction(ACTION_SKIP_ALARM);
        skipIntent.putExtra("ALARM_TIME", time);
        PendingIntent skipPendingIntent = PendingIntent.getBroadcast(context, (int)System.currentTimeMillis(), skipIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("Alarm Mendatang")
                .setContentText("Alarm diatur untuk jam " + time)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) 
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Lewati untuk hari ini", skipPendingIntent)
                .setAutoCancel(true);

        manager.notify(UPCOMING_NOTIFICATION_ID, builder.build());
    }

    private void handleSkipAlarm(Context context, String time) {
        ArrayList<AlarmModel> list = FileHelper.readAlarmList(context);
        for (AlarmModel alarm : list) {
            if (normalizeTime(alarm.getTime()).equals(normalizeTime(time))) {
                alarm.setSkipped(true);
                break;
            }
        }
        FileHelper.saveAlarmList(list, context);
        
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(UPCOMING_NOTIFICATION_ID);
        
        android.widget.Toast.makeText(context, "Alarm untuk hari ini dilewati", android.widget.Toast.LENGTH_SHORT).show();
    }

    private String normalizeTime(String time) {
        if (time == null) return "";
        String[] parts = time.split(":");
        if (parts.length != 2) return time;
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            return String.format("%02d:%02d", h, m);
        } catch (Exception e) { return time; }
    }
}