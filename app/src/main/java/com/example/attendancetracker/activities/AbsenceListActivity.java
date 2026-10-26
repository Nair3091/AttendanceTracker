package com.example.attendancetracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.attendancetracker.R;
import com.example.attendancetracker.adapters.AttendanceListAdapter;
import com.example.attendancetracker.db.AttendanceDao;
import com.example.attendancetracker.models.AttendanceRecord;
import java.util.List;

/**
 * Lists every absence for one course. Tapping one opens AbsenceDetailActivity
 * with that absence's attendance_id, so details and proofs can be edited.
 */
public class AbsenceListActivity extends AppCompatActivity {
    private ListView listViewAbsences;
    private AttendanceDao attendanceDao;
    private long courseId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_list);

        attendanceDao = new AttendanceDao(this);
        courseId = getIntent().getLongExtra("course_id", -1);
        String courseName = getIntent().getStringExtra("course_name");

        TextView tvTitle = findViewById(R.id.tvAbsenceListTitle);
        tvTitle.setText(courseName != null ? "Absences - " + courseName : "Absences");

        listViewAbsences = findViewById(R.id.listViewAbsences);
        listViewAbsences.setEmptyView(findViewById(R.id.tvNoAbsences));

        // AttendanceListAdapter.getItemId() returns the attendance row id
        listViewAbsences.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(AbsenceListActivity.this, AbsenceDetailActivity.class);
            intent.putExtra("attendance_id", id);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAbsences(); // refresh after returning from the detail screen
    }

    private void loadAbsences() {
        if (courseId == -1) return;
        List<AttendanceRecord> absences = attendanceDao.getAbsencesForCourse(courseId);
        listViewAbsences.setAdapter(new AttendanceListAdapter(this, absences));
    }
}