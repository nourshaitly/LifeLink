package com.example.lifelink.Model;

import java.io.Serializable;

public class Appointment implements Serializable {
    private String id; // ✅ Firestore document ID
    private String doctorName;
    private String date;
    private String time;
    private String location;

    public Appointment() {
        // Required empty constructor for Firestore
    }

    public Appointment(String id, String doctorName, String date, String time, String location) {
        this.id = id;
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    // ✅ Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
