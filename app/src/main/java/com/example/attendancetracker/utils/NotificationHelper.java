package com.example.attendancetracker.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.attendancetracker.R;
import com.example.attendancetracker.receivers.NotificationActionReceiver;

public class NotificationHelper {
    public static final String CHANNEL_MISSED_CLASS = "missed_class_channel";
    public static final String CHANNEL_REMINDERS = "reminders_channel";

    /**
     * Creates notification channels (required for API 26+)[cite: 2, 6].
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                // High importance channel for missed classes
                NotificationChannel missedChannel = new NotificationChannel(
                        CHANNEL_MISSED_CLASS,
                        "Missed Class Alerts",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(missedChannel);

                // Reminder channel for daily summaries
                NotificationChannel reminderChannel = new NotificationChannel(
                        CHANNEL_REMINDERS,
                        "Reminders & Announcements",
                        NotificationManager.IMPORTANCE_DEFAULT
                );
                manager.createNotificationChannel(reminderChannel);
            }
        }
    }

    /**
     * Posts a missed class alert with Present and Absent action buttons.
     */
    public static void showMissedClassNotification(Context context, long courseId, String courseName, String slotStart) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannels(context);

        // Action intent for Present button[cite: 5, 6]
        Intent presentIntent = new Intent(context, NotificationActionReceiver.class);
        presentIntent.setAction("ACTION_PRESENT");
        presentIntent.putExtra("course_id", courseId);
        PendingIntent pIntentPresent = PendingIntent.getBroadcast(
                context, (int) System.currentTimeMillis() + 1, presentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Action intent for Absent button[cite: 5, 6]
        Intent absentIntent = new Intent(context, NotificationActionReceiver.class);
        absentIntent.setAction("ACTION_ABSENT");
        absentIntent.putExtra("course_id", courseId);
        PendingIntent pIntentAbsent = PendingIntent.getBroadcast(
                context, (int) System.currentTimeMillis() + 2, absentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_MISSED_CLASS)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Did you attend " + courseName + "?")
                .setContentText("Lecture slot ended at " + slotStart + ". Tap to record attendance.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .addAction(0, "Present", pIntentPresent)
                .addAction(0, "Absent", pIntentAbsent);

        if (notificationManager != null) {
            notificationManager.notify((int) courseId, builder.build());
        }
    }
}