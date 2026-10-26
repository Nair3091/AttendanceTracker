package com.example.attendancetracker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.attendancetracker.models.Course;
import java.util.ArrayList;
import java.util.List;

public class CourseDao {
    private DatabaseHelper dbHelper;

    public CourseDao(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public long insertCourse(String name, String code, float targetPercentage) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("code", code);
        values.put("target_percentage", targetPercentage);
        return db.insert("courses", null, values);
    }

    public List<Course> getAllCourses() {
        List<Course> courseList = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Query courses and compute present/absent counts for each course
        Cursor cursor = db.rawQuery(
                "SELECT c.id, c.name, c.code, c.target_percentage, " +
                        "(SELECT COUNT(*) FROM attendance a WHERE a.course_id = c.id AND a.status = 'P') AS present, " +
                        "(SELECT COUNT(*) FROM attendance a WHERE a.course_id = c.id AND a.status = 'A') AS absent " +
                        "FROM courses c",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String code = cursor.getString(cursor.getColumnIndexOrThrow("code"));
                float targetPercentage = cursor.getFloat(cursor.getColumnIndexOrThrow("target_percentage"));
                int presentCount = cursor.getInt(cursor.getColumnIndexOrThrow("present"));
                int absentCount = cursor.getInt(cursor.getColumnIndexOrThrow("absent"));

                // Matches the 6-parameter constructor in Course.java
                courseList.add(new Course(id, name, code, targetPercentage, presentCount, absentCount));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return courseList;
    }
}