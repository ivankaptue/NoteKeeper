package com.klid.android.notekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;

public class NoteReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_NOTE_TITLE = "com.klid.android.notekeeper.extras.NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.klid.android.notekeeper.extras.NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "com.klid.android.notekeeper.extras.NOTE_ID";
    public static final String EXTRA_NOTE_REMINDER_DATE = "com.klid.android.notekeeper.extras.NOTE_REMINDER_DATE";

    @Override
    public void onReceive(Context context, Intent intent) {
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        int noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0);
        long reminderDate = intent.getLongExtra(EXTRA_NOTE_REMINDER_DATE, 0);

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        NoteReminderNotification.notify(context, noteTitle, noteText, noteId, reminderDate);
        if (vibrator.hasVibrator()) {
            long[] mVibratePattern = new long[]{0, 400, 200, 400};
            vibrator.vibrate(mVibratePattern, -1);
        }
    }
}
