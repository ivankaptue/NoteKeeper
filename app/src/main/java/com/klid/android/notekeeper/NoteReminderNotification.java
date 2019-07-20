package com.klid.android.notekeeper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NoteReminderNotification {
    /**
     * The unique identifier for this type of notification.
     */
    private static final String NOTIFICATION_TAG = "NoteReminder";

    public static void notify(final Context context,
                              final String noteTitle, final String noteText, int noteId) {
        final Resources res = context.getResources();

        final Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.pluralsight);

        Intent noteActivityIntent = new Intent(context, NoteActivity.class);
        noteActivityIntent.putExtra(NoteActivity.NOTE_ID, noteId);

        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent backupServiceIntent = new Intent(context, NoteBackupService.class);
        backupServiceIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "")
            // set color of app name in notification
            .setColor(ContextCompat.getColor(context, R.color.app_orange))

            // Set appropriate defaults for the notification light, sound,
            // and vibration.
            .setDefaults(Notification.DEFAULT_ALL)

            // Set required fields, including the small icon, the
            // notification title, and text.
            .setSmallIcon(R.drawable.ic_stat_settings_reminder)
            .setContentTitle("Review note")
            .setContentText(noteText)

            // All fields below this line are optional.

            // Use a default priority (recognized on devices running Android
            // 4.1 or later)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Provide a large icon, shown with the notification in the
            // notification drawer on devices running Android 3.0 or later.
            .setLargeIcon(picture)

            // Set ticker text (preview) information for this notification.
            .setTicker("Review note")

            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(noteText)
                .setBigContentTitle(noteTitle)
                .setSummaryText("Review note"))

//            .setStyle(new NotificationCompat.BigPictureStyle()
//            .bigPicture(picture)
//            .setSummaryText("Review note"))

            // Show a number. This is useful when stacking notifications of
            // a single type.
//            .setNumber(number)

            // If this notification relates to a past or upcoming event, you
            // should set the relevant time information using the setWhen
            // method below. If this call is omitted, the notification's
            // timestamp will by set to the time at which it was shown.
            // TODO: Call setWhen if this notification relates to a past or
            // upcoming event. The sole argument to this method should be
            // the notification timestamp in milliseconds.
            //.setWhen(...)

            // Set the pending intent to be initiated when the user touches
            // the notification.
//            .setContentIntent(
//                PendingIntent.getActivity(
//                    context,
//                    0,
//                    new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com")),
//                    PendingIntent.FLAG_UPDATE_CURRENT))
            .setContentIntent(
                PendingIntent.getActivities(
                    context,
                    0,
                    new Intent[]{mainIntent, noteActivityIntent},
                    PendingIntent.FLAG_UPDATE_CURRENT))

            .addAction(
                0,
                "view all notes",
                PendingIntent.getActivity(
                    context,
                    0,
                    new Intent(context, MainActivity.class),
                    PendingIntent.FLAG_UPDATE_CURRENT))

            .addAction(
                0,
                "backup notes",
                PendingIntent.getService(
                    context,
                    0,
                    backupServiceIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT))

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
