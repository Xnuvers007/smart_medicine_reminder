package dev.indra.smartmedicinereminder;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.indra.smartmedicinereminder.adapters.MedicationAdapter;
import dev.indra.smartmedicinereminder.models.Medication;
import dev.indra.smartmedicinereminder.utils.AlarmHelper;
import dev.indra.smartmedicinereminder.database.DatabaseHelper;

public class MainActivity extends AppCompatActivity implements MedicationAdapter.OnMedicationListener {

    private static final int TIME_INTERVAL = 2000;
    private RecyclerView recyclerView;
    private MedicationAdapter adapter;
    private List<Medication> medicationList;
    private List<Medication> originalMedicationList;
    private DatabaseHelper db;
    private TextView tvEmpty;
    private long backPressedTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (backPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
                    finish(); // Menutup activity jika kembali ditekan
                } else {
                    //Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
                    Snackbar.make(findViewById(android.R.id.content), "Press back again to exit", Snackbar.LENGTH_SHORT).show();
                }
                backPressedTime = System.currentTimeMillis();
            }
        });


        db = new DatabaseHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        EditText etSearch = findViewById(R.id.etSearch);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditMedicationActivity.class);
            startActivity(intent);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMedications(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        loadMedications();
    }

    private void filterMedications(String query) {
        if (originalMedicationList == null || originalMedicationList.isEmpty()) {
            return;
        }

        List<Medication> filteredList = originalMedicationList.stream()
                .filter(medication -> medication.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        adapter.updateList(filteredList);

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMedications();
    }

    private void loadMedications() {
        medicationList = db.getAllMedications();
        originalMedicationList = new ArrayList<>(medicationList);

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
        originalMedicationList.remove(medication);
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

    //    @Override
    //    public void onBackPressed() {
    //        if (backPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
    //            super.onBackPressed();
    //            return;
    //        } else {
    //            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
    //        }
    //
    //        backPressedTime = System.currentTimeMillis();
    //    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        db.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    @Override
    protected void onPause() {
        super.onPause();
        db.close();
    }

    @Override
    protected void onStart() {
        super.onStart();
        db = new DatabaseHelper(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        db = new DatabaseHelper(this);
    }

}