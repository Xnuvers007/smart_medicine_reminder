package dev.indra.smartmedicinereminder;

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
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.VibrationEffect;

import androidx.core.app.NotificationCompat;

import dev.indra.smartmedicinereminder.db.DatabaseHelper;
import dev.indra.smartmedicinereminder.model.Medication;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "medicine_reminder_channel";
    private static final String CHANNEL_NAME = "Medicine Reminder";
    private static MediaPlayer mediaPlayer;
    private static Handler handler;
    private static Runnable repeatingRunnable;
    private static PowerManager.WakeLock wakeLock;
    private static Vibrator vibrator;
    private static final long[] VIBRATION_PATTERN = {0, 500, 1000};

    @Override
    public void onReceive(Context context, Intent intent) {
        long medicationId = intent.getLongExtra("medication_id", -1);
        String medicationName = intent.getStringExtra("medication_name");
        String medicationDosage = intent.getStringExtra("medication_dosage");
        if (medicationId == -1 || medicationName == null) {
            return;
        }

        // Acquire wake lock to keep device awake during alert
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "smartmedicinereminder:wakelock");
        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        DatabaseHelper db = new DatabaseHelper(context);
        Medication medication = db.getMedication(medicationId);

        if (medication.isActive()) {
            // Reset alarm for next day
            AlarmHelper.setAlarm(context, medication);

            // Show notification and start persistent alerts
            showPersistentNotification(context, medicationName, medicationDosage, medicationId);
        }
    }

    private void showPersistentNotification(Context context, String medicationName, String medicationDosage, long medicationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create high priority notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            // Configure channel for continuous alerts
            channel.setSound(null, null);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(VIBRATION_PATTERN);
            channel.setBypassDnd(true);
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            notificationManager.createNotificationChannel(channel);
        }

        // Create intent for when notification is tapped
        Intent mainIntent = new Intent(context, MainActivity.class);
        PendingIntent mainPendingIntent = PendingIntent.getActivity(
                context, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Create dismiss action intent
        Intent dismissIntent = new Intent(context, NotificationDismissReceiver.class);
        dismissIntent.putExtra("notification_id", (int) medicationId);
        dismissIntent.setAction("DISMISS_MEDICATION_" + medicationId);
        PendingIntent dismissPendingIntent = PendingIntent.getBroadcast(
                context, (int) medicationId, dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
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
                .setOngoing(true)  // Makes the notification persistent
                .setSound(null)  // No sound in notification itself as we handle it separately
                .setVibrate(VIBRATION_PATTERN)
                .setContentIntent(mainPendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Sudah Minum", dismissPendingIntent);

        int notificationId = (int) medicationId;
        notificationManager.notify(notificationId, builder.build());

        // Start continuous sound and vibration
        startPersistentAlerts(context, notificationId);
    }

    private void startPersistentAlerts(Context context, int notificationId) {
        // Setup continuous vibration
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
            } else {
                vibrator.vibrate(VIBRATION_PATTERN, 0); // '0' means repeat indefinitely
            }
        }

        // Setup continuous sound
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build();
                mediaPlayer.setAudioAttributes(audioAttributes);
            } else {
                mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_ALARM);
            }
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup a repeating task to ensure the alarm stays active
        handler = new Handler();
        repeatingRunnable = new Runnable() {
            @Override
            public void run() {
                // Restart sound if it stopped for some reason
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                // Restart vibration if needed
                if (vibrator != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(VIBRATION_PATTERN, 0));
                    } else {
                        vibrator.vibrate(VIBRATION_PATTERN, 0);
                    }
                }

                // Schedule this task to run again in 5 seconds
                handler.postDelayed(this, 5000);
            }
        };

        // Start the repeating task
        handler.postDelayed(repeatingRunnable, 5000);
    }

    // Method to stop all alerts
    public static void stopAlerts(Context context, int notificationId) {
        // Stop the repeating task
        if (handler != null && repeatingRunnable != null) {
            handler.removeCallbacks(repeatingRunnable);
        }

        // Stop and release MediaPlayer
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        // Stop vibration
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }

        // Release wake lock
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }

        // Cancel the notification
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(notificationId);
    }
}