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
     * Inserts a record, or updates the existing one IN PLACE when a row for the same
     * (course_id, date, slot_start) already exists. Updating in place keeps the row id
     * stable, so proofs attached to that row stay linked.
     *
     * On an existing row only the status is changed: the reason/leave arguments are used
     * for new rows only (use updateAbsenceDetails to edit them later). If the new status
     * is not 'A', the old absence details are cleared.
     */
    public long upsertAttendance(long courseId, String date, String slotStart, String status,
                                 String reasonCategory, String reasonText, String leaveStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (slotStart == null) slotStart = "";

        long existingId = findAttendanceId(db, courseId, date, slotStart);
        if (existingId != -1) {
            ContentValues update = new ContentValues();
            update.put("status", status);
            if (!"A".equals(status)) {
                update.putNull("reason_category");
                update.putNull("reason_text");
                update.put("leave_status", "None");
            }
            db.update("attendance", update, "id = ?", new String[]{String.valueOf(existingId)});
            return existingId;
        }

        ContentValues values = new ContentValues();
        values.put("course_id", courseId);
        values.put("date", date);
        values.put("slot_start", slotStart);
        values.put("status", status); // 'P', 'A', or 'C'
        values.put("reason_category", reasonCategory);
        values.put("reason_text", reasonText);
        values.put("leave_status", leaveStatus);

        return db.insert("attendance", null, values);
    }

    private long findAttendanceId(SQLiteDatabase db, long courseId, String date, String slotStart) {
        Cursor c = db.rawQuery(
                "SELECT id FROM attendance WHERE course_id = ? AND date = ? AND slot_start = ?",
                new String[]{String.valueOf(courseId), date, slotStart});
        try {
            return c.moveToFirst() ? c.getLong(0) : -1;
        } finally {
            c.close();
        }
    }

    /**
     * Saves the absence form fields onto an existing attendance row.
     * Returns the number of rows updated (1 on success, 0 if the id doesn't exist).
     */
    public int updateAbsenceDetails(long attendanceId, String category, String reasonText, String leaveStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("reason_category", category);
        values.put("reason_text", reasonText);
        values.put("leave_status", leaveStatus);
        return db.update("attendance", values, "id = ?", new String[]{String.valueOf(attendanceId)});
    }

    /**
     * Returns {reason_category, reason_text, leave_status} for a row, or null if not found.
     */
    public String[] getAbsenceDetails(long attendanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT reason_category, reason_text, leave_status FROM attendance WHERE id = ?",
                new String[]{String.valueOf(attendanceId)});
        try {
            if (c.moveToFirst()) {
                return new String[]{c.getString(0), c.getString(1), c.getString(2)};
            }
            return null;
        } finally {
            c.close();
        }
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
     * Absences ('A') only, newest first, for the course's Absences list.
     */
    public List<AttendanceRecord> getAbsencesForCourse(long courseId) {
        List<AttendanceRecord> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id, date, slot_start, status, reason_category FROM attendance " +
                        "WHERE course_id = ? AND status = 'A' ORDER BY date DESC, slot_start DESC",
                new String[]{String.valueOf(courseId)}
        );
        try {
            while (cursor.moveToNext()) {
                list.add(new AttendanceRecord(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("slot_start")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status")),
                        cursor.getString(cursor.getColumnIndexOrThrow("reason_category"))
                ));
            }
        } finally {
            cursor.close();
        }
        return list;
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