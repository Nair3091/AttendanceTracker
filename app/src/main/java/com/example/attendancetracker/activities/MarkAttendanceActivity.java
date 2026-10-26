package com.example.attendancetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.AttendanceDao;
import com.example.attendancetracker.db.CourseDao;
import com.example.attendancetracker.models.Course;
import java.util.List;

public class MarkAttendanceActivity extends AppCompatActivity {
    private Spinner courseSpinner;
    private DatePicker datePicker;
    private RadioGroup statusRadioGroup;
    private Button btnSaveAttendance;
    private CourseDao courseDao;
    private AttendanceDao attendanceDao;
    private List<Course> courseList;
    private long incomingCourseId = -1;
    private String incomingSlot = null; // class slot passed from Today's Classes (e.g. "10:00")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);

        courseDao = new CourseDao(this);
        attendanceDao = new AttendanceDao(this);

        // Capture the course ID passed from CourseDetailActivity (if any)
        incomingCourseId = getIntent().getLongExtra("course_id", -1);
        incomingSlot = getIntent().getStringExtra("slot_start");

        courseSpinner = findViewById(R.id.spinnerCourses);
        datePicker = findViewById(R.id.datePicker);
        statusRadioGroup = findViewById(R.id.rgStatus);
        btnSaveAttendance = findViewById(R.id.btnSaveAttendance);

        loadCoursesIntoSpinner();

        btnSaveAttendance.setOnClickListener(v -> saveAttendanceRecord());
    }

    private void loadCoursesIntoSpinner() {
        courseList = courseDao.getAllCourses();
        if (courseList.isEmpty()) return;

        String[] courseNames = new String[courseList.size()];
        int selectedIndex = 0;

        for (int i = 0; i < courseList.size(); i++) {
            courseNames[i] = courseList.get(i).getName();
            // Match the incoming course ID to pre-select it in the spinner
            if (courseList.get(i).getId() == incomingCourseId) {
                selectedIndex = i;
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseNames);
        courseSpinner.setAdapter(adapter);
        courseSpinner.setSelection(selectedIndex);

        // If opened from a specific course detail page, lock the spinner to prevent changing courses
        if (incomingCourseId != -1) {
            courseSpinner.setEnabled(false);
        }
    }

    private void saveAttendanceRecord() {
        if (courseList.isEmpty()) {
            Toast.makeText(this, "Please add a course first", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedPosition = courseSpinner.getSelectedItemPosition();
        Course selectedCourse = courseList.get(selectedPosition);

        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth() + 1;
        int year = datePicker.getYear();
        String dateStr = String.format("%d-%02d-%02d", year, month, day);

        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        String status = "P";
        if (selectedId == R.id.rbAbsent) {
            status = "A";
        } else if (selectedId == R.id.rbCancelled) {
            status = "C";
        }

        // Use the real slot when we came from Today's Classes; manual entries keep the old default
        String slot = incomingSlot != null ? incomingSlot : "09:00";
        long result = attendanceDao.upsertAttendance(selectedCourse.getId(), dateStr, slot, status, null, null, "None");

        if (result != -1) {
            Toast.makeText(this, "Attendance saved successfully", Toast.LENGTH_SHORT).show();

            // If marked Absent, navigate to Absence Detail form
            if (status.equals("A")) {
                Intent intent = new Intent(this, AbsenceDetailActivity.class);
                intent.putExtra("attendance_id", result);
                startActivity(intent);
            }
            finish();
        } else {
            Toast.makeText(this, "Error saving attendance", Toast.LENGTH_SHORT).show();
        }
    }
}