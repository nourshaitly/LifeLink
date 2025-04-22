package com.example.lifelink.Model;

public class Reminder {

    private String name;    // Name of the medicine (e.g., "White Pill")
    private String time;    // Time to take it (e.g., "Before breakfast")
    private boolean taken;  // Whether it's marked as taken
    private String type;    // Type of medication (pill, capsule, drop)

    // Full constructor
    public Reminder(String name, String time, boolean taken, String type) {
        this.name = name;
        this.time = time;
        this.taken = taken;
        this.type = type;
    }

    // Empty constructor (required if using Firebase or serialization)
    public Reminder() {}

    // Getters
    public String getName() {
        return name;
    }

    public String getTime() {
        return time;
    }

    public boolean isTaken() {
        return taken;
    }

    public String getType() {
        return type;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public void setType(String type) {
        this.type = type;
    }
}
