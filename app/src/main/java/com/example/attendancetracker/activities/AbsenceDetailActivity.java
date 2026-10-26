package com.example.attendancetracker.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.example.attendancetracker.R;
import com.example.attendancetracker.db.ProofDao;

public class AbsenceDetailActivity extends AppCompatActivity {
    private Spinner spinnerCategory, spinnerLeaveStatus;
    private TextInputEditText editReasonText;
    private Button btnAddProof, btnSaveAbsence;
    private ProofDao proofDao;
    private static final int PICK_FILE_REQUEST_CODE = 101;
    private Uri attachedFileUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absence_detail);

        proofDao = new ProofDao(this);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerLeaveStatus = findViewById(R.id.spinnerLeaveStatus);
        editReasonText = findViewById(R.id.editReasonText);
        btnAddProof = findViewById(R.id.btnAddProof);
        btnSaveAbsence = findViewById(R.id.btnSaveAbsence);

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
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Select Proof File"), PICK_FILE_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                attachedFileUri = data.getData();
                Toast.makeText(this, "Proof file attached successfully", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveAbsenceDetails() {
        String category = spinnerCategory.getSelectedItem().toString();
        String leaveStatus = spinnerLeaveStatus.getSelectedItem().toString();
        String reason = editReasonText.getText() != null ? editReasonText.getText().toString().trim() : "";

        Toast.makeText(this, "Absence details saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}