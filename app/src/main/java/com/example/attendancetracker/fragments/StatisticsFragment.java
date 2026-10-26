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
        if (cursor != null && cursor.moveToFirst()) {
            present = cursor.getInt(cursor.getColumnIndexOrThrow("present"));
            absent = cursor.getInt(cursor.getColumnIndexOrThrow("absent"));
            cancelled = cursor.getInt(cursor.getColumnIndexOrThrow("cancelled"));
            cursor.close();
        }

        if (pieChartView != null) {
            pieChartView.setData(present, absent, cancelled);
        }

        // 2. Fetch Monthly Trend percentages for Bar Chart
        Cursor barCursor = db.rawQuery(
                "SELECT strftime('%m', date) AS month, " +
                        "SUM(CASE WHEN status = 'P' THEN 1 ELSE 0 END) AS present_count, " +
                        "COUNT(*) AS total_count " +
                        "FROM attendance GROUP BY month ORDER BY month ASC LIMIT 4",
                null
        );

        List<Float> percentages = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        if (barCursor != null && barCursor.moveToFirst()) {
            do {
                int p = barCursor.getInt(barCursor.getColumnIndexOrThrow("present_count"));
                int t = barCursor.getInt(barCursor.getColumnIndexOrThrow("total_count"));
                String m = barCursor.getString(barCursor.getColumnIndexOrThrow("month"));

                float pct = t > 0 ? (p / (float) t) * 100f : 0f;
                percentages.add(pct);
                labels.add("M" + m);
            } while (barCursor.moveToNext());
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