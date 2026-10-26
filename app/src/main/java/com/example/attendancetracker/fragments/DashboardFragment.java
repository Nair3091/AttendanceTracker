package com.example.attendancetracker.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.attendancetracker.R;
import com.example.attendancetracker.activities.AddEditCourseActivity;
import com.example.attendancetracker.adapters.CourseGridAdapter;
import com.example.attendancetracker.db.CourseDao;
import com.example.attendancetracker.models.Course;
import java.util.List;

public class DashboardFragment extends Fragment {
    private GridView courseGridView;
    private FloatingActionButton fabAddCourse;
    private CourseDao courseDao;
    private CourseGridAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        courseDao = new CourseDao(requireContext());
        courseGridView = view.findViewById(R.id.courseGrid);
        fabAddCourse = view.findViewById(R.id.fabAddCourse);

        fabAddCourse.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditCourseActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCourses();
    }

    private void loadCourses() {
        List<Course> courseList = courseDao.getAllCourses();
        adapter = new CourseGridAdapter(requireContext(), courseList);
        courseGridView.setAdapter(adapter);
    }
}