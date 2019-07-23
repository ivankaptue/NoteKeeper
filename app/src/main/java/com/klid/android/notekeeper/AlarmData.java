package com.klid.android.notekeeper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

public class AlarmData {

    public static final String ACTION_ALARM_INTENT = "com.klid.android.notekeeper.ACTION_ALARM_INTENT#";
    private final Context mContext;

    public AlarmData(Context cxt) {
        mContext = cxt;
    }

    public void setAlarm(NoteInfo note) {
        Calendar calendar = Calendar.getInstance();
        Calendar noteReminderDate = note.getReminderDate();

        if (note.isReminderEnabled() && noteReminderDate != null && noteReminderDate.getTimeInMillis() >= calendar.getTimeInMillis()) {
            PendingIntent pendingIntent = getAlarmIntent(note.getId(), note.getTitle(), note.getText(), noteReminderDate.getTimeInMillis());
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, noteReminderDate.getTimeInMillis(), pendingIntent);
        }
    }

    private PendingIntent getAlarmIntent(int noteId, String noteTitle, String noteText, long noteReminderDate) {
        Intent intent = new Intent(mContext, NoteReminderReceiver.class);
        intent.setAction(ACTION_ALARM_INTENT + noteId);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId);
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_REMINDER_DATE, noteReminderDate);

        return PendingIntent.getBroadcast(mContext, noteId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void cancelReminder(int noteId) {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = getAlarmIntent(noteId, null, null, 0);
        alarmManager.cancel(pendingIntent);
    }

}
