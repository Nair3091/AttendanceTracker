package com.example.attendancetracker.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.attendancetracker.R;
import com.example.attendancetracker.activities.MarkAttendanceActivity;
import com.example.attendancetracker.db.DatabaseHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TodaysClassesFragment extends Fragment {
    private ListView listViewToday;
    private DatabaseHelper dbHelper;
    private List<Long> courseIdsList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todays_classes, container, false);
        listViewToday = view.findViewById(R.id.listViewToday);
        dbHelper = new DatabaseHelper(requireContext());

        loadTodaysClasses();

        // When a class slot is clicked, navigate to MarkAttendanceActivity for that course
        listViewToday.setOnItemClickListener((parent, v, position, id) -> {
            if (!courseIdsList.isEmpty() && position < courseIdsList.size()) {
                long courseId = courseIdsList.get(position);
                Intent intent = new Intent(requireContext(), MarkAttendanceActivity.class);
                intent.putExtra("course_id", courseId);
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), "No classes scheduled for this slot", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTodaysClasses(); // Refresh data whenever returning to this tab
    }

    private void loadTodaysClasses() {
        // Automatically fetches current day name matching the DB (e.g., "Monday", "Tuesday")
        String currentDay = new SimpleDateFormat("EEEE", Locale.getDefault()).format(new Date());

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> todaysClasses = new ArrayList<>();
        courseIdsList.clear();

        // Query timetable joined with courses for the current day
        Cursor cursor = db.rawQuery(
                "SELECT t.course_id, t.slot_start, c.name FROM timetable t " +
                        "JOIN courses c ON t.course_id = c.id WHERE t.day_of_week = ? ORDER BY t.slot_start",
                new String[]{currentDay}
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long courseId = cursor.getLong(cursor.getColumnIndexOrThrow("course_id"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("slot_start"));
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));

                todaysClasses.add(courseName + " (" + time + ")");
                courseIdsList.add(courseId);
            } while (cursor.moveToNext());
            cursor.close();
        }

        // Fallback message if no classes are scheduled for today
        if (todaysClasses.isEmpty()) {
            todaysClasses.add("No classes scheduled for today (" + currentDay + ")");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, todaysClasses);
        listViewToday.setAdapter(adapter);
    }
}