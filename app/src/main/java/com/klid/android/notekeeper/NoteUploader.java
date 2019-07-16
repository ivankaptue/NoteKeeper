package com.klid.android.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;

import static com.klid.android.notekeeper.NoteActivity.CreateNoteTask.simulateLongRunningWork;

public class NoteUploader {

    private final String TAG = getClass().getSimpleName();
    private final Context mContext;
    private boolean mCancelled;

    public NoteUploader(Context ctx) {
        mContext = ctx;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    public void cancel() {
        mCancelled = true;
    }

    public void doUpload(Uri dataUri) {

        String[] columns = {
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT,
        };

        Cursor cursor = mContext.getContentResolver().query(dataUri, columns, null, null, null);

        if (cursor == null) return;

        int courseIdPos = cursor.getColumnIndex(Notes.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>*** UPDLOAD START - " + dataUri + "***<<<");
        mCancelled = false;
        while (!mCancelled && cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);
            if (!noteTitle.isEmpty()) {
                Log.i(TAG, ">>> UPLOADING Note <<< " + courseId + " | " + noteTitle + " | " + noteText);
                simulateLongRunningWork();
            }
        }
        Log.i(TAG, ">>>*** UPLOAD COMPLETE ***<<<");
        cursor.close();

    }

}
