package com.klid.android.notekeeper;

import android.app.IntentService;
import android.content.Intent;


/**
 * A note backup service to backup notes.
 */
public class NoteBackupService extends IntentService {

    public static final String EXTRA_COURSE_ID = "com.klid.android.notekeeper.extra.COURSE_ID";

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            String backupCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
            NoteBackup.doBackup(this, backupCourseId);
        }
    }

}
