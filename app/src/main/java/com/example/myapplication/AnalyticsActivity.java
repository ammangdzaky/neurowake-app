package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
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
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all wake-up logs?")
                    .setPositiveButton("Yes, Clear", (dialog, which) -> {
                        clearAllLogs();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Fitur Info Detail Rank saat Card dipencet
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

        String rank;
        if (total == 0) rank = "Newbie";
        else if (total < 5) rank = "Starter";
        else if (total < 15) rank = "Consistent";
        else if (total < 30) rank = "Early Bird";
        else rank = "Master";
        
        tvDisciplineLevel.setText(rank);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, logList);
        lvLogs.setAdapter(adapter);
    }

    private void showRankInfoDialog() {
        String infoMessage = "Tingkatkan kedisiplinan Anda untuk meraih rank tertinggi:\n\n" +
                "• NEWBIE: 0 Wakeups\n" +
                "• STARTER: 1-4 Wakeups\n" +
                "• CONSISTENT: 5-14 Wakeups\n" +
                "• EARLY BIRD: 15-29 Wakeups\n" +
                "• MASTER: 30+ Wakeups\n\n" +
                "Setiap Anda berhasil mematikan alarm, skor akan bertambah!";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("🏆 Rank Information")
                .setMessage(infoMessage)
                .setPositiveButton("I UNDERSTAND", null)
                .show();
    }

    private void clearAllLogs() {
        try (FileOutputStream fos = openFileOutput("discipline_logs.txt", MODE_PRIVATE)) {
            fos.write("".getBytes());
            Toast.makeText(this, "All logs cleared!", Toast.LENGTH_SHORT).show();
            loadLogs();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}