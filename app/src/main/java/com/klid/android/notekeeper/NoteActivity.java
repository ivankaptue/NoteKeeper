package com.klid.android.notekeeper;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import com.google.android.material.snackbar.Snackbar;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Courses;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;
import com.klid.android.notekeeper.utils.NoteDateUtils;
import com.klid.android.notekeeper.utils.SoftInputUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;

public class NoteActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    public static final String NOTE_ID = "com.klid.android.notekeeper.NOTE_ID";
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.klid.android.notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.klid.android.notekeeper.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_TEXT = "com.klid.android.notekeeper.ORIGINAL_NOTE_TEXT";
    public static final String ORIGINAL_NOTE_REMINDER_DATE = "com.klid.android.notekeeper.ORIGINAL_NOTE_REMINDER_DATE";
    public static final int ID_NOT_SET = -1;
    //    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNoteId;
    private boolean mIsCancelling;
    private String mOriginalNoteCourseId;
    private String mOriginalNoteTitle;
    private String mOriginalNoteText;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCoursesQueryFinished;
    private boolean mNotesQueryFinished;
    private Uri mNoteUri;
    private boolean mIsDeleting = false;
    private ModuleStatusView mViewModuleStatus;
    private EditText mNoteReminderDate;
    private EditText mNoteReminderTime;
    private TimePickerFragment mTimePickerFragment;
    private DatePickerFragment mDatePickerFragment;
    private ImageButton mNoteReminderCancelButton;
    private int mNoteReminderDatePos;
    private long mOriginalNoteReminderDate;
    private CheckBox mNoteReminderCheckbox;
    private int mOriginalNoteReminderEnabled;
    private int mNoteReminderEnabledPos;
    private LinearLayout mNoteReminderTimeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDbOpenHelper = new NoteKeeperOpenHelper(this);

        mSpinnerCourses = findViewById(R.id.spinner_courses);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item, null,
            new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE}, new int[]{android.R.id.text1}, 0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);

        LoaderManager.getInstance(this).initLoader(LOADER_COURSES, null, this);

        readDisplayStateValues();
        if (savedInstanceState != null) {
            restoreOriginalNoteValues(savedInstanceState);
        }

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if (!mIsNewNote)
            LoaderManager.getInstance(this).initLoader(LOADER_NOTES, null, this);

        mViewModuleStatus = findViewById(R.id.module_status);
        loadModuleStatusValues();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, 0);
        } else {
            displayNetworkState();
        }

        mNoteReminderTimeContainer = findViewById(R.id.note_reminder_time_container);
        ImageButton noteDatePickerButton = findViewById(R.id.note_date_picker);
        ImageButton noteTimePickerButton = findViewById(R.id.note_time_picker);

        mNoteReminderDate = findViewById(R.id.note_reminder_date);
        mNoteReminderDate.setOnClickListener(this::showDatePickerDialog);
        mNoteReminderDate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showDatePickerDialog(v);
        });
        noteDatePickerButton.setOnClickListener(view -> onDatePickerButtonClicked());
        mDatePickerFragment = new DatePickerFragment(this, mNoteReminderDate);

        mNoteReminderCancelButton = findViewById(R.id.note_cancel_reminder_date);
        mNoteReminderCancelButton.setOnClickListener(v -> handleCancelReminderDate());

        mNoteReminderTime = findViewById(R.id.note_reminder_time);
        mNoteReminderTime.setOnClickListener(this::showTimePickerDialog);
        mNoteReminderTime.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) showTimePickerDialog(v);
        });
        noteTimePickerButton.setOnClickListener(v -> onTimePickerButtonClicked());
        mTimePickerFragment = new TimePickerFragment(this, mNoteReminderTime);

        mNoteReminderCheckbox = findViewById(R.id.note_reminder_checkbox);
    }

    private void onDatePickerButtonClicked() {
        mNoteReminderDate.clearFocus();
        mNoteReminderDate.requestFocus();
    }

    private void onTimePickerButtonClicked() {
        mNoteReminderTime.clearFocus();
        mNoteReminderTime.requestFocus();
    }

    private void handleCancelReminderDate() {
        mNoteReminderDate.setText("");
        mNoteReminderTime.setText("");

        if (mDatePickerFragment != null) {
            mDatePickerFragment.setCalendar(null);
        }
        if (mTimePickerFragment != null) {
            mTimePickerFragment.setCalendar(null);
        }

        mNoteReminderTimeContainer.setVisibility(View.GONE);
        mNoteReminderCancelButton.setVisibility(View.GONE);
    }

    private void showTimePickerDialog(View v) {
        mTimePickerFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        mDatePickerFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void loadModuleStatusValues() {
        // in real life we'd lookup the selected course's statuses from the content provider
        int totalNumberOfModules = 10;
        int completedNumberOfModules = 7;
        boolean[] moduleStatus = new boolean[totalNumberOfModules];
        for (int i = 0; i < completedNumberOfModules; i++) {
            moduleStatus[i] = true;
        }

        mViewModuleStatus.setModuleStatus(moduleStatus);
    }

    private void displayNetworkState() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                Log.i(TAG, Boolean.toString(networkInfo != null && networkInfo.isConnected()));
                return null;
            }
        };

        task.execute();

