package com.example.attendancetracker.models;

public class Course {
    private long id;
    private String name;
    private String code;
    private float targetPercentage;
    private int presentCount;
    private int absentCount;

    public Course(long id, String name, String code, float targetPercentage, int presentCount, int absentCount) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.targetPercentage = targetPercentage;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public float getTargetPercentage() { return targetPercentage; }
    public int getPresentCount() { return presentCount; }
    public int getAbsentCount() { return absentCount; }
}