package com.example.myapplication;

public class RingtoneModel {
    private String uri;
    private String fileName;
    private boolean isSelected;

    public RingtoneModel(String uri, String fileName) {
        this.uri = uri;
        this.fileName = fileName;
        this.isSelected = false;
    }

    public String getUri() { return uri; }
    public String getFileName() { return fileName; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}