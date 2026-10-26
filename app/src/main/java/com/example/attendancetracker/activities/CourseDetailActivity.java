package com.example.attendancetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.AttendanceDao;
import com.example.attendancetracker.utils.AttendanceCalculator;
import com.example.attendancetracker.views.ProgressRingView;

public class CourseDetailActivity extends AppCompatActivity {
    private TextView tvCourseName, tvCourseCode, tvAttendanceStats, tvAttendanceAdvice;
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
        tvAttendanceAdvice = findViewById(R.id.tvAttendanceAdvice);
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
            Intent intent = new Intent(CourseDetailActivity.this, AbsenceListActivity.class);
            intent.putExtra("course_id", courseId);
            intent.putExtra("course_name", courseName);
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
        showAdvice(present, absent, percentage);
    }

    /** Tells the student how many classes they can skip, or how many they must attend to recover. */
    private void showAdvice(int present, int absent, double percentage) {
        int held = present + absent;
        if (held == 0) {
            tvAttendanceAdvice.setText("No classes held yet");
            tvAttendanceAdvice.setTextColor(ContextCompat.getColor(this, R.color.status_neutral));
            return;
        }

        String target = (targetPercent == Math.floor(targetPercent))
                ? String.valueOf((int) targetPercent) : String.valueOf(targetPercent);
        int need = AttendanceCalculator.calculateRecoveryClasses(present, held, targetPercent);

        if (need == 0) {
            // At or above target
            int safe = AttendanceCalculator.calculateSafeLeaves(present, held, targetPercent);
            tvAttendanceAdvice.setText(safe == 0
                    ? "On target, but you can't miss any more classes"
                    : "You can skip " + safe + " more class" + (safe == 1 ? "" : "es")
                    + " and stay at or above " + target + "%");
            tvAttendanceAdvice.setTextColor(ContextCompat.getColor(this, R.color.status_green));
        } else if (need == -1) {
            tvAttendanceAdvice.setText("A " + target + "% target can't be recovered after an absence");
            tvAttendanceAdvice.setTextColor(ContextCompat.getColor(this, R.color.status_red));
        } else {
            tvAttendanceAdvice.setText("Attend the next " + need + " class" + (need == 1 ? "" : "es")
                    + " in a row to reach " + target + "%");
            int color = percentage >= targetPercent - 5 ? R.color.status_orange : R.color.status_red;
            tvAttendanceAdvice.setTextColor(ContextCompat.getColor(this, color));
        }
    }
}