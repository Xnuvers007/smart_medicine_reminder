package dev.indra.smartmedicinereminder.receiver;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import dev.indra.smartmedicinereminder.MainActivity;
import dev.indra.smartmedicinereminder.models.Medication;
import dev.indra.smartmedicinereminder.R;
import dev.indra.smartmedicinereminder.utils.AlarmHelper;
import dev.indra.smartmedicinereminder.database.DatabaseHelper;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final String CHANNEL_NAME = "Medicine Reminder";
    private static final long[] VIBRATION_PATTERN = {0, 500, 1000};
    private static MediaPlayer mediaPlayer;
    private static Handler handler;
    private static Runnable repeatingRunnable;
    private static PowerManager.WakeLock wakeLock;
    private static Vibrator vibrator;

    public static void stopAlerts(Context context, int notificationId) {
        if (handler != null && repeatingRunnable != null) {
            handler.removeCallbacks(repeatingRunnable);
        }

        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }

    @SuppressLint("Wakelock")
    @Override
    public void onReceive(Context context, Intent intent) {
        long medicationId = intent.getLongExtra("medication_id", -1);
        String medicationName = intent.getStringExtra("medication_name");
        String medicationDosage = intent.getStringExtra("medication_dosage");
        if (medicationId == -1 || medicationName == null) {
            return;
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "smartmedicinereminder:wakelock"
        );
        wakeLock.acquire(10 * 60 * 1000L); // 10 minutes

        Medication medication;
        try (DatabaseHelper db = new DatabaseHelper(context)) {
            medication = db.getMedication(medicationId);
        }

        if (medication != null && medication.isActive()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, medication.getHour());
            calendar.set(Calendar.MINUTE, medication.getMinute());
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            AlarmHelper.setAlarm(context, medication);

            showPersistentNotification(context, medicationName, medicationDosage, medicationId);
        }
    }

    private void showPersistentNotification(Context context, String medicationName, String medicationDosage, long medicationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

        channel.setSound(null, null);
        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setVibrationPattern(VIBRATION_PATTERN);
        channel.setBypassDnd(true);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        notificationManager.createNotificationChannel(channel);

        Intent mainIntent = new Intent(context, MainActivity.class);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        flags |= PendingIntent.FLAG_IMMUTABLE;

        // PendingIntent mainPendingIntent = PendingIntent.getActivity(
        // context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, flags);

        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        dismissIntent.putExtra("notification_id", (int) medicationId);
        dismissIntent.setAction("DISMISS_MEDICATION_" + medicationId);

        // PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
        // context, (int) medicationId, dismissIntent,
        // PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context, (int) medicationId, dismissIntent, flags);

        String title = "Waktunya Minum Obat!";
        String message = medicationName;
        if (medicationDosage != null && !medicationDosage.isEmpty()) {
            message += " - " + medicationDosage;
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_medication)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .setSound(null)
                .setVibrate(VIBRATION_PATTERN)
                .setContentIntent(mainPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Sudah Minum", dismissPendingIntent);

        int notificationId = (int) medicationId;
        notificationManager.notify(notificationId, builder.build());

        startPersistentAlerts(context, notificationId);
    }

    private void startPersistentAlerts(Context context, int notificationId) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
        }

        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmSound == null) {
                alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }

            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(context, alarmSound);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mediaPlayer.setAudioAttributes(audioAttributes);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to play alarm sound");
        }

        handler = new Handler();
        repeatingRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Failed to play alarm sound");
                    }
                }

                if (vibrator != null) {
                    vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
                }

                handler.postDelayed(this, 5000);
            }
        };

        handler.postDelayed(repeatingRunnable, 5000);
    }
}