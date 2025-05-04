package com.example.lifelink.Model;

import java.io.Serializable;
import java.util.List;

public class Reminder implements Serializable {
    private String id; // Firestore document ID
    private String name;
    private String time;
    private boolean taken;
    private List<String> days; // ✅ New field: selected days (e.g., ["Mon", "Wed", "Fri"])

    public Reminder() {
        // Required empty constructor for Firestore
    }

    public Reminder(String id, String name, String time, boolean taken, List<String> days) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.taken = taken;
        this.days = days;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public boolean isTaken() { return taken; }
    public void setTaken(boolean taken) { this.taken = taken; }

    public List<String> getDays() { return days; }
    public void setDays(List<String> days) { this.days = days; }
}
