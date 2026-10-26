package com.example.attendancetracker.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.CourseDao;

public class AddEditCourseActivity extends AppCompatActivity {
    private EditText editCourseName, editCourseCode, editTargetPercentage;
    private Button btnSaveCourse;
    private CourseDao courseDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_course);

        courseDao = new CourseDao(this);

        editCourseName = findViewById(R.id.editCourseName);
        editCourseCode = findViewById(R.id.editCourseCode);
        editTargetPercentage = findViewById(R.id.editTargetPercentage);
        btnSaveCourse = findViewById(R.id.btnSaveCourse);

        btnSaveCourse.setOnClickListener(v -> saveCourse());
    }

    private void saveCourse() {
        String name = editCourseName.getText() != null ? editCourseName.getText().toString().trim() : "";
        String code = editCourseCode.getText() != null ? editCourseCode.getText().toString().trim() : "";
        String targetStr = editTargetPercentage.getText() != null ? editTargetPercentage.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a course name", Toast.LENGTH_SHORT).show();
            return;
        }

        float targetPercentage = 75.0f; // Default target percentage
        if (!targetStr.isEmpty()) {
            try {
                targetPercentage = Float.parseFloat(targetStr);
            } catch (NumberFormatException e) {
                targetPercentage = 75.0f;
            }
        }

        // Matches CourseDao.insertCourse(String name, String code, float targetPercentage)
        long result = courseDao.insertCourse(name, code, targetPercentage);

        if (result != -1) {
            Toast.makeText(this, "Course added successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error adding course", Toast.LENGTH_SHORT).show();
        }
    }
}