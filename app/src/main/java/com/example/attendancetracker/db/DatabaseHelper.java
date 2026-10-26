package com.example.attendancetracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "attendance_tracker.db";
    private static final int DATABASE_VERSION = 3; // Incremented version to apply schema update

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Courses table
        db.execSQL("CREATE TABLE courses (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "code TEXT, " +
                "target_percentage REAL DEFAULT 75.0);");

        // Attendance table with all required columns and UNIQUE constraint for upsert/replace
        db.execSQL("CREATE TABLE attendance (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER, " +
                "date TEXT, " +
                "slot_start TEXT, " +
                "status TEXT, " +
                "reason_category TEXT, " +
                "reason_text TEXT, " +
                "leave_status TEXT, " +
                "UNIQUE(course_id, date, slot_start), " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE);");

        // Timetable table
        db.execSQL("CREATE TABLE timetable (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "course_id INTEGER, " +
                "day_of_week TEXT, " +
                "slot_start TEXT, " +
                "slot_end TEXT, " +
                "FOREIGN KEY(course_id) REFERENCES courses(id) ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS timetable");
        db.execSQL("DROP TABLE IF EXISTS attendance");
        db.execSQL("DROP TABLE IF EXISTS courses");
        onCreate(db);
    }
}