//        Log.i(TAG, Boolean.toString(connectivityManager.isDefaultNetworkActive()));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    displayNetworkState();
                } else {
                    Log.i(TAG, "Permission denied");
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoftInputUtils.hideSoftKeyboard(this);
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private void loadCourseData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
        final String[] courseColumns = {
            CourseInfoEntry.COLUMN_COURSE_ID,
            CourseInfoEntry.COLUMN_COURSE_TITLE,
            CourseInfoEntry._ID,
        };
        Cursor cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
            null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);

        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        String courseId = "android_intents";
        String titleStart = "dynamic";

        String selection = NoteInfoEntry._ID + " = ?";

        String[] selectionArgs = {Integer.toString(mNoteId)};

        String[] noteColumns = {
            NoteInfoEntry.COLUMN_COURSE_ID,
            NoteInfoEntry.COLUMN_NOTE_TITLE,
            NoteInfoEntry.COLUMN_NOTE_TEXT
        };
        mNoteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
            selection, selectionArgs, null, null, null);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteCursor.moveToNext();
        displayNote();
    }


    private void restoreOriginalNoteValues(Bundle savedInstanceState) {
        mOriginalNoteCourseId = savedInstanceState.getString(ORIGINAL_NOTE_COURSE_ID);
        mOriginalNoteTitle = savedInstanceState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText = savedInstanceState.getString(ORIGINAL_NOTE_TEXT);
        mOriginalNoteReminderDate = savedInstanceState.getLong(ORIGINAL_NOTE_REMINDER_DATE);
    }

    private void saveOriginalNoteValues(String courseId, String noteTitle, String noteText, long reminderDate, int reminderDateEnabled) {
        mOriginalNoteCourseId = courseId;
        mOriginalNoteTitle = noteTitle;
        mOriginalNoteText = noteText;
        mOriginalNoteReminderDate = reminderDate;
        mOriginalNoteReminderEnabled = reminderDateEnabled;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mIsDeleting) {
            if (mIsCancelling) {
                Log.i(TAG, "Cancelling note at position: " + mNoteId);
                if (mIsNewNote) {
                    deleteNoteFromDatabase();
                } else {
                    restorePreviousNoteValues();
                }
            } else {
                saveNote();
            }
        }
        Log.d(TAG, "onPause");
    }

    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};


        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(Notes.CONTENT_URI, selection, selectionArgs);
                /*SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);*/
                return null;
            }
        };

        task.execute();
    }

    private void restorePreviousNoteValues() {
//        CourseInfo course = DataManager.getInstance().getCourse(mOriginalNoteCourseId);
//        mNote.setCourse(course);
//        mNote.setTitle(mOriginalNoteTitle);
//        mNote.setText(mOriginalNoteText);
        saveNoteToDatabase(mOriginalNoteCourseId, mOriginalNoteTitle, mOriginalNoteText, mOriginalNoteReminderDate, mOriginalNoteReminderEnabled);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mOriginalNoteCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
        outState.putLong(ORIGINAL_NOTE_REMINDER_DATE, mOriginalNoteReminderDate);
    }

    private void saveNote() {
        String courseId = selectedCourseId();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();
        boolean reminderEnabled = mNoteReminderCheckbox.isChecked();
        Calendar calendar = null;
        if (mDatePickerFragment.getCalendar() != null) {
            calendar = mDatePickerFragment.getCalendar();
            Calendar timePickerCalendar = mTimePickerFragment.getCalendar();
            if (timePickerCalendar != null) {
                calendar.set(Calendar.HOUR_OF_DAY, timePickerCalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, timePickerCalendar.get(Calendar.MINUTE));
            }
        }
        saveNoteToDatabase(courseId, noteTitle, noteText, calendar == null ? 0 : calendar.getTimeInMillis(), reminderEnabled ? 1 : 0);
    }

    private String selectedCourseId() {
        int selectedPosition = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        return cursor.getString(courseIdPos);
    }

    private void saveNoteToDatabase(String courseId, String noteTitle, String noteText, long reminderDate, int reminderEnabled) {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, courseId);
        values.put(Notes.COLUMN_NOTE_TITLE, noteTitle);
        values.put(Notes.COLUMN_NOTE_TEXT, noteText);
        values.put(Notes.COLUMN_REMINDER_DATE, reminderDate);
        values.put(Notes.COLUMN_REMINDER_ENABLED, reminderEnabled);

        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().update(Notes.CONTENT_URI, values, selection, selectionArgs);
                /*SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);*/
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                CourseInfo courseInfo = new CourseInfo(courseId, "", null);
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(reminderDate);
                NoteInfo note = new NoteInfo(mNoteId, courseInfo, noteTitle, noteText);
                note.setReminderEnabled(reminderEnabled == 1);
                note.setReminderDate(calendar);
                setNoteReminder(note);
            }
        };

        task.execute();
    }

    private void setNoteReminder(NoteInfo note) {
        Calendar calendar = note.getReminderDate();
        Log.i(TAG, "note date reminder " + calendar.getTime().toString());
        AlarmData alarmData = new AlarmData(this);
        alarmData.cancelReminder(note.getId());
        alarmData.setAlarm(note);
    }

    private void displayNote() {
//        List<CourseInfo> courses = DataManager.getInstance().getCourses();
//        CourseInfo course = DataManager.getInstance().getCourse(courseId);
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);
        long reminderDate = mNoteCursor.getLong(mNoteReminderDatePos);
        int reminderDateEnabled = mNoteCursor.getInt(mNoteReminderEnabledPos);

        Log.i(TAG, "note reminder date " + reminderDate);

        int courseIndex = getIndexOfCourseId(courseId);

        mSpinnerCourses.setSelection(courseIndex);
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
        mNoteReminderCheckbox.setChecked(reminderDateEnabled == 1);
        displayNoteReminderDate(reminderDate);
        CourseEventBroadcastHelper.sendBroadcast(this, courseId, "Editing note");
    }

    private void displayNoteReminderDate(long reminderDate) {
        if (reminderDate == 0) return;

        mNoteReminderTimeContainer.setVisibility(View.VISIBLE);
        mNoteReminderCancelButton.setVisibility(View.VISIBLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(reminderDate);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        mNoteReminderDate.setText(NoteDateUtils.formatCalendarLocale(this, day, month, year));
        mNoteReminderTime.setText(NoteDateUtils.formatTimeLocale(this, hour, minute));
        mDatePickerFragment.setCalendar(calendar);
        mTimePickerFragment.setCalendar(calendar);
    }

    private int getIndexOfCourseId(String courseId) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseRowIndex = 0;
        int courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId)) {
                courseRowIndex = i;
                break;
            }
        }
        return courseRowIndex;
        /*boolean more = cursor.moveToFirst();
        while (more) {
            String cursorCourseId = cursor.getString(courseIdPos);
            if (courseId.equals(cursorCourseId)) break;

            courseRowIndex++;
            more = cursor.moveToNext();
        }*/
    }

    private void readDisplayStateValues() {
        Intent intent = getIntent();
        mNoteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }

        Log.i(TAG, "mNoteId: " + mNoteId);
