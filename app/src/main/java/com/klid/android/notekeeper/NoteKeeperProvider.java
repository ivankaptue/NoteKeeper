package com.klid.android.notekeeper;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Courses;
import com.klid.android.notekeeper.NoteKeeperProviderContract.CoursesIdColumns;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteKeeperProvider extends ContentProvider {

    public static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    private NoteKeeperOpenHelper mDbOpenHelper;

    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXPANDED = 2;

    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTES_ROW);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriMatch = sUriMatcher.match(uri);
        int rowNb = -1;
        switch (uriMatch) {
            case NOTES:
                SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
                return db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
            case COURSES:
                // throw exception cannot delete course at moment
                break;
            case NOTES_EXPANDED:
                // throw exception read only uri
                break;
        }
        return rowNb;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch) {
            case COURSES:
                // vnd.android.cursor.dir/vnd.com.klid.android.notekeeper.provider.courses
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri rowUri = null;
        long rowId = -1;
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                // content://com.klid.android.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                // content://com.klid.android.notekeeper.provider/courses/1
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId);
                break;
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
                break;
        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowId)};

                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs,
                    null, null, null);
                break;
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for (int idx = 0; idx < projection.length; idx++) {
            String pv = projection[idx];
            columns[idx] = pv.equals(BaseColumns._ID) ||
                pv.equals(CoursesIdColumns.COLUMN_COURSE_ID)
                ? NoteInfoEntry.getQName(pv) : pv;
        }

        String tableWithJoin = NoteInfoEntry.TABLE_NAME + " LEFT JOIN " +
            CourseInfoEntry.TABLE_NAME + " ON " +
            CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID) + " = " +
            NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID);

        return db.query(tableWithJoin, columns, selection, selectionArgs,
            null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();

        int rowNb = -1;
        int uriMatch = sUriMatcher.match(uri);

        switch (uriMatch) {
            case NOTES:
                return db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
            case COURSES:
                return db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
            case NOTES_EXPANDED:
                // throw exception saying that this is a read-only table
                break;
        }
        return rowNb;
    }
}
