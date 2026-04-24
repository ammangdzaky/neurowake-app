package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RingtoneAdapter extends RecyclerView.Adapter<RingtoneAdapter.RingtoneViewHolder> {

    private ArrayList<RingtoneModel> list;
    private OnMusicClickListener listener;
    private OnSelectionChangedListener selectionListener;

    public interface OnMusicClickListener {
        void onMusicClick(String uri);
    }

    // Listener baru untuk mendeteksi perubahan ceklis agar langsung disimpan
    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    public RingtoneAdapter(ArrayList<RingtoneModel> list, OnMusicClickListener listener, OnSelectionChangedListener selectionListener) {
        this.list = list;
        this.listener = listener;
        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public RingtoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_ringtone, parent, false);
        return new RingtoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RingtoneViewHolder holder, int position) {
        RingtoneModel model = list.get(position);
        holder.tvName.setText(model.getFileName());
        
        // Hapus listener lama sebelum set checked untuk menghindari trigger otomatis
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(model.isSelected());

        // Gunakan setOnCheckedChangeListener agar mencakup klik pada teks/area checkbox
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            model.setSelected(isChecked);
            if (selectionListener != null) {
                selectionListener.onSelectionChanged();
            }
        });

        holder.ivIcon.setOnClickListener(v -> {
            if (listener != null) listener.onMusicClick(model.getUri());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class RingtoneViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        CheckBox checkBox;
        ImageView ivIcon;

        public RingtoneViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ringtone_name);
            checkBox = itemView.findViewById(R.id.cb_ringtone);
            ivIcon = itemView.findViewById(R.id.iv_music_icon);
        }
    }
}