//        mNote = DataManager.getInstance().getNotes().get(mNoteId);

    }

    private void createNewNote() {
        AsyncTask<ContentValues, Integer, Uri> task = new CreateNoteTask(this);

        ContentValues values = new ContentValues();
        values.put(Notes.COLUMN_COURSE_ID, "");
        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");

        Log.d(TAG, "call execute " + Thread.currentThread().getId());
        task.execute(values);

//                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
//                mNoteId = (int) db.insert(NoteInfoEntry.TABLE_NAME, null, values);
    }

    private void displaySnackBar(String uri) {
        Snackbar.make(mSpinnerCourses, uri, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_send_mail) {
            sendEmail();
            return true;
        } else if (id == R.id.action_cancel) {
            mIsCancelling = true;
            finish();
        } else if (id == R.id.action_next) {
//            moveNext();
        } else if (id == R.id.action_delete) {
            actionDeleteNote();
        } else if (id == R.id.action_set_reminder) {
            showReminderNotification();
        } else if (id == R.id.action_cancel_reminder) {
            cancelReminder();
        }

        return super.onOptionsItemSelected(item);
    }

    private void cancelReminder() {
        AlarmData alarmData = new AlarmData(this);
        alarmData.cancelReminder(mNoteId);
    }

    private void showReminderNotification() {
        /*PendingIntent pendingIntent = getAlarmIntent();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long currentTimeInMilliseconds = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long SECONDS = 30 * 1000; // 30 seconds
        long alarmTime = currentTimeInMilliseconds + SECONDS;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) + 1);

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);*/
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, alarmTime, SECONDS, pendingIntent);
//        NoteReminderNotification.notify(this, noteTitle, noteText, mNoteId);
    }

    private void actionDeleteNote() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteId)};


        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                getContentResolver().delete(Notes.CONTENT_URI, selection, selectionArgs);
                /*SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);*/
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                Log.d(TAG, "deleted");
                mIsDeleting = true;
                Toast.makeText(NoteActivity.this, "Deleted", Toast.LENGTH_LONG).show();
                finish();
            }
        };

        task.execute();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_next);
        int lastNoteIndex = DataManager.getInstance().getNotes().size() - 1;
        item.setEnabled(mNoteId < lastNoteIndex);
        return super.onPrepareOptionsMenu(menu);
    }

