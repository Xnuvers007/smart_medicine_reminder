package dev.indra.smartmedicinereminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import android.os.Vibrator;
import android.os.VibrationEffect;

import androidx.core.app.NotificationCompat;

import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicationId = intent.getLongExtra("medication_id", -1);
        String medicationName = intent.getStringExtra("medication_name");
        String medicationDosage = intent.getStringExtra("medication_dosage");
        if (medicationId == -1 || medicationName == null) {
            return;
        }

        AlarmHelper.cancelAlarm(context, (int) medicationId);

        DatabaseHelper db = new DatabaseHelper(context);
        Medication medication = db.getMedication(medicationId);
        if (medication.isActive()) {
            AlarmHelper.setAlarm(context, medication);
            showNotification(context, medicationName, medicationDosage);
        }
    }

    private void showNotification(Context context, String medicationName, String medicationDosage) {
        String channelId = "medicine_reminder_channel";
        String channelName = "Medicine Reminder";

        String title = "Waktunya Minum Obat!";
        String message = medicationName;
        if (medicationDosage != null && !medicationDosage.isEmpty()) {
            message += " - " + medicationDosage;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(500);
            }
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(alarmSound)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}