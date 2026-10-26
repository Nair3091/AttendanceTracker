package com.example.attendancetracker.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.example.attendancetracker.R;
import com.example.attendancetracker.activities.CourseDetailActivity;
import com.example.attendancetracker.models.Course;
import com.example.attendancetracker.utils.AttendanceCalculator;
import com.example.attendancetracker.views.ProgressRingView;
import java.util.List;

public class CourseGridAdapter extends BaseAdapter {
    private Context context;
    private List<Course> courseList;

    public CourseGridAdapter(Context context, List<Course> courseList) {
        this.context = context;
        this.courseList = courseList;
    }

    @Override
    public int getCount() {
        return courseList.size();
    }

    @Override
    public Object getItem(int position) {
        return courseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return courseList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_course_card, parent, false);
        }

        Course course = courseList.get(position);

        TextView tvName = convertView.findViewById(R.id.tvCourseName);
        TextView tvCode = convertView.findViewById(R.id.tvCourseCode);
        TextView tvPercentage = convertView.findViewById(R.id.tvPercentage);
        ProgressRingView progressRing = convertView.findViewById(R.id.progressRing);

        tvName.setText(course.getName());
        tvCode.setText(course.getCode() != null ? course.getCode() : "");

        double percentage = AttendanceCalculator.calculatePercentage(course.getPresentCount(), course.getAbsentCount());
        tvPercentage.setText(String.format("%.1f%%", percentage));

        // FIXED: Explicitly cast float/double to int for the progress ring
        progressRing.setProgress((int) percentage, (int) course.getTargetPercentage());

        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, CourseDetailActivity.class);
            intent.putExtra("course_id", course.getId());
            intent.putExtra("course_name", course.getName());
            intent.putExtra("course_code", course.getCode());
            intent.putExtra("target_percent", course.getTargetPercentage());
            context.startActivity(intent);
        });

        return convertView;
    }
}