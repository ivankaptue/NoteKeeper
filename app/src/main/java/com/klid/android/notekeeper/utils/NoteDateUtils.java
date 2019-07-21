package com.klid.android.notekeeper.utils;

import android.content.Context;
import android.text.format.DateUtils;
import androidx.core.os.ConfigurationCompat;
import org.apache.commons.text.WordUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NoteDateUtils {

    public static String formatCalendarLocale(Context context, int day, int month, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        String formattedString = DateUtils.formatDateTime(context, calendar.getTimeInMillis(),
            DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_ALL);
        return WordUtils.capitalizeFully(formattedString);
    }

    public static String formatTimeLocale(Context context, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return DateUtils.formatDateTime(context, calendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
    }

    public static long getPersistableDate(int day, int month, int year, int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        return calendar.getTimeInMillis();
    }

    public static Calendar getCalendarDate(long persistableDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(persistableDate);
        return calendar;
    }

    public static Locale getDefaultLocale(Context context) {
        return ConfigurationCompat.getLocales(context.getResources().getConfiguration()).get(0);
    }
}
