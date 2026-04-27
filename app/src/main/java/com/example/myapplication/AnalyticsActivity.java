package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class AnalyticsActivity extends AppCompatActivity {

    private TextView tvTotalCount, tvDisciplineLevel, btnClearLogs;
    private ListView lvLogs;
    private ArrayList<String> logList;
    private MaterialCardView cardRank;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        tvTotalCount = findViewById(R.id.tv_total_wakeups_count);
        tvDisciplineLevel = findViewById(R.id.tv_discipline_level);
        lvLogs = findViewById(R.id.lv_logs);
        btnClearLogs = findViewById(R.id.btn_clear_logs);
        cardRank = findViewById(R.id.card_rank);

        loadLogs();

        btnClearLogs.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Hapus Riwayat")
                    .setMessage("Yakin mau hapus semua log bangun kamu? Nanti statistiknya hilang lho!")
                    .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                        clearAllLogs();
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });

        cardRank.setOnClickListener(v -> {
            showRankInfoDialog();
        });
    }

    private void loadLogs() {
        logList = new ArrayList<>();
        try (FileInputStream fis = openFileInput("discipline_logs.txt");
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                logList.add(line);
            }
        } catch (Exception e) { }

        Collections.reverse(logList);

        int total = logList.size();
        tvTotalCount.setText(String.valueOf(total));

        // RANK STATUS DENGAN UNSUR MEME
        String rank;
        if (total == 0) rank = "Beban Keluarga";
        else if (total < 5) rank = "Mulai Sadar";
        else if (total < 15) rank = "Pejuang Subuh";
        else if (total < 30) rank = "Sepuh Pagi";
        else rank = "Legend Pagi";
        
        tvDisciplineLevel.setText(rank);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, logList);
        lvLogs.setAdapter(adapter);
    }

    private void showRankInfoDialog() {
        String infoMessage = "Tingkatkan rank kamu biar makin jago:\n\n" +
                "• BEBAN KELUARGA: 0 Bangun\n" +
                "• MULAI SADAR: 1-4 Bangun\n" +
                "• PEJUANG SUBUH: 5-14 Bangun\n" +
                "• SEPUH PAGI: 15-29 Bangun\n" +
                "• LEGEND PAGI: 30+ Bangun\n\n" +
                "Jangan malas ya, biar rank-nya nggak stuck!";

        new MaterialAlertDialogBuilder(this)
                .setTitle("🏆 Info Peringkat")
                .setMessage(infoMessage)
                .setPositiveButton("SIAP BOS!", null)
                .show();
    }

    private void clearAllLogs() {
        try (FileOutputStream fos = openFileOutput("discipline_logs.txt", MODE_PRIVATE)) {
            fos.write("".getBytes());
            Toast.makeText(this, "Log sudah bersih!", Toast.LENGTH_SHORT).show();
            loadLogs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}