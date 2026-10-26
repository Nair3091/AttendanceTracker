package com.example.attendancetracker.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.DatabaseHelper;
import com.example.attendancetracker.views.BarChartView;
import com.example.attendancetracker.views.PieChartView;
import java.util.ArrayList;
import java.util.List;

public class StatisticsFragment extends Fragment {
    private PieChartView pieChartView;
    private BarChartView barChartView;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        pieChartView = view.findViewById(R.id.pieChartView);
        barChartView = view.findViewById(R.id.barChartView);

        loadStatistics();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStatistics();
    }

    private static final String[] MONTH_NAMES =
            {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

    /** "2026-10" -> "Oct '26" */
    private String monthLabel(String ym) {
        try {
            String[] parts = ym.split("-");
            int month = Integer.parseInt(parts[1]);
            return MONTH_NAMES[month - 1] + " '" + parts[0].substring(2);
        } catch (Exception e) {
            return ym;
        }
    }

    private void loadStatistics() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Fetch Present, Absent, Cancelled counts for Pie Chart
        Cursor cursor = db.rawQuery(
                "SELECT " +
                        "SUM(CASE WHEN status = 'P' THEN 1 ELSE 0 END) AS present, " +
                        "SUM(CASE WHEN status = 'A' THEN 1 ELSE 0 END) AS absent, " +
                        "SUM(CASE WHEN status = 'C' THEN 1 ELSE 0 END) AS cancelled " +
                        "FROM attendance",
                null
        );

        int present = 0, absent = 0, cancelled = 0;
        try {
            if (cursor.moveToFirst()) {
                present = cursor.getInt(cursor.getColumnIndexOrThrow("present"));
                absent = cursor.getInt(cursor.getColumnIndexOrThrow("absent"));
                cancelled = cursor.getInt(cursor.getColumnIndexOrThrow("cancelled"));
            }
        } finally {
            cursor.close();
        }

        if (pieChartView != null) {
            pieChartView.setData(present, absent, cancelled);
        }

        // 2. Fetch Monthly Trend percentages for Bar Chart
        // Latest 4 months (year-aware), percentage = P / (P + A); cancelled classes are excluded
        Cursor barCursor = db.rawQuery(
                "SELECT ym, present_count, held_count FROM (" +
                        "SELECT strftime('%Y-%m', date) AS ym, " +
                        "SUM(CASE WHEN status = 'P' THEN 1 ELSE 0 END) AS present_count, " +
                        "SUM(CASE WHEN status IN ('P','A') THEN 1 ELSE 0 END) AS held_count " +
                        "FROM attendance WHERE date IS NOT NULL " +
                        "GROUP BY ym HAVING held_count > 0 " +
                        "ORDER BY ym DESC LIMIT 4) " +
                        "ORDER BY ym ASC",
                null
        );

        List<Float> percentages = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        try {
            while (barCursor.moveToNext()) {
                int p = barCursor.getInt(barCursor.getColumnIndexOrThrow("present_count"));
                int held = barCursor.getInt(barCursor.getColumnIndexOrThrow("held_count"));
                String ym = barCursor.getString(barCursor.getColumnIndexOrThrow("ym"));

                float pct = held > 0 ? (p / (float) held) * 100f : 0f;
                percentages.add(pct);
                labels.add(monthLabel(ym));
            }
        } finally {
            barCursor.close();
        }

        // Fallback default sample trend if no attendance logged yet
        if (percentages.isEmpty()) {
            percentages.add(0f);
            labels.add("No Data");
        }

        if (barChartView != null) {
            barChartView.setData(percentages, labels);
        }
    }
}