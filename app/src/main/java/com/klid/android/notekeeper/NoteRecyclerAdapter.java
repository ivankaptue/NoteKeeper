package com.klid.android.notekeeper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.klid.android.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int mCoursePos;
    private int mNoteTitlePos;
    private int mIdPos;
    private final SharedPreferences mPrefs;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;
        public final TextView mTextTitle;

        public ViewHolder(View itemView) {
            super(itemView);

            String username = mPrefs.getString("user_display_name", "");

            mTextCourse = itemView.findViewById(R.id.text_course);
            mTextTitle = itemView.findViewById(R.id.text_title);

            itemView.setOnClickListener(view -> {
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
