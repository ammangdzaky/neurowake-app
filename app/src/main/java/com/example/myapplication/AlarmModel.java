package com.example.myapplication;

public class AlarmModel {
    private String time;
    private String label;
    private String category;
    private boolean isActive;
    private boolean isSkipped; // Flag baru untuk skip sementara

    public AlarmModel(String time, String label, String category) {
        this.time = time;
        this.label = label;
        this.category = category;
        this.isActive = true;
        this.isSkipped = false;
    }

    public String getTime() { return time; }
    public String getLabel() { return label; }
    public String getCategory() { return category; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isSkipped() { return isSkipped; }
    public void setSkipped(boolean skipped) { isSkipped = skipped; }
}