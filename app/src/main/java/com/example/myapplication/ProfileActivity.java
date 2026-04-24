package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ivProfile, ivBadge;
    private EditText etName;
    private TextView tvBadgeTitle, tvBadgeDesc;
    private SharedPreferences sharedPref;
    private Button btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ivProfile = findViewById(R.id.iv_profile_picture);
        etName = findViewById(R.id.et_profile_name);
        tvBadgeTitle = findViewById(R.id.tv_badge_title);
        tvBadgeDesc = findViewById(R.id.tv_badge_desc);
        btnBack = findViewById(R.id.btn_back_dashboard);
        ivBadge = findViewById(R.id.iv_badge_icon);

        sharedPref = getSharedPreferences("NeuroWakeProfile", Context.MODE_PRIVATE);

        loadProfileData();
        updateDisciplineBadge();

        ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        getContentResolver().takePersistableUriPermission(imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        ivProfile.setImageURI(imageUri);
                        saveProfileImage(imageUri.toString());
                    }
                }
        );

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                sharedPref.edit().putString("USER_NAME", s.toString()).apply();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void saveProfileImage(String uri) {
        sharedPref.edit().putString("PROFILE_IMAGE_URI", uri).apply();
    }

    private void loadProfileData() {
        String name = sharedPref.getString("USER_NAME", "Abdurrahman Dzaky Safrullah");
        String imageUriStr = sharedPref.getString("PROFILE_IMAGE_URI", null);

        etName.setText(name);

        if (imageUriStr != null) {
            try {
                ivProfile.setImageURI(Uri.parse(imageUriStr));
            } catch (Exception e) {
                ivProfile.setImageResource(R.drawable.ic_default_avatar);
            }
        }
    }

    private void updateDisciplineBadge() {
        int totalWakeups = 0;
        try (FileInputStream fis = openFileInput("discipline_logs.txt");
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            while (br.readLine() != null) totalWakeups++;
        } catch (Exception e) { }

        String rank, desc;
        if (totalWakeups == 0) {
            rank = "NEWBIE";
            desc = "Start your journey tomorrow!";
        } else if (totalWakeups < 5) {
            rank = "STARTER";
            desc = "Good start! Keep it up.";
        } else if (totalWakeups < 15) {
            rank = "CONSISTENT";
            desc = "You're building a great habit.";
        } else if (totalWakeups < 30) {
            rank = "EARLY BIRD";
            desc = "The sun rises for you!";
        } else {
            rank = "MASTER";
            desc = "A true legend of discipline!";
        }

        tvBadgeTitle.setText(rank);
        tvBadgeDesc.setText(desc);
    }
}