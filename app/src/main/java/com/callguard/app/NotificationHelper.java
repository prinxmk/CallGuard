package com.callguard.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationHelper {
    public static final String CHANNEL_ID = "callguard_alerts";
    private NotificationHelper() {}

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "CallGuard Alerts", NotificationManager.IMPORTANCE_HIGH);
                ch.setDescription("Alerts when CallGuard blocks calls or messages");
                ch.enableVibration(true);
                nm.createNotificationChannel(ch);
            }
        }
    }

    public static void showBlocked(Context context, String title, String text, int id) {
        ensureChannel(context);
        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context).setPriority(Notification.PRIORITY_HIGH);
        b.setSmallIcon(com.callguard.app.R.drawable.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new Notification.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .setCategory(Notification.CATEGORY_CALL)
                .setWhen(System.currentTimeMillis());
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(id, b.build());
    }
}
