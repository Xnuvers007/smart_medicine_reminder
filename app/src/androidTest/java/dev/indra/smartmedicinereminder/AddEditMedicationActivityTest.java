package dev.indra.smartmedicinereminder;

import android.content.Intent;
import android.widget.EditText;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class AddEditMedicationActivityTest {

    @Test
    public void saveMedication_addsNewMedication() {
        try (ActivityScenario<AddEditMedicationActivity> scenario = ActivityScenario.launch(AddEditMedicationActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etName);
                EditText etDosage = activity.findViewById(R.id.etDosage);
                EditText etNotes = activity.findViewById(R.id.etNotes);
                TimePicker timePicker = activity.findViewById(R.id.timePicker);

                etName.setText("Test Medication");
                etDosage.setText("10mg");
                etNotes.setText("Take with water");
                timePicker.setHour(8);
                timePicker.setMinute(30);

                activity.findViewById(R.id.btnSave).performClick();

                try (DatabaseHelper db = new DatabaseHelper(activity)) { // Menggunakan try-with-resources
                    Medication medication = db.getMedication(1); // Mengambil ID pertama setelah disimpan

                    assertNotNull(medication);
                    assertEquals("Test Medication", medication.getName());
                    assertEquals("10mg", medication.getDosage());
                    assertEquals("Take with water", medication.getNotes());
                    assertEquals(8, medication.getHour());
                    assertEquals(30, medication.getMinute());
                }
            });
        }
    }

    @Test
    public void saveMedication_updatesExistingMedication() {
        try (DatabaseHelper db = new DatabaseHelper(ApplicationProvider.getApplicationContext())) { // Menggunakan try-with-resources
            Medication medication = new Medication("Existing Medication", "5mg", 9, 0);
            long id = db.addMedication(medication);

            Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AddEditMedicationActivity.class);
            intent.putExtra("medication_id", id);

            try (ActivityScenario<AddEditMedicationActivity> scenario = ActivityScenario.launch(intent)) {
                scenario.onActivity(activity -> {
                    EditText etName = activity.findViewById(R.id.etName);
                    EditText etDosage = activity.findViewById(R.id.etDosage);
                    EditText etNotes = activity.findViewById(R.id.etNotes);
                    TimePicker timePicker = activity.findViewById(R.id.timePicker);

                    etName.setText("Updated Medication");
                    etDosage.setText("15mg");
                    etNotes.setText("Take after meal");
                    timePicker.setHour(10);
                    timePicker.setMinute(45);

                    activity.findViewById(R.id.btnSave).performClick();

                    Medication updatedMedication = db.getMedication(id);

                    assertNotNull(updatedMedication);
                    assertEquals("Updated Medication", updatedMedication.getName());
                    assertEquals("15mg", updatedMedication.getDosage());
                    assertEquals("Take after meal", updatedMedication.getNotes());
                    assertEquals(10, updatedMedication.getHour());
                    assertEquals(45, updatedMedication.getMinute());
                });
            }
        }
    }

    @Test
    public void saveMedication_showsErrorWhenNameIsEmpty() {
        try (ActivityScenario<AddEditMedicationActivity> scenario = ActivityScenario.launch(AddEditMedicationActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etName);
                EditText etDosage = activity.findViewById(R.id.etDosage);
                EditText etNotes = activity.findViewById(R.id.etNotes);
                TimePicker timePicker = activity.findViewById(R.id.timePicker);

                etName.setText("");
                etDosage.setText("10mg");
                etNotes.setText("Take with water");
                timePicker.setHour(8);
                timePicker.setMinute(30);

                activity.findViewById(R.id.btnSave).performClick();

                assertEquals("Please enter medication name", etName.getError());
            });
        }
    }
}
