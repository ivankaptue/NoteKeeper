package com.klid.android.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;
    private final SharedPreferences mPrefs;
    private int mDeletedNoteId;
    private int mDeletedNotePosition;
    private final RecyclerView mRecyclerView;

    public NoteRecyclerAdapter(Context context, Cursor cursor, RecyclerView recyclerView) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        mRecyclerView = recyclerView;

    }

    private void populateColumnPositions() {
        if (!isCursorValid()) return;

        mCoursePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) mCursor.close();

        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = mLayoutInflater.inflate(R.layout.item_note_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if (isCursorValid()) {
            mCursor.moveToPosition(position);
            String course = mCursor.getString(mCoursePos);
            String noteTitle = mCursor.getString(mNoteTitlePos);

            viewHolder.mTextCourse.setText(course);
            viewHolder.mTextTitle.setText(noteTitle);
        }
    }

    private boolean isCursorValid() {
        return mCursor != null && !mCursor.isClosed();
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void deleteItem(int position) {
        if (isCursorValid()) {
            mCursor.moveToPosition(position);
            mDeletedNoteId = mCursor.getInt(mIdPos);
            mDeletedNotePosition = position;
            notifyItemRemoved(position);
            showUndoSnackbar();
        }
    }

    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(mRecyclerView, "Note deleted",
            Snackbar.LENGTH_LONG);
        snackbar.setAction("Undo", v -> undoDelete());
        snackbar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                deleteNoteFromDatabase(mDeletedNoteId);
            }
        });
        snackbar.show();
    }

    private void undoDelete() {
        notifyItemInserted(mDeletedNotePosition);
    }

    private void deleteNoteFromDatabase(int noteId) {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(noteId)};


        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                mContext.getContentResolver().delete(Notes.CONTENT_URI, selection, selectionArgs);
                return null;
            }
        };

        task.execute();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(view -> {
                String username = mPrefs.getString("user_display_name", "");
                if (username != null && username.equals(mContext.getResources().getString(R.string.pref_default_display_name))) {
                    SettingsReminderNotification.notify(mContext);
                }

                mCursor.moveToPosition(getAdapterPosition());
                int noteId = mCursor.getInt(mIdPos);

                Intent intent = new Intent(mContext, NoteActivity.class);
                intent.putExtra(NoteActivity.NOTE_ID, noteId);
                mContext.startActivity(intent);
            });
        }

    }

}
