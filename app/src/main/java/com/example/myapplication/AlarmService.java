package com.example.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        String difficulty = "Easy";
        if (intent != null) {
            difficulty = intent.getStringExtra("DIFFICULTY_LEVEL");
        }

        // 1. Buat Intent untuk membuka ChallengeActivity
        Intent challengeIntent = new Intent(this, ChallengeActivity.class);
        challengeIntent.putExtra("DIFFICULTY_LEVEL", difficulty);
        challengeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // 2. Bungkus dalam PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, challengeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // 3. Bangun Notifikasi dengan FullScreenIntent
        Notification notification = new NotificationCompat.Builder(this, "NEUROWAKE_ALARM")
                .setContentTitle("NeuroWake is Active")
                .setContentText("Tap to solve the challenge and stop the alarm!")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // Saat notif diklik
                .setFullScreenIntent(pendingIntent, true) // Muncul otomatis & di lockscreen
                .build();

        startForeground(1, notification);

        // 4. Mainkan Musik menggunakan Volume Alarm (Bukan Media)
        String activeUri = FileHelper.getActiveRingtoneUri(this);
        Uri alarmUri = (activeUri != null) ? Uri.parse(activeUri) : android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;

        try {
            mediaPlayer = new MediaPlayer();
            
            // Setting agar menggunakan output suara ALARM
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback jika terjadi error
            try {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        // Buka Activity secara langsung
        startActivity(challengeIntent);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "NEUROWAKE_ALARM", "Alarm Service Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            serviceChannel.setSound(null, null);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}