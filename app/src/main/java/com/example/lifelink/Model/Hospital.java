package com.example.lifelink.Model;


public class Hospital {
    private String name;
    private double lat;
    private double lng;
    private String address;

    // Optional: add phone, 24/7 availability, etc.

    public Hospital(String name, double lat, double lng, String address) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
    }

    // Required empty constructor for JSON parsing (Gson)
    public Hospital() {}

    // Getters
    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public String getAddress() {
        return address;
    }

    // Setters (optional if using Gson)
    public void setName(String name) {
        this.name = name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Add this field and its getter/setter to Hospital.java
    private float distance;

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

}
