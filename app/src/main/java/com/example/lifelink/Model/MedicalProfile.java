package com.example.lifelink.Model;

import com.google.firebase.firestore.PropertyName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.Serializable;
public class MedicalProfile implements Serializable {

    private String birthdate;
    private String gender;
    private String blood_type;
    private String rh_factor;
    private int height_cm;
    private int weight_kg;
    private boolean smoker;
    private boolean alcoholic;

    @PropertyName("symptoms")
    private List<String> symptoms;

    @PropertyName("family_history")
    private List<String> familyHistory;

    @PropertyName("mental_health")
    private List<String> mentalHealth;

    @PropertyName("chronic_diseases")
    private List<String> chronicDiseases;

    private String medication_list;
    private boolean has_allergies;
    private String allergy_detail;

    private String emergency_name;
    private String emergency_phone;
    private String emergency_relation;

    public MedicalProfile() {
        // Required no-arg constructor for Firestore
    }

    // --- Basic Info ---
    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBlood_type() {
        return blood_type;
    }

    public void setBlood_type(String blood_type) {
        this.blood_type = blood_type;
    }

    public String getRh_factor() {
        return rh_factor;
    }

    public void setRh_factor(String rh_factor) {
        this.rh_factor = rh_factor;
    }

    public int getHeightCm() {
        return height_cm;
    }

    public void setHeightCm(int height_cm) {
        this.height_cm = height_cm;
    }

    public int getWeightKg() {
        return weight_kg;
    }

    public void setWeightKg(int weight_kg) {
        this.weight_kg = weight_kg;
    }

    // --- Additional Fields ---
    public boolean isSmoker() {
        return smoker;
    }

    public void setSmoker(boolean smoker) {
        this.smoker = smoker;
    }

    public boolean isAlcoholic() {
        return alcoholic;
    }

    public void setAlcoholic(boolean alcoholic) {
        this.alcoholic = alcoholic;
    }

    // --- Calculated Age from Birthdate ---
    public int getAge() {
        if (birthdate == null || birthdate.isEmpty()) return 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date birthDateParsed = sdf.parse(birthdate);
            Calendar birth = Calendar.getInstance();
            Calendar today = Calendar.getInstance();
            birth.setTime(birthDateParsed);

            int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // --- Symptoms ---
    @PropertyName("symptoms")
    public List<String> getSymptoms() {
        return symptoms;
    }

    @PropertyName("symptoms")
    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    // --- Family History ---
    @PropertyName("family_history")
    public List<String> getFamilyHistory() {
        return familyHistory;
    }

    @PropertyName("family_history")
    public void setFamilyHistory(List<String> familyHistory) {
        this.familyHistory = familyHistory;
    }

    // --- Mental Health ---
    @PropertyName("mental_health")
    public List<String> getMentalHealth() {
        return mentalHealth;
    }

    @PropertyName("mental_health")
    public void setMentalHealth(List<String> mentalHealth) {
        this.mentalHealth = mentalHealth;
    }

    // --- Chronic Diseases ---
    @PropertyName("chronic")
    public List<String> getChronicDiseases() {
        return chronicDiseases;
    }

    @PropertyName("chronic")
    public void setChronicDiseases(List<String> chronicDiseases) {
        this.chronicDiseases = chronicDiseases;
    }

    // --- Medication / Allergy ---
    public String getMedication_list() {
        return medication_list;
    }

    public void setMedication_list(String medication_list) {
        this.medication_list = medication_list;
    }

    public boolean isHas_allergies() {
        return has_allergies;
    }

    public void setHas_allergies(boolean has_allergies) {
        this.has_allergies = has_allergies;
    }

    public String getAllergy_detail() {
        return allergy_detail;
    }

    public void setAllergy_detail(String allergy_detail) {
        this.allergy_detail = allergy_detail;
    }

    // --- Emergency Contact ---
    public String getEmergency_name() {
        return emergency_name;
    }

    public void setEmergency_name(String emergency_name) {
        this.emergency_name = emergency_name;
    }

    public String getEmergency_phone() {
        return emergency_phone;
    }

    public void setEmergency_phone(String emergency_phone) {
        this.emergency_phone = emergency_phone;
    }

    public String getEmergency_relation() {
        return emergency_relation;
    }

    public void setEmergency_relation(String emergency_relation) {
        this.emergency_relation = emergency_relation;
    }
}
