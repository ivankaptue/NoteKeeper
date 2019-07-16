package com.klid.android.notekeeper;

import android.content.Context;
import android.content.Intent;

public class CourseEventBroadcastHelper {

    public static final String ACTION_COURSE_EVENT = "com.klid.android.notekeeper.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID = "com.klid.android.notekeeper.COURSE_ID";
    public static final String EXTRA_COURSE_MESSAGE = "com.klid.android.notekeeper.COURSE_MESSAGE";

    public static void sendBroadcast(Context context, String courseId, String message) {
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_COURSE_MESSAGE, message);

        context.sendBroadcast(intent);
    }
}
