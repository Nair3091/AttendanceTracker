package com.example.attendancetracker.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.google.android.material.textfield.TextInputEditText;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.AttendanceDao;
import com.example.attendancetracker.db.ProofDao;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AbsenceDetailActivity extends AppCompatActivity {
    private Spinner spinnerCategory, spinnerLeaveStatus;
    private TextInputEditText editReasonText;
    private Button btnAddProof, btnSaveAbsence;
    private LinearLayout proofContainer;
    private ProofDao proofDao;
    private AttendanceDao attendanceDao;
    // Activity Result API (replaces startActivityForResult/onActivityResult)
    private final ActivityResultLauncher<String> pickFileLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) saveProofFile(uri);
            });
    // 14: file copying runs here, not on the UI thread
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private long attendanceId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_detail);

        proofDao = new ProofDao(this);
        attendanceDao = new AttendanceDao(this);

        // Set by MarkAttendanceActivity when an absence is recorded
        attendanceId = getIntent().getLongExtra("attendance_id", -1);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLeaveStatus = findViewById(R.id.spinnerLeaveStatus);
        editReasonText = findViewById(R.id.editReasonText);
        btnAddProof = findViewById(R.id.btnAddProof);
        btnSaveAbsence = findViewById(R.id.btnSaveAbsence);
        proofContainer = findViewById(R.id.proofContainer);

        // Setup Spinners
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.absence_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> leaveAdapter = ArrayAdapter.createFromResource(this,
                R.array.leave_statuses, android.R.layout.simple_spinner_item);
        leaveAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeaveStatus.setAdapter(leaveAdapter);

        btnAddProof.setOnClickListener(v -> openFilePicker());
        btnSaveAbsence.setOnClickListener(v -> saveAbsenceDetails());

        loadAbsenceDetails();
        loadProofs();
    }

    private void openFilePicker() {
        if (attendanceId == -1) {
            Toast.makeText(this, "Open this screen from an absence entry to attach proof", Toast.LENGTH_LONG).show();
            return;
        }
        pickFileLauncher.launch("*/*");
    }

    /**
     * The picked Uri is only temporary, so copy the file into the app's private
     * storage (filesDir/proofs/) and record it in the proofs table.
     * The copy runs on a background thread so large files can't freeze the screen.
     */
    private void saveProofFile(Uri uri) {
        btnAddProof.setEnabled(false);
        Toast.makeText(this, "Attaching file...", Toast.LENGTH_SHORT).show();

        ioExecutor.execute(() -> {
            String error = copyAndRecordProof(uri); // null on success
            runOnUiThread(() -> {
                if (isDestroyed()) return; // screen was closed or rotated meanwhile
                btnAddProof.setEnabled(true);
                if (error == null) {
                    Toast.makeText(this, "Proof file attached successfully", Toast.LENGTH_SHORT).show();
                    loadProofs();
                } else {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    /** Runs on a background thread. Returns null on success, otherwise an error message. */
    private String copyAndRecordProof(Uri uri) {
        String displayName = queryDisplayName(uri);
        String mime = getContentResolver().getType(uri);
        if (mime == null) mime = "*/*";

        File dir = new File(getFilesDir(), "proofs");
        if (!dir.exists() && !dir.mkdirs()) {
            return "Could not create proof folder";
        }

        String storedName = System.currentTimeMillis() + "_" + displayName.replaceAll("[^A-Za-z0-9._-]", "_");
        File dest = new File(dir, storedName);

        try (InputStream in = getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) throw new IOException("Cannot open selected file");
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
        } catch (IOException e) {
            dest.delete();
            return "Failed to copy file: " + e.getMessage();
        }

        long id = proofDao.insertProof(attendanceId, storedName, mime, displayName);
        if (id == -1) {
            dest.delete();
            return "Failed to save proof";
        }
        return null;
    }

    private String queryDisplayName(Uri uri) {
        String name = null;
        try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) name = c.getString(idx);
            }
        }
        return (name == null || name.isEmpty()) ? "proof" : name;
    }

    /** Rebuilds the list of attached proofs; tapping one opens it. */
    private void loadProofs() {
        proofContainer.removeAllViews();
        int pad = (int) (12 * getResources().getDisplayMetrics().density);
        int count = 0;

        if (attendanceId != -1) {
            Cursor c = proofDao.getProofsForAttendance(attendanceId);
            try {
                while (c.moveToNext()) {
                    long proofId = c.getLong(c.getColumnIndexOrThrow("id"));
                    String storedName = c.getString(c.getColumnIndexOrThrow("file_path"));
                    String mime = c.getString(c.getColumnIndexOrThrow("file_type"));
                    String displayName = c.getString(c.getColumnIndexOrThrow("display_name"));

                    TextView tv = new TextView(this);
                    tv.setText(displayName + "  (tap to view, hold to delete)");
                    tv.setTextSize(16f);
                    tv.setPadding(0, pad, 0, pad);
                    tv.setOnClickListener(v -> openProof(storedName, mime));
                    tv.setOnLongClickListener(v -> {
                        confirmDeleteProof(proofId, storedName, displayName);
                        return true;
                    });
                    proofContainer.addView(tv);
                    count++;
                }
            } finally {
                c.close();
            }
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("No proof attached yet");
            empty.setPadding(0, pad, 0, pad);
            proofContainer.addView(empty);
        }
    }

    private void confirmDeleteProof(long proofId, String storedName, String displayName) {
        new AlertDialog.Builder(this)
                .setTitle("Delete proof")
                .setMessage("Delete \"" + displayName + "\"? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    new File(new File(getFilesDir(), "proofs"), storedName).delete();
                    proofDao.deleteProof(proofId);
                    loadProofs();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ioExecutor.shutdown(); // lets an in-flight copy finish, accepts no new work
    }

    private void openProof(String storedName, String mime) {
        File file = new File(new File(getFilesDir(), "proofs"), storedName);
        if (!file.exists()) {
            Toast.makeText(this, "File is missing from storage", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime != null ? mime : "*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app installed that can open this file type", Toast.LENGTH_LONG).show();
        }
    }

    /** Pre-fills the form with whatever was saved before for this absence. */
    private void loadAbsenceDetails() {
        if (attendanceId == -1) return;
        String[] d = attendanceDao.getAbsenceDetails(attendanceId);
        if (d == null) return;
        selectSpinnerValue(spinnerCategory, d[0]);
        selectSpinnerValue(spinnerLeaveStatus, d[2]);
        if (d[1] != null) editReasonText.setText(d[1]);
    }

    private void selectSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < spinner.getCount(); i++) {
            if (value.equals(spinner.getItemAtPosition(i).toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private void saveAbsenceDetails() {
        if (attendanceId == -1) {
            Toast.makeText(this, "Open this screen from an absence entry to save details", Toast.LENGTH_LONG).show();
            return;
        }
        String category = spinnerCategory.getSelectedItem().toString();
        String leaveStatus = spinnerLeaveStatus.getSelectedItem().toString();
        String reason = editReasonText.getText() != null ? editReasonText.getText().toString().trim() : "";

        int rows = attendanceDao.updateAbsenceDetails(attendanceId, category, reason, leaveStatus);
        if (rows > 0) {
            Toast.makeText(this, "Absence details saved", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Could not save: attendance record not found", Toast.LENGTH_SHORT).show();
        }
    }
}