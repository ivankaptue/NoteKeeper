package com.klid.android.notekeeper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RestoreOnBootReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if ("android.intent.action.BOOT_COMPLETED".equals(action)) {
            List<NoteInfo> notes = getReminderNotes(context);
            AlarmData alarmData = new AlarmData(context);
            Log.d(TAG, "boot completed: register alarm. notes count : " + notes.size());
            for (NoteInfo note : notes) {
                alarmData.setAlarm(note);
            }
        }
    }

    private List<NoteInfo> getReminderNotes(Context context) {
        List<NoteInfo> result = new ArrayList<>();
        String[] columns = {
            Notes._ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT,
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_REMINDER_ENABLED,
            Notes.COLUMN_REMINDER_DATE,
        };
        String selection = Notes.COLUMN_REMINDER_ENABLED + " = ?";
        String[] selectionArgs = {
            Integer.toString(1)
        };
        Cursor cursor = context.getContentResolver().query(Notes.CONTENT_URI, columns, selection, selectionArgs, null);
        if (cursor != null) {
            int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
            int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);
            int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
            int idPos = cursor.getColumnIndex(Notes._ID);
            int reminderEnabledPos = cursor.getColumnIndex(Notes.COLUMN_REMINDER_ENABLED);
            int reminderDatePos = cursor.getColumnIndex(Notes.COLUMN_REMINDER_DATE);
            try {
                while (cursor.moveToNext()) {
                    String noteTitle = cursor.getString(noteTitlePos);
                    String noteText = cursor.getString(noteTextPos);
                    String courseId = cursor.getString(courseIdPos);
                    int id = cursor.getInt(idPos);
                    boolean reminderEnabled = cursor.getInt(reminderEnabledPos) == 1;
                    long reminderDate = cursor.getLong(reminderDatePos);

                    CourseInfo noteCourse = new CourseInfo(courseId, "", null);
                    NoteInfo note = new NoteInfo(id, noteCourse, noteTitle, noteText);
                    note.setReminderEnabled(reminderEnabled);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(reminderDate);
                    note.setReminderDate(calendar);

                    result.add(note);
                }
            } finally {
                cursor.close();
            }
        }

        return result;
    }

}
