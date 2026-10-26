package com.example.attendancetracker.fragments;

import android.app.AlertDialog;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.DatabaseHelper;
import java.io.File;

public class SettingsFragment extends Fragment {
    private Button btnClearAttendance, btnResetDatabase;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        dbHelper = new DatabaseHelper(requireContext());

        btnClearAttendance = view.findViewById(R.id.btnClearAttendance);
        btnResetDatabase = view.findViewById(R.id.btnResetDatabase);

        btnClearAttendance.setOnClickListener(v -> showClearAttendanceDialog());
        btnResetDatabase.setOnClickListener(v -> showResetDatabaseDialog());

        return view;
    }

    private void showClearAttendanceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear Attendance Records")
                .setMessage("Are you sure you want to delete all logged attendance history? Your courses and timetable will remain intact.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.execSQL("DELETE FROM proofs");
                    db.execSQL("DELETE FROM attendance");
                    deleteProofFiles();
                    Toast.makeText(requireContext(), "All attendance records cleared.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /** Removes the copied proof files from app storage (their DB rows are removed separately). */
    private void deleteProofFiles() {
        File dir = new File(requireContext().getFilesDir(), "proofs");
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                f.delete();
            }
        }
    }

    private void showResetDatabaseDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Reset Entire Database")
                .setMessage("WARNING: This will permanently delete ALL courses, timetable slots, and attendance logs!")
                .setPositiveButton("Reset Everything", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    dbHelper.resetAllTables(db);
                    deleteProofFiles();
                    Toast.makeText(requireContext(), "Database has been completely reset.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}