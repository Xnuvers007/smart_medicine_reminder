package dev.indra.smartmedicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import dev.indra.smartmedicinereminder.adapter.MedicationAdapter;
import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

public class MainActivity extends AppCompatActivity implements MedicationAdapter.OnMedicationListener {

    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;
    private DatabaseHelper db;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditMedicationActivity.class);
            startActivity(intent);
        });

        loadMedications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }

    private void loadMedications() {
        medicationList = db.getAllMedications();

        if (medicationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);

            adapter = new MedicationAdapter(this, medicationList, this);
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public void onMedicationClick(int position) {
        Medication medication = medicationList.get(position);
        Intent intent = new Intent(MainActivity.this, AddEditMedicationActivity.class);
        intent.putExtra("medication_id", medication.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(int position) {
        Medication medication = medicationList.get(position);
        AlarmHelper.cancelAlarm(this, (int) medication.getId());
        db.deleteMedication(medication.getId());
        medicationList.remove(position);
        adapter.notifyItemRemoved(position);

        if (medicationList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onSwitchChanged(int position, boolean isChecked) {
        Medication medication = medicationList.get(position);
        medication.setActive(isChecked);
        db.updateMedication(medication);

        if (isChecked) {
            AlarmHelper.setAlarm(this, medication);
        } else {
            AlarmHelper.cancelAlarm(this, (int) medication.getId());
        }
    }
}