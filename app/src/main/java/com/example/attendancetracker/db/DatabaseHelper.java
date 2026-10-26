package com.example.attendancetracker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "attendance_tracker.db";
    private static final int DATABASE_VERSION = 4; // v4 adds the proofs table

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // SQLite ignores FOREIGN KEY / ON DELETE CASCADE unless this is switched on for every connection
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
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

        createProofsTable(db);
    }

    // IF NOT EXISTS so SettingsFragment's "Reset Database" (which calls onCreate again) doesn't crash
    private void createProofsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS proofs (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "attendance_id INTEGER, " +
                "file_path TEXT, " +      // stored file NAME inside filesDir/proofs/
                "file_type TEXT, " +      // MIME type, used when opening the file
                "display_name TEXT, " +   // original file name shown to the user
                "FOREIGN KEY(attendance_id) REFERENCES attendance(id) ON DELETE CASCADE);");
    }

    /**
     * Drops every table and recreates the empty schema, atomically.
     * Used for incompatible legacy databases and by Settings > Reset Database,
     * so the list of tables lives in exactly one place.
     */
    public void resetAllTables(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            db.execSQL("DROP TABLE IF EXISTS proofs");
            db.execSQL("DROP TABLE IF EXISTS timetable");
            db.execSQL("DROP TABLE IF EXISTS attendance");
            db.execSQL("DROP TABLE IF EXISTS courses");
            onCreate(db);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Versions 1-2 were early development schemas that can't be migrated: rebuild.
        if (oldVersion < 3) {
            resetAllTables(db);
            return;
        }

        // Incremental migrations: one block per version, run in order, and never edited
        // after release. Existing courses/attendance are preserved.
        if (oldVersion < 4) {
            // v3 -> v4: proof files for absences
            createProofsTable(db);
        }
        // Template for the next schema change (also bump DATABASE_VERSION):
        // if (oldVersion < 5) {
        //     db.execSQL("ALTER TABLE courses ADD COLUMN color TEXT");
        // }
    }
}