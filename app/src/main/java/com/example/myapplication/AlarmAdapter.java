package com.example.myapplication;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.ArrayList;

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private Context context;
    private ArrayList<AlarmModel> alarmList;
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;
    private OnStatusChangedListener statusListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public interface OnStatusChangedListener {
        void onStatusChanged(int position, boolean isActive);
    }

    public AlarmAdapter(Context context, ArrayList<AlarmModel> alarmList, 
                        OnItemClickListener clickListener, 
                        OnItemLongClickListener longClickListener,
                        OnStatusChangedListener statusListener) {
        this.context = context;
        this.alarmList = alarmList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.statusListener = statusListener;
    }

    @NonNull
    @Override
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {
        AlarmModel currentAlarm = alarmList.get(position);

        holder.timeText.setText(currentAlarm.getTime());
        holder.labelText.setText(currentAlarm.getLabel());
        holder.categoryText.setText(currentAlarm.getCategory());

        // Status visual: Aktif hanya jika isActive TRUE DAN isSkipped FALSE
        boolean isUiActive = currentAlarm.isActive() && !currentAlarm.isSkipped();
        
        holder.switchAlarm.setOnCheckedChangeListener(null);
        holder.switchAlarm.setChecked(isUiActive);
        updateUiColors(holder, isUiActive);

        // Gunakan setOnClickListener pada Switch untuk kontrol dialog yang lebih baik
        holder.switchAlarm.setOnClickListener(v -> {
            boolean isChecked = holder.switchAlarm.isChecked();
            
            if (!isChecked) {
                // User mencoba mematikan alarm
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Matikan Alarm?")
                        .setMessage("Apakah Anda ingin melewati alarm ini hanya untuk hari ini, atau mematikannya secara permanen?")
                        .setCancelable(false)
                        .setNeutralButton("Batal", (dialog, which) -> {
                            holder.switchAlarm.setChecked(true); // Kembalikan ke ON
                        })
                        .setNegativeButton("Hanya Hari Ini", (dialog, which) -> {
                            currentAlarm.setSkipped(true);
                            currentAlarm.setActive(true);
                            updateUiColors(holder, false);
                            if (statusListener != null) statusListener.onStatusChanged(position, true);
                        })
                        .setPositiveButton("Permanen", (dialog, which) -> {
                            currentAlarm.setSkipped(false);
                            currentAlarm.setActive(false);
                            updateUiColors(holder, false);
                            if (statusListener != null) statusListener.onStatusChanged(position, false);
                        })
                        .show();
            } else {
                // User menyalakan kembali alarm
                currentAlarm.setActive(true);
                currentAlarm.setSkipped(false);
                updateUiColors(holder, true);
                if (statusListener != null) statusListener.onStatusChanged(position, true);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onItemClick(position);
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) longClickListener.onItemLongClick(position);
            return true;
        });
    }

    private void updateUiColors(AlarmViewHolder holder, boolean isCurrentlyOn) {
        if (isCurrentlyOn) {
            holder.timeText.setTextColor(Color.WHITE);
            holder.categoryText.setTextColor(context.getResources().getColor(R.color.accent_cyan));
            holder.switchAlarm.setThumbTintList(android.content.res.ColorStateList.valueOf(context.getResources().getColor(R.color.accent_cyan)));
        } else {
            holder.timeText.setTextColor(Color.parseColor("#66FFFFFF"));
            holder.categoryText.setTextColor(Color.RED);
            holder.switchAlarm.setThumbTintList(android.content.res.ColorStateList.valueOf(Color.RED));
        }
    }

    @Override
    public int getItemCount() {
        return alarmList.size();
    }

    public static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, labelText, categoryText;
        SwitchMaterial switchAlarm;

        public AlarmViewHolder(@NonNull View itemView) {
            super(itemView);
            timeText = itemView.findViewById(R.id.text_item_time);
            labelText = itemView.findViewById(R.id.text_item_label);
            categoryText = itemView.findViewById(R.id.text_item_category);
            switchAlarm = itemView.findViewById(R.id.switch_alarm);
        }
    }
}