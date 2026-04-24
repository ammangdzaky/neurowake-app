package com.example.myapplication;

import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class ChallengeActivity extends AppCompatActivity {

    private TextView tvTitle, tvQuestion;
    private EditText etAnswer;
    private Button btnVerify;
    private int correctAnswer;
    private String difficulty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setupLockScreenFlags();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenge);

        tvTitle = findViewById(R.id.tv_wake_up_call);
        tvQuestion = findViewById(R.id.tv_math_question);
        etAnswer = findViewById(R.id.et_answer);
        btnVerify = findViewById(R.id.btn_verify);

        SharedPreferences sharedPref = getSharedPreferences("NeuroWakeProfile", Context.MODE_PRIVATE);
        String name = sharedPref.getString("USER_NAME", "USER");
        tvTitle.setText("WAKE UP, " + name.toUpperCase() + "!");

        difficulty = getIntent().getStringExtra("DIFFICULTY_LEVEL");
        if (difficulty == null) difficulty = "Easy";

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Toast.makeText(ChallengeActivity.this, "Finish the logic to stop!", Toast.LENGTH_SHORT).show();
            }
        });

        generateQuestion();

        btnVerify.setOnClickListener(v -> {
            String userAnswer = etAnswer.getText().toString();
            if (!userAnswer.isEmpty()) {
                try {
                    if (Integer.parseInt(userAnswer) == correctAnswer) {
                        Toast.makeText(this, "Neuro-Logic Verified. Alarm Stopped.", Toast.LENGTH_SHORT).show();
                        
                        // 1. Simpan log ke Analytics
                        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                        FileHelper.appendLog("Berhasil Bangun: " + timeStamp, this);

                        // 2. MATIKAN NOTIFIKASI UPCOMING (Penting!)
                        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        if (manager != null) {
                            manager.cancel(AlarmReceiver.UPCOMING_NOTIFICATION_ID);
                        }

                        // 3. Matikan musik alarm
                        stopService(new Intent(this, AlarmService.class));
                        
                        finish();
                    } else {
                        Toast.makeText(this, "Wrong! Brain still foggy?", Toast.LENGTH_SHORT).show();
                        etAnswer.setText("");
                    }
                } catch (NumberFormatException e) {
                    etAnswer.setText("");
                }
            }
        });
    }

    private void setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
            
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }

    private void generateQuestion() {
        Random random = new Random();
        int a, b, c;

        if ("Hard".equalsIgnoreCase(difficulty)) {
            a = random.nextInt(20) + 10;
            b = random.nextInt(10) + 5;
            c = random.nextInt(50) + 1;
            correctAnswer = (a * b) + c;
            tvQuestion.setText("(" + a + " x " + b + ") + " + c + " = ?");
        } else {
            a = random.nextInt(50) + 1;
            b = random.nextInt(50) + 1;
            correctAnswer = a + b;
            tvQuestion.setText(a + " + " + b + " = ?");
        }
    }
}