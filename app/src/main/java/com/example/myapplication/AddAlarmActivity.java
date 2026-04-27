package com.example.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Calendar;

public class AddAlarmActivity extends AppCompatActivity {

    private EditText editAlarmName;
    private Spinner spinnerCategory;
    private RadioGroup rgDifficulty;
    private Button btnSave;
    private TextView tvDisplayTime;
    private LinearLayout btnClickArea;

    private String selectedTime = "00:00";
    private int selectedHour = 0;
    private int selectedMinute = 0;
    
    private int editPosition = -1;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alarm);

        editAlarmName = findViewById(R.id.edit_alarm_name);
        spinnerCategory = findViewById(R.id.spinner_category);
        rgDifficulty = findViewById(R.id.rg_difficulty);
        btnSave = findViewById(R.id.btn_save_alarm);
        tvDisplayTime = findViewById(R.id.tv_display_time);
        btnClickArea = findViewById(R.id.btn_click_area);

        String[] categories = {"Bangun Tidur", "Ibadah", "Kuliah", "Olahraga", "Kerja"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        Calendar now = Calendar.getInstance();
        updateTimeDisplay(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE));

        btnClickArea.setOnClickListener(v -> {
            TimePickerDialog mTimePicker = new TimePickerDialog(
                    AddAlarmActivity.this,
                    android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                    (timePicker, hourOfDay, minuteOfHour) -> updateTimeDisplay(hourOfDay, minuteOfHour),
                    selectedHour, selectedMinute, true);
            mTimePicker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            mTimePicker.show();
        });

        isEditMode = getIntent().getBooleanExtra("IS_EDIT", false);
        editPosition = getIntent().getIntExtra("ALARM_POSITION", -1);

        if (isEditMode) {
            String name = getIntent().getStringExtra("ALARM_NAME");
            String category = getIntent().getStringExtra("ALARM_CATEGORY");
            String time = getIntent().getStringExtra("ALARM_TIME");

            editAlarmName.setText(name);
            if (time != null) {
                String[] parts = time.split(":");
                updateTimeDisplay(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
            
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            btnSave.setText("UPDATE ALARM");
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editAlarmName.getText().toString();
                String category = spinnerCategory.getSelectedItem().toString();

                if (name.isEmpty()) name = "Bangun woi!";

                String difficulty = "Easy";
                if (rgDifficulty.getCheckedRadioButtonId() == R.id.rb_hard) {
                    difficulty = "Hard";
                }

                ArrayList<AlarmModel> currentList = FileHelper.readAlarmList(AddAlarmActivity.this);
                AlarmModel alarmToSave = new AlarmModel(selectedTime, name, category);

                if (isEditMode && editPosition != -1) {
                    currentList.set(editPosition, alarmToSave);
                } else {
                    currentList.add(alarmToSave);
                }

                FileHelper.saveAlarmList(currentList, AddAlarmActivity.this);
                scheduleAlarmWithPreNotification(selectedHour, selectedMinute, difficulty);
                finish();
            }
        });
    }

    private void updateTimeDisplay(int hour, int minute) {
        selectedHour = hour;
        selectedMinute = minute;
        selectedTime = String.format("%02d:%02d", hour, minute);
        tvDisplayTime.setText(selectedTime);
    }

    private void scheduleAlarmWithPreNotification(int hour, int minute, String difficulty) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent settingsIntent = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(settingsIntent);
                return;
            }
        }

        String timeStr = String.format("%02d:%02d", hour, minute);

        // 1. Jadwalkan Alarm Utama
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("DIFFICULTY_LEVEL", difficulty);
        intent.putExtra("ALARM_TIME", timeStr);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (hour * 60 + minute), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        if (calendar.before(Calendar.getInstance())) calendar.add(Calendar.DATE, 1);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        // 2. Logika Notifikasi Pra-Alarm (Upcoming)
        Intent preIntent = new Intent(this, AlarmReceiver.class);
        preIntent.setAction(AlarmReceiver.ACTION_PRE_ALARM);
        preIntent.putExtra("ALARM_TIME", timeStr);
        
        Calendar preCalendar = (Calendar) calendar.clone();
        preCalendar.add(Calendar.MINUTE, -30);

        if (preCalendar.after(Calendar.getInstance())) {
            // Jika alarm masih > 30 menit lagi, jadwalkan notifikasi muncul nanti
            PendingIntent prePendingIntent = PendingIntent.getBroadcast(this, (hour * 60 + minute) + 1000, preIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, preCalendar.getTimeInMillis(), prePendingIntent);
        } else {
            // Jika alarm < 30 menit dari sekarang, kirim notifikasi SEKARANG
            sendBroadcast(preIntent);
        }
    }
}