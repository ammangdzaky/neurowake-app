package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RingtoneManagerActivity extends AppCompatActivity {

    private RecyclerView rvRingtones;
    private Button btnAdd;
    private ArrayList<RingtoneModel> ringtoneList;
    private RingtoneAdapter adapter;
    private android.media.MediaPlayer previewPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringtone_manager);

        rvRingtones = findViewById(R.id.rv_ringtones);
        rvRingtones.setLayoutManager(new LinearLayoutManager(this));

        btnAdd = findViewById(R.id.btn_add_local_file);
        
        ringtoneList = FileHelper.readRingtonesFull(this);

        // Tambahkan listener ketiga untuk menyimpan setiap kali ada perubahan ceklis
        adapter = new RingtoneAdapter(ringtoneList, uri -> playPreview(uri), () -> {
            FileHelper.saveRingtones(ringtoneList, RingtoneManagerActivity.this);
        });
        rvRingtones.setAdapter(adapter);

        ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                ringtoneList.remove(position);
                adapter.notifyItemRemoved(position);
                FileHelper.saveRingtones(ringtoneList, RingtoneManagerActivity.this);
                Toast.makeText(RingtoneManagerActivity.this, "Lagu dihapus", Toast.LENGTH_SHORT).show();
            }
        };
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvRingtones);

        ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        if (result.getData().getClipData() != null) {
                            int count = result.getData().getClipData().getItemCount();
                            for (int i = 0; i < count; i++) {
                                Uri uri = result.getData().getClipData().getItemAt(i).getUri();
                                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                addRingtoneIfNotDuplicate(uri);
                            }
                        } else if (result.getData().getData() != null) {
                            Uri uri = result.getData().getData();
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            addRingtoneIfNotDuplicate(uri);
                        }
                        adapter.notifyDataSetChanged();
                        FileHelper.saveRingtones(ringtoneList, this);
                    }
                }
        );

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            filePickerLauncher.launch(intent);
        });
    }

    private void addRingtoneIfNotDuplicate(Uri uri) {
        String uriStr = uri.toString();
        for (RingtoneModel item : ringtoneList) {
            if (item.getUri().equals(uriStr)) return;
        }
        ringtoneList.add(new RingtoneModel(uriStr, getFileNameFromUri(uri)));
    }

    public void playPreview(String uriString) {
        try {
            if (previewPlayer != null) {
                previewPlayer.release();
            }
            previewPlayer = new android.media.MediaPlayer();
            previewPlayer.setDataSource(this, Uri.parse(uriString));
            previewPlayer.prepare();
            previewPlayer.start();
        } catch (Exception e) {
            Toast.makeText(this, "Error playing file", Toast.LENGTH_SHORT).show();
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String path = uri.getPath();
        int cut = path.lastIndexOf('/');
        if (cut != -1) path = path.substring(cut + 1);
        return path;
    }

    @Override
    protected void onPause() {
        super.onPause();
        FileHelper.saveRingtones(ringtoneList, this);
        if (previewPlayer != null) previewPlayer.release();
    }
}