package com.klid.android.notekeeper;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PersistableBundle;
import androidx.annotation.UiThread;

import java.lang.ref.WeakReference;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.klid.android.notekeeper.extras.DATA_URI";
    private NoteUploader mNoteUploader;

    @Override
    public boolean onStartJob(JobParameters params) {
        AsyncTask<JobParameters, Void, Void> task =  new NoteUploaderAsync(this);
        mNoteUploader = new NoteUploader(this);
        task.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        mNoteUploader.cancel();
        return true;
    }

    private static class NoteUploaderAsync  extends AsyncTask<JobParameters, Void, Void>{

        private final WeakReference<NoteUploaderJobService> mReference;

        NoteUploaderAsync(NoteUploaderJobService context) {
            mReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(JobParameters... backgroundParams) {
            NoteUploaderJobService noteUploaderJobService = mReference.get();
            JobParameters jobParams = backgroundParams[0];
            String stringDataUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
            Uri dataUri = Uri.parse(stringDataUri);
            noteUploaderJobService.mNoteUploader.doUpload(dataUri);

            if (!noteUploaderJobService.mNoteUploader.isCancelled()) {
                noteUploaderJobService.jobFinished(jobParams, false);
            }
            return null;
        }
    }

}
