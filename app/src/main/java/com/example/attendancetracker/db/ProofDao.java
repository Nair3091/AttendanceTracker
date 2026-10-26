package com.example.attendancetracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ProofDao {
    private DatabaseHelper dbHelper;

    public ProofDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertProof(long attendanceId, String filePath, String fileType, String displayName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("attendance_id", attendanceId);
        values.put("file_path", filePath);
        values.put("file_type", fileType);
        values.put("display_name", displayName);
        return db.insert("proofs", null, values);
    }

    public Cursor getProofsForAttendance(long attendanceId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.rawQuery("SELECT * FROM proofs WHERE attendance_id = ?", new String[]{String.valueOf(attendanceId)});
    }

    public void deleteProof(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("proofs", "id = ?", new String[]{String.valueOf(id)});
    }
}