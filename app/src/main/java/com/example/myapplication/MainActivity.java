package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAlarms;
    private FloatingActionButton fabAdd;
    private ArrayList<AlarmModel> alarmList;
    private AlarmAdapter adapter;

    private ImageButton btnMusic, btnStats, btnProfile, btnInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewAlarms = findViewById(R.id.recyclerViewAlarms);
        recyclerViewAlarms.setLayoutManager(new LinearLayoutManager(this));
        
        fabAdd = findViewById(R.id.fab_add_alarm);

        alarmList = new ArrayList<>();
        ArrayList<AlarmModel> savedAlarms = FileHelper.readAlarmList(this);
        if (savedAlarms.isEmpty()) {
            alarmList.add(new AlarmModel("05:00", "Bangun Subuh", "Ibadah"));
            alarmList.add(new AlarmModel("07:30", "Persiapan Kuliah", "Kuliah"));
            FileHelper.saveAlarmList(alarmList, this);
        } else {
            alarmList.addAll(savedAlarms);
        }

        adapter = new AlarmAdapter(this, alarmList, 
            position -> {
                AlarmModel selectedAlarm = alarmList.get(position);
                Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
                intent.putExtra("IS_EDIT", true);
                intent.putExtra("ALARM_POSITION", position);
                intent.putExtra("ALARM_NAME", selectedAlarm.getLabel());
                intent.putExtra("ALARM_CATEGORY", selectedAlarm.getCategory());
                intent.putExtra("ALARM_TIME", selectedAlarm.getTime());
                startActivity(intent);
            }, 
            position -> {
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Hapus Alarm")
                        .setMessage("Hapus alarm ini?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            alarmList.remove(position);
                            FileHelper.saveAlarmList(alarmList, MainActivity.this);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Alarm dihapus permanen", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Batal", null)
                        .show();
            },
            (position, isActive) -> {
                FileHelper.saveAlarmList(alarmList, MainActivity.this);
            }
        );
        
        recyclerViewAlarms.setAdapter(adapter);

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddAlarmActivity.class);
                intent.putExtra("IS_EDIT", false);
                startActivity(intent);
            }
        });

        btnMusic = findViewById(R.id.btn_nav_music);
        btnStats = findViewById(R.id.btn_nav_stats);
        btnProfile = findViewById(R.id.btn_nav_profile);
        btnInfo = findViewById(R.id.btn_nav_info);

        btnMusic.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, RingtoneManagerActivity.class)));
        btnStats.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AnalyticsActivity.class)));
        btnProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
        
        // MODIFIKASI TOMBOL INFO: Menampilkan Dialog Keren dengan Link GitHub Spesifik
        btnInfo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("NeuroWake v1.0")
                    .setIcon(R.drawable.ic_info_circle)
                    .setMessage("NeuroWake is an advanced alarm system designed to ensure you wake up with a clear and active mind.\n\nDeveloped by: Abdurrahman Dzaky Safrullah")
                    .setNeutralButton("Source Code", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ammangdzaky/neurowake-app"));
                        startActivity(intent);
                    })
                    .setPositiveButton("Awesome!", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        alarmList.clear();
        alarmList.addAll(FileHelper.readAlarmList(this));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}