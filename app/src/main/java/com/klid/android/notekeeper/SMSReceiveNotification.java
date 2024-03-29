package com.klid.android.notekeeper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class SMSReceiveNotification {
    private static final String NOTIFICATION_TAG = "SMSReceive";

    public static void notify(final Context context,
                              final String fromNumber, final String message) {
        final Resources res = context.getResources();

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "")
            .setDefaults(Notification.DEFAULT_ALL)
            .setColor(ActivityCompat.getColor(context, R.color.app_orange))
            .setSmallIcon(R.drawable.ic_stat_settings_reminder)
            .setContentTitle(fromNumber)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(message)
                .setBigContentTitle(fromNumber)
                .setSummaryText("SMS receive"))

            .addAction(
                R.drawable.ic_action_stat_share,
                res.getString(R.string.action_share),
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent.createChooser(new Intent(Intent.ACTION_SEND)
                        .setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, "SMS from : " + fromNumber + "\n Text: " + message), "SMS sharing"),
                    PendingIntent.FLAG_UPDATE_CURRENT))
            .addAction(
                R.drawable.ic_action_stat_reply,
                res.getString(R.string.action_reply),
                null)

            // Automatically dismiss the notification when it is touched.
            .setAutoCancel(true);

        notify(context, builder.build());
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.notify(NOTIFICATION_TAG, 0, notification);
        } else {
            nm.notify(NOTIFICATION_TAG.hashCode(), notification);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
            .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            nm.cancel(NOTIFICATION_TAG, 0);
        } else {
            nm.cancel(NOTIFICATION_TAG.hashCode());
        }
    }
}
