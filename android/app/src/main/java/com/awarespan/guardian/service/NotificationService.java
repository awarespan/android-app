package com.awarespan.guardian.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.awarespan.guardian.R;
import com.awarespan.guardian.ui.MainActivity;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages AWARESPAN system and incident notifications.
 * All notifications are user-dismissible events — no permanent foreground notifications.
 */
public final class NotificationService {

    public static final String CHANNEL_INCIDENTS = "awarespan_incidents";
    public static final String CHANNEL_ALERTS = "awarespan_system_alerts";
    public static final String EXTRA_INCIDENT_ID = "com.awarespan.guardian.EXTRA_INCIDENT_ID";
    public static final String PREFS_NAME = "awarespan_settings";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";

    private static final AtomicInteger NEXT_ID = new AtomicInteger(100);

    private NotificationService() {}

    public static boolean isNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_NOTIFICATIONS_ENABLED, true);
    }

    public static void setNotificationsEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_NOTIFICATIONS_ENABLED, enabled).apply();
    }

    public static void notifyNewIncident(Context context, String incidentId,
                                         String threatType, String description) {
        if (!isNotificationsEnabled(context)) return;
        ensureChannels(context);

        Intent open = new Intent(context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(EXTRA_INCIDENT_ID, incidentId);
        PendingIntent pi = PendingIntent.getActivity(
                context,
                incidentId == null ? NEXT_ID.get() : incidentId.hashCode(),
                open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String title = "AWARESPAN: Incident Detected";
        String content = (threatType == null ? "UNKNOWN THREAT" : threatType) +
                (description != null && !description.isBlank() ? " — " + description : "");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_INCIDENTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        notify(context, NEXT_ID.getAndIncrement(), builder);
    }

    public static void notifySystemAlert(Context context, String level, String message) {
        if (!isNotificationsEnabled(context)) return;
        ensureChannels(context);

        String title = "AWARESPAN: " + (level == null ? "System Alert" : level.toUpperCase());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ALERTS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message == null ? "" : message)
                .setAutoCancel(true)
                .setOngoing(false)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notify(context, NEXT_ID.getAndIncrement(), builder);
    }

    private static void notify(Context context, int id, NotificationCompat.Builder builder) {
        NotificationManager nm = manager(context);
        if (nm != null) {
            nm.notify(id, builder.build());
        }
    }

    private static NotificationManager manager(Context context) {
        return context.getSystemService(NotificationManager.class);
    }

    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = manager(context);
        if (nm == null) return;

        // Clean up legacy channel names from prior versions
        try {
            nm.deleteNotificationChannel("awarespan_events");
            nm.deleteNotificationChannel("sip_events");
            nm.deleteNotificationChannel("sip_incidents");
            nm.deleteNotificationChannel("awarespan_alerts");
        } catch (Exception ignored) {}

        NotificationChannel incidentChannel = new NotificationChannel(
                CHANNEL_INCIDENTS,
                "AWARESPAN Incidents",
                NotificationManager.IMPORTANCE_HIGH
        );
        incidentChannel.setDescription("Real-time notifications for detected threat incidents");
        incidentChannel.enableLights(true);
        incidentChannel.enableVibration(true);

        NotificationChannel alertChannel = new NotificationChannel(
                CHANNEL_ALERTS,
                "AWARESPAN System Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        alertChannel.setDescription("Operational alerts and connectivity notices");

        nm.createNotificationChannel(incidentChannel);
        nm.createNotificationChannel(alertChannel);
    }
}
