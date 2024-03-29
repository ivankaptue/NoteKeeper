package com.klid.android.notekeeper;

import android.provider.BaseColumns;

public final class NoteKeeperDatabaseContract {

    private NoteKeeperDatabaseContract() {
    }

    public static final class CourseInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        // CREATE INDEX course_info_index1 ON course_info(course_title);
        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 =
            "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                "(" + COLUMN_COURSE_TITLE + ")";

        public static String getQName(String columnName) {
            return TABLE_NAME + "." + columnName;
        }

        public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_COURSE_ID + " TEXT UNIQUE NOT NULL, " +
                COLUMN_COURSE_TITLE + " TEXT NOT NULL)";
    }

    public static final class NoteInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_REMINDER_ENABLED = "reminder_enabled";
        public static final String COLUMN_REMINDER_DATE = "reminder_date";

        // REMINDER COLUMNS
        public static final String ADD_COLUMN_REMINDER_STATE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_REMINDER_ENABLED + " BOOLEAN;";
        public static final String ADD_COLUMN_REMINDER_DATE = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COLUMN_REMINDER_DATE + " INTEGER;";

        // CREATE INDEX note_info_index1 ON note_info(note_title);
        public static final String INDEX1 = TABLE_NAME + "_index1";
        public static final String SQL_CREATE_INDEX1 =
            "CREATE INDEX " + INDEX1 + " ON " + TABLE_NAME +
                "(" + COLUMN_NOTE_TITLE + ")";

        public static String getQName(String columnName) {
            return TABLE_NAME + "." + columnName;
        }

        public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY, " +
                COLUMN_NOTE_TITLE + " TEXT NOT NULL, " +
                COLUMN_NOTE_TEXT + ", " +
                COLUMN_COURSE_ID + " TEXT NOT NULL)";
    }

}
