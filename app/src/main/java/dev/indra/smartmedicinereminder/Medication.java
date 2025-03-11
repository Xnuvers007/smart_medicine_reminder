package dev.indra.smartmedicinereminder.model;

import java.io.Serializable;

public class Medication implements Serializable {
    private long id;
    private String name;
    private String dosage;
    private int hour;
    private int minute;
    private boolean isActive;
    private String notes;

    public Medication() {
        this.isActive = true;
    }

    public Medication(String name, String dosage, int hour, int minute) {
        this.name = name;
        this.dosage = dosage;
        this.hour = hour;
        this.minute = minute;
        this.isActive = true;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getTimeString() {
        String hourString = (hour < 10) ? "0" + hour : String.valueOf(hour);
        String minuteString = (minute < 10) ? "0" + minute : String.valueOf(minute);
        return hourString + ":" + minuteString;
    }
}