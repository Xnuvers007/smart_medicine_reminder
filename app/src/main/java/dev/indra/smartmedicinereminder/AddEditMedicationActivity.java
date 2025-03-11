package dev.indra.smartmedicinereminder;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

public class AddEditMedicationActivity extends AppCompatActivity {

    private EditText etName, etDosage, etNotes;
    private TimePicker timePicker;
    private Button btnSave;

    private DatabaseHelper db;
    private long medicationId = -1;
    private Medication medication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_medication);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etDosage = findViewById(R.id.etDosage);
        etNotes = findViewById(R.id.etNotes);
        timePicker = findViewById(R.id.timePicker);
        btnSave = findViewById(R.id.btnSave);

        timePicker.setIs24HourView(true);

        medicationId = getIntent().getLongExtra("medication_id", -1);

        if (medicationId != -1) {
            setTitle("Edit Medication");
            medication = db.getMedication(medicationId);
            populateFields();
        } else {
            setTitle("Add Medication");
            medication = new Medication();
        }

        btnSave.setOnClickListener(v -> saveMedication());
    }

    private void populateFields() {
        etName.setText(medication.getName());
        etDosage.setText(medication.getDosage());
        etNotes.setText(medication.getNotes());
        timePicker.setHour(medication.getHour());
        timePicker.setMinute(medication.getMinute());
    }

    private void saveMedication() {
        String name = etName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        if (name.isEmpty()) {
            etName.setError("Please enter medication name");
            etName.requestFocus();
            return;
        }

        medication.setName(name);
        medication.setDosage(dosage);
        medication.setNotes(notes);
        medication.setHour(hour);
        medication.setMinute(minute);

        if (medicationId == -1) {
            medication.setActive(true);
            long id = db.addMedication(medication);
            medication.setId(id);
            AlarmHelper.setAlarm(this, medication);
            Toast.makeText(this, "Medication added successfully", Toast.LENGTH_SHORT).show();
        } else {
            db.updateMedication(medication);
            if (medication.isActive()) {
                AlarmHelper.updateAlarm(this, medication);
            }
            Toast.makeText(this, "Medication updated successfully", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}