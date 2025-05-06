package com.example.nursely;

public class EventItemAdmin {
    String date;
    String time;
    String patientName;
    String address;
    String phone;
    String details;
    String notes;
    String nurseLogin;

    public EventItemAdmin(String date, String time, String patientName, String address, String phone, String details, String notes, String nurseLogin) {
        this.date = date;
        this.time = time;
        this.patientName = patientName;
        this.address = address;
        this.phone = phone;
        this.details = details;
        this.notes = notes;
        this.nurseLogin = nurseLogin;

    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getPatientName() { return patientName; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }
    public String getDetails() { return details; }
    public String getNotes() { return notes; }
    public String getNurseLogin() { return nurseLogin; }
}
