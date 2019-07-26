package com.klid.android.notekeeper;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Courses;

public class CourseRecyclerAdapter extends RecyclerView.Adapter<CourseRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private final LayoutInflater mLayoutInflater;
    private Cursor mCursor;
    private int mCourseIdPos;
    private int mCourseTitlePos;

    public CourseRecyclerAdapter(Context context, Cursor cursor) {
        mContext = context;
        mLayoutInflater = LayoutInflater.from(mContext);
        mCursor = cursor;
        populateColumnsPosition();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = mLayoutInflater.inflate(R.layout.item_course_list, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
       if (isValidCursor(mCursor)) {
           mCursor.moveToPosition(i);
           String courseTitle = mCursor.getString(mCourseTitlePos);
           viewHolder.mTextCourse.setText(courseTitle);
       }
    }

    @Override
    public int getItemCount() {
        return mCursor != null ? mCursor.getCount() : 0;
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) mCursor.close();

        mCursor = cursor;
        populateColumnsPosition();
        notifyDataSetChanged();
    }

    private void populateColumnsPosition() {
        if (!isValidCursor(mCursor)) return;
        mCourseIdPos = mCursor.getColumnIndex(Courses.COLUMN_COURSE_ID);
        mCourseTitlePos = mCursor.getColumnIndex(Courses.COLUMN_COURSE_TITLE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mTextCourse;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextCourse = itemView.findViewById(R.id.text_course);

            itemView.setOnClickListener(view -> {
                if (isValidCursor(mCursor)) {
                    mCursor.moveToPosition(getAdapterPosition());
                    String courseTitle = mCursor.getString(mCourseTitlePos);
                    Snackbar.make(view, courseTitle, Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    private boolean isValidCursor(Cursor cursor) {
        return cursor != null && !cursor.isClosed();
    }

}
