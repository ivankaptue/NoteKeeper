package com.klid.android.notekeeper;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;

import java.util.List;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener,
    LoaderManager.LoaderCallbacks<Cursor> {

    private static final String CURRENT_VIEW_ID = "CURRENT_VIEW_ID";
    public static final int LOADER_NOTES = 0;
    public static final int NOTE_UPLOADER_JOB_ID = 1;
    private final String TAG = getClass().getSimpleName();


    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCoursesLayoutManager;
    private NoteKeeperOpenHelper mDBOpenHelper;
    private Loader<Cursor> mNoteLoader;
    private DrawerLayout mDrawer;
    private int mCurrentViewId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableStrictMode();

        mDBOpenHelper = new NoteKeeperOpenHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, NoteActivity.class)));

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);

        mDrawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null)
            restoreInstanceState(savedInstanceState);

        initializeDisplayContent();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy =
                new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();

            StrictMode.setThreadPolicy(policy);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        mNoteLoader = LoaderManager.getInstance(this).restartLoader(LOADER_NOTES, null, this);
        /*if (loader != null) {
            loader.forceLoad();
        }*/
//        loadNotes();
//        mNoteRecyclerAdapter.notifyDataSetChanged();
//        mAdapterNotes.notifyDataSetChanged();

        updateNavHeader();

        if (mCurrentViewId != -1)
            selectNavigationMenuItem(mCurrentViewId);
//        openDrawer();
    }

    private void openDrawer() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> mDrawer.openDrawer(GravityCompat.START), 1500);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (mNoteLoader != null) {
            LoaderManager.getInstance(this).destroyLoader(mNoteLoader.getId());
        }*/
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onDestroy() {
        mDBOpenHelper.close();

        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void loadNotes() {
        SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
        final String[] noteColumns = {
            NoteInfoEntry.COLUMN_NOTE_TITLE,
            NoteInfoEntry.COLUMN_NOTE_TEXT,
            NoteInfoEntry.COLUMN_COURSE_ID,
            NoteInfoEntry._ID,
        };
        String noteOrderBy = NoteInfoEntry.COLUMN_COURSE_ID + " ASC," + NoteInfoEntry.COLUMN_NOTE_TITLE + " ASC";
        final Cursor noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns,
            null, null, null, null, noteOrderBy);

        mNoteRecyclerAdapter.changeCursor(noteCursor);
    }

    private void updateNavHeader() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView textUserName = headerView.findViewById(R.id.text_user_name);
        TextView textEmailAddress = headerView.findViewById(R.id.text_email_address);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = pref.getString("user_display_name", "");
        String emailAddress = pref.getString("user_email_address", "");

        textUserName.setText(userName);
        textEmailAddress.setText(emailAddress);
    }

    private void initializeDisplayContent() {
//        final ListView listNotes = findViewById(R.id.list_notes);
//
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//
//        mAdapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notes);
//        listNotes.setAdapter(mAdapterNotes);
//
//        listNotes.setOnItemClickListener((parent, view, position, id) -> {
//            Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);
////            NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);
////            Log.d("NOTE", note.toString());
//            intent.putExtra(NoteActivity.NOTE_ID, position);
//            startActivity(intent);
//        });

        DataManager.loadFromDatabase(mDBOpenHelper);
        mRecyclerItems = findViewById(R.id.list_items);
        mNotesLayoutManager = new LinearLayoutManager(this);
        mCoursesLayoutManager = new GridLayoutManager(this,
            getResources().getInteger(R.integer.course_grid_span));

        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, null);

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        if (mCurrentViewId == R.id.nav_courses) {
            displayCourses();
        } else {
            displayNotes();
        }
    }

    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_notes);
    }

    private void selectNavigationMenuItem(int itemId) {
        Log.d(TAG, "selectNavigationMenuItem : " + itemId);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(itemId).setChecked(true);
        mCurrentViewId = itemId;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENT_VIEW_ID, mCurrentViewId);
        super.onSaveInstanceState(outState);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentViewId = savedInstanceState.getInt(CURRENT_VIEW_ID);
    }

    private void displayCourses() {
        mRecyclerItems.setLayoutManager(mCoursesLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);

        selectNavigationMenuItem(R.id.nav_courses);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_backup_notes) {
            backupNotes();
        } else if (id == R.id.action_upload_notes) {
            scheduleNoteUpload();
        }

        return super.onOptionsItemSelected(item);
    }

    private void scheduleNoteUpload() {
        PersistableBundle extras = new PersistableBundle();
        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());

        ComponentName componentName = new ComponentName(this, NoteUploaderJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(NOTE_UPLOADER_JOB_ID, componentName)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setExtras(extras)
            .build();

        JobScheduler jobScheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(jobInfo);
    }

    private void backupNotes() {
//        NoteBackup.doBackup(MainActivity.this, NoteBackup.ALL_COURSES);
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_notes) {
            displayNotes();
        } else if (id == R.id.nav_courses) {
            displayCourses();
        } else if (id == R.id.nav_share) {
//            handleSelection(R.string.nav_share_message);
            handleShare();
        } else if (id == R.id.nav_send) {
            handleSelection(R.string.nav_send_message);
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, "Shared to - " +
                PreferenceManager.getDefaultSharedPreferences(this).getString("user_favorite_social", ""),
            Snackbar.LENGTH_LONG).setAction("cancel", v -> Log.d(TAG, "Snackbar action")).show();
    }

    private void handleSelection(int messageId) {
        View view = findViewById(R.id.list_items);
        Snackbar.make(view, messageId, Snackbar.LENGTH_LONG)
            .setAction("cancel", v -> Log.d(TAG, "Snackbar action"))
            .show();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        CursorLoader loader = new CursorLoader(this);
        if (id == LOADER_NOTES)
            loader = createLoaderNotes();
        return loader;
    }

    private CursorLoader createLoaderNotes() {

        final String[] noteColumns = {
            Notes._ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_COURSE_TITLE,
        };

        String noteOrderBy = Notes.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;

        return new CursorLoader(this, Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, noteOrderBy);
       /* return new CursorLoader(this) {
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();
                final String[] noteColumns = {
                    NoteInfoEntry.getQName(NoteInfoEntry._ID),
                    NoteInfoEntry.COLUMN_NOTE_TITLE,
                    CourseInfoEntry.COLUMN_COURSE_TITLE,
                };

                String noteOrderBy = CourseInfoEntry.COLUMN_COURSE_TITLE + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;

                // note_info join course_info on note_info.course_id = course_info.course_id;
                String tableWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " +
                    CourseInfoEntry.TABLE_NAME + " ON " +
                    CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID) + " = " +
                    NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID);
                return db.query(tableWithJoin, noteColumns,
                    null, null, null, null, noteOrderBy);
            }
        };*/
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == LOADER_NOTES) {
            loadFinishedNotes(data);
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteRecyclerAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == LOADER_NOTES) {
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}
