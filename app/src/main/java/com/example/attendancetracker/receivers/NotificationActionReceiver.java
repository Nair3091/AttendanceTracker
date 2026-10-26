package com.example.attendancetracker.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.attendancetracker.db.AttendanceDao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long courseId = intent.getLongExtra("course_id", -1);

        if (courseId == -1) return;

        AttendanceDao attendanceDao = new AttendanceDao(context);
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        String status = "P";
        if ("ACTION_ABSENT".equals(action)) {
            status = "A";
        }

        // Write record to database
        attendanceDao.upsertAttendance(courseId, currentDate, "09:00", status, null, null, "None");

        // Dismiss notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancel((int) courseId);
        }

        Toast.makeText(context, "Marked as " + (status.equals("P") ? "Present" : "Absent"), Toast.LENGTH_SHORT).show();
    }
}