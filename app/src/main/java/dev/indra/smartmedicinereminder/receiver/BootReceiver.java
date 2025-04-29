package dev.indra.smartmedicinereminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            List<Medication> medicationList;
            try (DatabaseHelper db = new DatabaseHelper(context)) {
                medicationList = db.getAllMedications();
            }

            for (Medication medication : medicationList) {
                if (medication.isActive()) {
                    AlarmHelper.setAlarm(context, medication);
                }
            }
        }
    }
}