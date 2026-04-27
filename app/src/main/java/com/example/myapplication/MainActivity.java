package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
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

        // MULAI URUTAN PERIZINAN 3 TAHAP
        showPermissionDialogTier1();

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
                        .setMessage("Apakah Anda yakin ingin menghapus alarm ini?")
                        .setPositiveButton("Hapus", (dialog, which) -> {
                            alarmList.remove(position);
                            FileHelper.saveAlarmList(alarmList, MainActivity.this);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(MainActivity.this, "Alarm berhasil dihapus", Toast.LENGTH_SHORT).show();
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
        
        btnInfo.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(MainActivity.this)
                    .setTitle("Tentang NeuroWake")
                    .setIcon(R.drawable.ic_info_circle)
                    .setMessage("NeuroWake adalah sistem alarm cerdas yang dirancang untuk memastikan Anda bangun dengan pikiran yang segar dan aktif.\n\nDikembangkan oleh: Abdurrahman Dzaky Safrullah")
                    .setNeutralButton("Lihat Kode Sumber", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ammangdzaky/neurowake-app"));
                        startActivity(intent);
                    })
                    .setPositiveButton("Mantap!", null)
                    .show();
        });
    }

    // --- LOGIKA PERIZINAN 3 TAHAP ---

    private void showPermissionDialogTier1() {
        SharedPreferences prefs = getSharedPreferences("NeuroWakePrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("PERMISSIONS_FINALIZED", false)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Izin Penting Diperlukan")
                    .setMessage("Agar NeuroWake bisa membangunkan Anda otomatis saat layar mati, kami butuh satu izin kecil:\n\n1. Klik 'Buka Pengaturan'.\n2. Pilih 'Perizinan Lainnya'.\n3. Ceklis 'Tampil di Jendela' atau 'Layar Kunci'.")
                    .setCancelable(false)
                    .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                        openAppSettings();
                    })
                    .setNegativeButton("Sudah Saya Izinkan", (dialog, which) -> {
                        showPermissionDialogTier2();
                    })
                    .show();
        }
    }

    private void showPermissionDialogTier2() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Yakin Deck?")
                .setMessage("Nanti kalau alarmnya nggak bunyi karena perizinannya belum aktif, jangan salahkan NeuroWake ya kalau kamu telat kuliah/kerja! 🥺")
                .setCancelable(false)
                .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("Sudah Kok!", (dialog, which) -> {
                    showPermissionDialogTier3();
                })
                .show();
    }

    private void showPermissionDialogTier3() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Emang Ea?")
                .setMessage("Emang ea sudah diaktifkan? Kasih izin dulu dong biar NeuroWake makin jago jagain kamu! 🙏\n\nSetelah ini kami nggak akan tanya lagi, janji!")
                .setCancelable(false)
                .setPositiveButton("Buka Pengaturan", (dialog, which) -> {
                    openAppSettings();
                })
                .setNegativeButton("Oke, Paham!", (dialog, which) -> {
                    // Tandai bahwa user sudah melewati semua tahap, jangan tanya lagi selamanya.
                    getSharedPreferences("NeuroWakePrefs", MODE_PRIVATE)
                            .edit().putBoolean("PERMISSIONS_FINALIZED", true).apply();
                    Toast.makeText(this, "NeuroWake Siap Beraksi!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void openAppSettings() {
        // Setelah klik buka pengaturan, kita anggap user akan mengaktifkannya, 
        // jadi kita jangan munculkan lagi dialognya nanti.
        getSharedPreferences("NeuroWakePrefs", MODE_PRIVATE)
                .edit().putBoolean("PERMISSIONS_FINALIZED", true).apply();
        
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
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