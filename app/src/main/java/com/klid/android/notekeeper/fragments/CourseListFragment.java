package com.klid.android.notekeeper.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.klid.android.notekeeper.CourseRecyclerAdapter;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Courses;
import com.klid.android.notekeeper.R;

import java.util.Objects;

public class CourseListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();

    public static final int COURSE_LOADER = 0;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: CourseFragment");
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(getContext(), null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_course_main, container, false);
        RecyclerView recyclerItems = view.findViewById(R.id.list_items);
        recyclerItems.setLayoutManager(new GridLayoutManager(getContext(), getResources().getInteger(R.integer.course_grid_span)));
        recyclerItems.setAdapter(mCourseRecyclerAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        LoaderManager.getInstance(this).restartLoader(COURSE_LOADER, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> loader = null;
        if (id == COURSE_LOADER) {
            final String[] columns = {
                Courses._ID,
                Courses.COLUMN_COURSE_TITLE,
            };

            String orderBy = Courses.COLUMN_COURSE_TITLE;

            loader = new CursorLoader(Objects.requireNonNull(getContext()), Courses.CONTENT_URI, columns, null, null, orderBy);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == COURSE_LOADER) {
            mCourseRecyclerAdapter.changeCursor(data);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == COURSE_LOADER) {
            mCourseRecyclerAdapter.changeCursor(null);
        }
    }
}
