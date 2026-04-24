package com.example.myapplication;

import android.content.Context;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

public class FileHelper {
    private static final String RINGTONE_FILE = "ringtones_data.txt";

    public static void saveRingtones(ArrayList<RingtoneModel> list, Context context) {
        try (FileOutputStream fos = context.openFileOutput(RINGTONE_FILE, Context.MODE_PRIVATE)) {
            for (RingtoneModel model : list) {
                String line = model.getUri() + "|" + model.getFileName() + "|" + model.isSelected() + "\n";
                fos.write(line.getBytes());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static ArrayList<RingtoneModel> readRingtonesFull(Context context) {
        ArrayList<RingtoneModel> list = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput(RINGTONE_FILE);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    RingtoneModel model = new RingtoneModel(parts[0], parts[1]);
                    model.setSelected(Boolean.parseBoolean(parts[2]));
                    list.add(model);
                }
            }
        } catch (Exception e) { }
        return list;
    }

    public static String getActiveRingtoneUri(Context context) {
        ArrayList<RingtoneModel> allList = readRingtonesFull(context);
        ArrayList<String> selectedUris = new ArrayList<>();
        for (RingtoneModel m : allList) {
            if (m.isSelected()) selectedUris.add(m.getUri());
        }
        if (selectedUris.isEmpty()) return null;
        return selectedUris.get(new Random().nextInt(selectedUris.size()));
    }

    public static void appendLog(String log, Context context) {
        try (FileOutputStream fos = context.openFileOutput("discipline_logs.txt", Context.MODE_APPEND)) {
            fos.write((log + "\n").getBytes());
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Update simpan: Time|Label|Category|IsActive|IsSkipped
    public static void saveAlarmList(ArrayList<AlarmModel> list, Context context) {
        try (FileOutputStream fos = context.openFileOutput("alarms.txt", Context.MODE_PRIVATE)) {
            for (AlarmModel alarm : list) {
                String line = alarm.getTime() + "|" + alarm.getLabel() + "|" + alarm.getCategory() + "|" + alarm.isActive() + "|" + alarm.isSkipped() + "\n";
                fos.write(line.getBytes());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static ArrayList<AlarmModel> readAlarmList(Context context) {
        ArrayList<AlarmModel> list = new ArrayList<>();
        try (FileInputStream fis = context.openFileInput("alarms.txt");
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 3) {
                    AlarmModel alarm = new AlarmModel(parts[0], parts[1], parts[2]);
                    if (parts.length >= 4) alarm.setActive(Boolean.parseBoolean(parts[3]));
                    if (parts.length >= 5) alarm.setSkipped(Boolean.parseBoolean(parts[4]));
                    list.add(alarm);
                }
            }
        } catch (Exception e) { }
        return list;
    }
}