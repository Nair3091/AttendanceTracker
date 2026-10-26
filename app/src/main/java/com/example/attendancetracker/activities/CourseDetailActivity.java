package com.example.attendancetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.AttendanceDao;
import com.example.attendancetracker.utils.AttendanceCalculator;
import com.example.attendancetracker.views.ProgressRingView;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView tvCourseName, tvCourseCode, tvAttendanceStats;
    private ProgressRingView progressRing;
    private Button btnMarkAttendance, btnViewAbsences;
    private long courseId = -1;
    private String courseName, courseCode;
    private float targetPercent;
    private AttendanceDao attendanceDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_detail);

        attendanceDao = new AttendanceDao(this);

        // Safely retrieve intent extras passed from CourseGridAdapter
        courseId = getIntent().getLongExtra("course_id", -1);
        courseName = getIntent().getStringExtra("course_name");
        courseCode = getIntent().getStringExtra("course_code");
        targetPercent = getIntent().getFloatExtra("target_percent", 75.0f);

        tvCourseName = findViewById(R.id.tvDetailCourseName);
        tvCourseCode = findViewById(R.id.tvDetailCourseCode);
        tvAttendanceStats = findViewById(R.id.tvAttendanceStats);
        progressRing = findViewById(R.id.detailProgressRing);
        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        btnViewAbsences = findViewById(R.id.btnViewAbsences);

        if (courseName != null) {
            tvCourseName.setText(courseName);
        }
        if (courseCode != null) {
            tvCourseCode.setText(courseCode);
        }

        btnMarkAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailActivity.this, MarkAttendanceActivity.class);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
        });

        btnViewAbsences.setOnClickListener(v -> {
            Intent intent = new Intent(CourseDetailActivity.this, AbsenceDetailActivity.class);
            intent.putExtra("course_id", courseId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAttendanceData();
    }

    private void loadAttendanceData() {
        if (courseId == -1) return;

        int present = attendanceDao.getAttendanceCount(courseId, "P");
        int absent = attendanceDao.getAttendanceCount(courseId, "A");

        double percentage = AttendanceCalculator.calculatePercentage(present, absent);
        if (tvAttendanceStats != null) {
            tvAttendanceStats.setText(String.format("Present: %d | Absent: %d", present, absent));
        }
        if (progressRing != null) {
            progressRing.setProgress((int) percentage, (int) targetPercent);
        }
    }
}