//    private void moveNext() {
//        saveNote();
//
//        ++mNoteId;
////        mNote = DataManager.getInstance().getNotes().get(mNoteId);
//
//        saveOriginalNoteValues();
//        displayNote();
//        invalidateOptionsMenu();
//    }

    private void sendEmail() {
//        CourseInfo course = (CourseInfo) mSpinnerCourses.getSelectedItem();
        String courseTitle = selectedCourseId();
        String subject = mTextNoteTitle.getText().toString();
        String text = "Checkout what I learned in the Pluralsight course \"" +
            courseTitle + "\"\n" + mTextNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if (id == LOADER_COURSES)
            loader = createLoaderCourses();

        return loader;
    }

    private CursorLoader createLoaderCourses() {
        mCoursesQueryFinished = false;
        Uri uri = Courses.CONTENT_URI;
        final String[] courseColumns = {
            Courses.COLUMN_COURSE_ID,
            Courses.COLUMN_COURSE_TITLE,
            Courses._ID,
        };

        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);

        /*return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();
                final String[] courseColumns = {
                    CourseInfoEntry.COLUMN_COURSE_ID,
                    CourseInfoEntry.COLUMN_COURSE_TITLE,
                    CourseInfoEntry._ID,
                };
                return db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                    null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE);
            }
        };*/
    }

    private CursorLoader createLoaderNotes() {
        mNotesQueryFinished = false;
        String[] noteColumns = {
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT,
            Notes.COLUMN_REMINDER_DATE,
            Notes.COLUMN_REMINDER_ENABLED,
        };

        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);

        /*return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

                String courseId = "android_intents";
                String titleStart = "dynamic";

                String selection = NoteInfoEntry._ID + " = ?";
                String[] selectionArgs = {Integer.toString(mNoteId)};

                String[] noteColumns = {
                    NoteInfoEntry.COLUMN_COURSE_ID,
                    NoteInfoEntry.COLUMN_NOTE_TITLE,
                    NoteInfoEntry.COLUMN_NOTE_TEXT
                };
                return db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
                    selection, selectionArgs, null, null, null);
            }
        };*/
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES)
            loadFinishedNotes(data);
        if (loader.getId() == LOADER_COURSES) {
            mAdapterCourses.changeCursor(data);
            mCoursesQueryFinished = true;
            displayNoteWhenQueryFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;

        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mNoteReminderDatePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_REMINDER_DATE);
        mNoteReminderEnabledPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_REMINDER_ENABLED);
        mNoteCursor.moveToFirst();

        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);
        long reminderDate = mNoteCursor.getLong(mNoteReminderDatePos);
        int reminderDateEnabled = mNoteCursor.getInt(mNoteReminderEnabledPos);
        saveOriginalNoteValues(courseId, noteTitle, noteText, reminderDate, reminderDateEnabled);

        mNotesQueryFinished = true;
        displayNoteWhenQueryFinished();
    }

    private void displayNoteWhenQueryFinished() {
        if (mCoursesQueryFinished && mNotesQueryFinished)
            displayNote();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            if (mNoteCursor != null) mNoteCursor.close();
        } else if (loader.getId() == LOADER_COURSES) {
            mAdapterCourses.changeCursor(null);
        }
    }

    public static class CreateNoteTask extends AsyncTask<ContentValues, Integer, Uri> {

        private WeakReference<NoteActivity> mActivityReference;

        public CreateNoteTask(NoteActivity ctx) {
            mActivityReference = new WeakReference<>(ctx);
        }

        @Override
        protected void onPreExecute() {
            NoteActivity activity = mActivityReference.get();
            ProgressBar mProgressBar = activity.findViewById(R.id.progress_bar);
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(0);
        }

        @Override
        protected Uri doInBackground(ContentValues... contentValues) {
            NoteActivity activity = mActivityReference.get();
            Log.d(activity.TAG, "call doInBackground " + Thread.currentThread().getId());

            ContentValues insertValues = contentValues[0];
            Uri uri = activity.getContentResolver().insert(Notes.CONTENT_URI, insertValues);

            for (int i = 1; i <= 100; i++) {
                // simulateLongRunningWork();
                publishProgress(i);
            }

            return uri;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            ProgressBar progressBar = getProgressBar();
            int value = values[0];
            progressBar.setProgress(value);
        }

        public static void simulateLongRunningWork() {
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }

        @Override
        protected void onPostExecute(Uri uri) {
            NoteActivity activity = mActivityReference.get();
            Log.d(activity.TAG, "call onPostExecute " + Thread.currentThread().getId());

            activity.mNoteUri = uri;
            activity.mNoteId = (int) ContentUris.parseId(activity.mNoteUri);
            activity.displaySnackBar(uri.toString());
            getProgressBar().setVisibility(View.GONE);
        }

        private ProgressBar getProgressBar() {
            NoteActivity activity = mActivityReference.get();
            return activity.findViewById(R.id.progress_bar);
        }
    }

    public static class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

        private final NoteActivity mContext;
        private Calendar mCalendar;

        public DatePickerFragment(NoteActivity context, EditText editText) {
            mContext = context;
            configureEditText(editText);
            Log.i("NoteActivity", "configure edittext");
        }

        private void configureEditText(EditText editText) {
            editText.setInputType(InputType.TYPE_NULL);
            editText.setShowSoftInputOnFocus(false);
            SoftInputUtils.hideSoftKeyboard(mContext);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = getCalendar() != null ? getCalendar() : Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(mContext, this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar calendar = Calendar.getInstance();
            mContext.mNoteReminderTimeContainer.setVisibility(View.VISIBLE);
            mContext.mNoteReminderCancelButton.setVisibility(View.VISIBLE);
            if (year == calendar.get(Calendar.YEAR) &&
                month == calendar.get(Calendar.MONTH) &&
                day == calendar.get(Calendar.DAY_OF_MONTH)) {
                setCalendar(calendar);
                mContext.mNoteReminderDate.setText(getString(R.string.today));
            } else {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                setCalendar(calendar);
                mContext.mNoteReminderDate.setText(NoteDateUtils.formatCalendarLocale(mContext, day, month, year));
            }
        }

        public Calendar getCalendar() {
            return mCalendar;
        }

        public void setCalendar(Calendar calendar) {
            mCalendar = calendar;
        }
    }

    public static class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

        private final NoteActivity mContext;
        private Calendar mCalendar;

        public TimePickerFragment(NoteActivity context, EditText editText) {
            mContext = context;
            configureEditText(editText);
            Log.i("NoteActivity", "configure edittext");
        }

        private void configureEditText(EditText editText) {
            editText.setInputType(InputType.TYPE_NULL);
            editText.setShowSoftInputOnFocus(false);
            SoftInputUtils.hideSoftKeyboard(mContext);
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE) + 2);
            final Calendar c = getCalendar() != null ? getCalendar() : currentCalendar;
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            return new TimePickerDialog(mContext, this, hour, minute, DateFormat.is24HourFormat(mContext));
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            setCalendar(calendar);
            mContext.mNoteReminderTime.setText(NoteDateUtils.formatTimeLocale(mContext, hourOfDay, minute));
        }

        public Calendar getCalendar() {
            return mCalendar;
        }

        public void setCalendar(Calendar calendar) {
            mCalendar = calendar;
        }
    }
}