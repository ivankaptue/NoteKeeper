package com.klid.android.notekeeper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class NoteSwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private NoteRecyclerAdapter mAdapter;
    private final ColorDrawable mColor;
    private final Drawable mIcon;

    public NoteSwipeToDeleteCallback(Context context, NoteRecyclerAdapter adapter) {
//        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        super(0, ItemTouchHelper.LEFT);
        mAdapter = adapter;
        mColor = new ColorDrawable(Color.RED);
        mIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_black_24dp);
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        int iconMargin = (itemView.getHeight() - mIcon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - mIcon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + mIcon.getIntrinsicHeight();
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) itemView.getLayoutParams();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + mIcon.getIntrinsicWidth();

            mIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            mColor.setBounds(
                itemView.getLeft() + marginParams.leftMargin,
                itemView.getTop(),
                itemView.getLeft() + ((int) dX) + marginParams.leftMargin,
                itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - mIcon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            mIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            mColor.setBounds(
                itemView.getRight() + ((int) dX) - marginParams.rightMargin,
                itemView.getTop(),
                itemView.getRight() - marginParams.rightMargin,
                itemView.getBottom());
        } else { // view is unSwiped
            mColor.setBounds(0, 0, 0, 0);
        }

        mColor.draw(c);
        mIcon.draw(c);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        mAdapter.deleteItem(position);
    }
}
