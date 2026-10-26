package com.example.attendancetracker.fragments;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.CourseDao;
import com.example.attendancetracker.db.DatabaseHelper;
import com.example.attendancetracker.models.Course;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TimetableFragment extends Fragment {
    private ListView listViewTimetable;
    private FloatingActionButton fabAddTimetableSlot;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);

        dbHelper = new DatabaseHelper(requireContext());
        listViewTimetable = view.findViewById(R.id.listViewTimetable);
        fabAddTimetableSlot = view.findViewById(R.id.fabAddTimetableSlot);

        fabAddTimetableSlot.setOnClickListener(v -> showAddSlotDialog());

        loadTimetable();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTimetable(); // Refresh list when returning to fragment
    }

    private void showAddSlotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_timetable, null);
        builder.setView(dialogView);

        Spinner spinnerCourses = dialogView.findViewById(R.id.spinnerDialogCourses);
        Spinner spinnerDays = dialogView.findViewById(R.id.spinnerDialogDays);
        Button btnPickTime = dialogView.findViewById(R.id.btnDialogPickTime);
        Button btnSaveSlot = dialogView.findViewById(R.id.btnDialogSaveSlot);

        CourseDao courseDao = new CourseDao(requireContext());
        List<Course> courseList = courseDao.getAllCourses();
        List<String> courseNames = new ArrayList<>();
        List<Long> courseIds = new ArrayList<>();

        for (Course course : courseList) {
            courseIds.add(course.getId());
            courseNames.add(course.getName());
        }

        ArrayAdapter<String> courseAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, courseNames);
        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCourses.setAdapter(courseAdapter);

        ArrayAdapter<CharSequence> dayAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDays.setAdapter(dayAdapter);

        final String[] selectedTime = {"09:00"};
        btnPickTime.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(), (view, hourOfDay, minute1) -> {
                selectedTime[0] = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
                btnPickTime.setText("Time: " + selectedTime[0]);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        AlertDialog dialog = builder.create();

        btnSaveSlot.setOnClickListener(v -> {
            if (courseIds.isEmpty()) {
                Toast.makeText(requireContext(), "Please add a course first!", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedCoursePosition = spinnerCourses.getSelectedItemPosition();
            long courseId = courseIds.get(selectedCoursePosition);
            String dayOfWeek = spinnerDays.getSelectedItem().toString();
            String slotStart = selectedTime[0];
            String slotEnd = "10:00";

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("course_id", courseId);
            values.put("day_of_week", dayOfWeek);
            values.put("slot_start", slotStart);
            values.put("slot_end", slotEnd);

            db.insert("timetable", null, values);
            Toast.makeText(requireContext(), "Timetable slot added successfully", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadTimetable();
        });

        dialog.show();
    }

    private void loadTimetable() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        List<String> timetableItems = new ArrayList<>();

        // Join timetable and courses table to display course name along with time and day
        Cursor cursor = db.rawQuery(
                "SELECT t.day_of_week, t.slot_start, c.name FROM timetable t " +
                        "JOIN courses c ON t.course_id = c.id ORDER BY t.day_of_week, t.slot_start",
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String day = cursor.getString(cursor.getColumnIndexOrThrow("day_of_week"));
                String time = cursor.getString(cursor.getColumnIndexOrThrow("slot_start"));
                String courseName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                timetableItems.add(day + " at " + time + " - " + courseName);
            } while (cursor.moveToNext());
            cursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, timetableItems);
        listViewTimetable.setAdapter(adapter);
    }
}