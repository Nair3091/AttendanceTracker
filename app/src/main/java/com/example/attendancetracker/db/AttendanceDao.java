package com.example.attendancetracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.attendancetracker.models.AttendanceRecord;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDao {
    private DatabaseHelper dbHelper;

    public AttendanceDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Inserts or updates an attendance record.
     * The database schema has a UNIQUE constraint on (course_id, date, slot_start).
     */
    public long upsertAttendance(long courseId, String date, String slotStart, String status,
                                 String reasonCategory, String reasonText, String leaveStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("course_id", courseId);
        values.put("date", date);
        values.put("slot_start", slotStart);
        values.put("status", status); // 'P', 'A', or 'C'
        values.put("reason_category", reasonCategory);
        values.put("reason_text", reasonText);
        values.put("leave_status", leaveStatus);

        // Conflict resolution: update existing record instead of adding duplicate
        return db.insertWithOnConflict("attendance", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Query per-course totals for the dashboard or detail view.
     */
    public Cursor getCourseAttendanceCounts(long courseId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery(
                "SELECT " +
                        "SUM(CASE WHEN status = 'P' THEN 1 ELSE 0 END) AS present, " +
                        "SUM(CASE WHEN status = 'A' THEN 1 ELSE 0 END) AS absent, " +
                        "SUM(CASE WHEN status = 'C' THEN 1 ELSE 0 END) AS cancelled " +
                        "FROM attendance WHERE course_id = ?",
                new String[]{String.valueOf(courseId)}
        );
    }

    /**
     * Helper method to get total count for a specific status ('P' or 'A')
     */
    public int getAttendanceCount(long courseId, String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM attendance WHERE course_id = ? AND status = ?",
                new String[]{String.valueOf(courseId), status}
        );
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Query attendance history records for a course to display in history list.
     */
    public List<AttendanceRecord> getAttendanceHistoryForCourse(long courseId) {
        List<AttendanceRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, date, slot_start, status, reason_category FROM attendance WHERE course_id = ? ORDER BY date DESC",
                new String[]{String.valueOf(courseId)}
        );
        if (cursor.moveToFirst()) {
            do {
                list.add(new AttendanceRecord(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("slot_start")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("reason_category"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }
}