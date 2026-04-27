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
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    private MediaPlayer mediaPlayer;
    private Handler autoStopHandler = new Handler(Looper.getMainLooper());
    private Runnable autoStopRunnable;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        String difficulty = "Easy";
        if (intent != null) {
            difficulty = intent.getStringExtra("DIFFICULTY_LEVEL");
        }

        Intent challengeIntent = new Intent(this, ChallengeActivity.class);
        challengeIntent.putExtra("DIFFICULTY_LEVEL", difficulty);
        challengeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, challengeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, "NEUROWAKE_ALARM")
                .setContentTitle("NeuroWake menyala!!!")
                .setContentText("Tekan untuk mematikan alarm!")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true)
                .build();

        startForeground(1, notification);

        // 4. Mainkan Musik menggunakan Volume Alarm
        String activeUri = FileHelper.getActiveRingtoneUri(this);
        Uri alarmUri = (activeUri != null) ? Uri.parse(activeUri) : android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI;

        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }
            mediaPlayer = new MediaPlayer();
            
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setDataSource(this, alarmUri);
            mediaPlayer.setLooping(true); // Lagu akan terus diulang (looping)
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_ALARM_ALERT_URI);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            } catch (Exception ex) { ex.printStackTrace(); }
        }

        // FITUR AUTO-STOP: 10 Menit (600,000 ms)
        autoStopRunnable = () -> {
            // Jika dalam 10 menit tidak dimatikan, service akan berhenti sendiri
            stopSelf(); 
        };
        autoStopHandler.postDelayed(autoStopRunnable, 10 * 60 * 1000);

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
        // Hentikan timer jika service dimatikan (misal user sudah jawab benar)
        if (autoStopHandler != null && autoStopRunnable != null) {
            autoStopHandler.removeCallbacks(autoStopRunnable);
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}