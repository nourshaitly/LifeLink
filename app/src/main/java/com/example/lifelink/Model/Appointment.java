package com.example.lifelink.Model;

public class Appointment {
    private String doctorName;
    private String date;
    private String time;
    private String location;

    public Appointment() {}

    public Appointment(String doctorName, String date, String time, String location) {
        this.doctorName = doctorName;
        this.date = date;
        this.time = time;
        this.location = location;
    }

    public String getDoctorName() { return doctorName; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
}
