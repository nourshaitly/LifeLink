package com.example.lifelink.Model;

import com.google.android.gms.maps.model.LatLng;

public class NearbyPlace {

    private String name;
    private LatLng location;
    private String type;
    private float distance;  // in meters

    // Optional: For detailed info later
    private String address;
    private String phone;
    private String email;
    private String website;

    // Constructor


    // Constructor → add them
    public NearbyPlace(String name, LatLng location, String type, String address, String phone, String email, String website) {
        this.name = name;
        this.location = location;
        this.type = type;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.website = website;
    }




    // --- Getters and Setters ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }


    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
