package com.example.nursely;

public class EventItem {
    String date;
    String time;
    String patientName;
    String address;

    public EventItem(String date, String time, String patientName, String address) {
        this.date = date;
        this.time = time;
        this.patientName = patientName;
        this.address = address;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getPatientName() { return patientName; }
    public String getAddress() { return address; }
}
