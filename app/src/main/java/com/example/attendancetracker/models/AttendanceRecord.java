package com.example.attendancetracker.models;

public class AttendanceRecord {
    private long id;
    private String date;
    private String slotStart;
    private String status;
    private String reasonCategory;

    public AttendanceRecord(long id, String date, String slotStart, String status, String reasonCategory) {
        this.id = id;
        this.date = date;
        this.slotStart = slotStart;
        this.status = status;
        this.reasonCategory = reasonCategory;
    }

    public long getId() { return id; }
    public String getDate() { return date; }
    public String getSlotStart() { return slotStart; }
    public String getStatus() { return status; }
    public String getReasonCategory() { return reasonCategory; }
}