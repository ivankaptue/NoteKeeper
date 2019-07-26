package com.klid.android.notekeeper.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.klid.android.notekeeper.NoteActivity;
import com.klid.android.notekeeper.NoteKeeperProviderContract.Notes;
import com.klid.android.notekeeper.NoteRecyclerAdapter;
import com.klid.android.notekeeper.R;

import java.util.Objects;

public class NoteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = getClass().getSimpleName();

    public static final int NOTES_LOADER = 0;
    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private Animation mScaleInAnim;
    private FloatingActionButton mFab;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: NoteFragment");
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(getContext(), null);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_note_main, container, false);
        RecyclerView recyclerItems = view.findViewById(R.id.list_items);
        recyclerItems.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerItems.setAdapter(mNoteRecyclerAdapter);

        mFab = view.findViewById(R.id.fab);
        mFab.setOnClickListener(v -> startActivity(new Intent(getContext(), NoteActivity.class)));
        mScaleInAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_in);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mFab.startAnimation(mScaleInAnim);
        LoaderManager.getInstance(this).restartLoader(NOTES_LOADER, null, this);
    }

    @Override
    public void onDestroyView() {
        Animation scaleOutAnim = AnimationUtils.loadAnimation(getContext(), R.anim.scale_out);
        mFab.startAnimation(scaleOutAnim);
        super.onDestroyView();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Cursor> loader = null;
        if (id == NOTES_LOADER) {
            final String[] noteColumns = {
                Notes._ID,
                Notes.COLUMN_NOTE_TITLE,
                Notes.COLUMN_COURSE_TITLE,
            };

            String noteOrderBy = Notes.COLUMN_COURSE_TITLE + "," + Notes.COLUMN_NOTE_TITLE;

            loader = new CursorLoader(Objects.requireNonNull(getContext()), Notes.CONTENT_EXPANDED_URI, noteColumns, null, null, noteOrderBy);
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == NOTES_LOADER) {
            loadFinishedNotes(data);
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteRecyclerAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == NOTES_LOADER) {
            mNoteRecyclerAdapter.changeCursor(null);
        }
    }
}
