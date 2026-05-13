package com.example.modprojecttracker.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Date helpers used across project and expense screens. */
public class DateUtil {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US);
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);

    /** Formats milliseconds as dd/MM/yyyy. */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /** Formats milliseconds as dd/MM/yyyy HH:mm. */
    public static String formatDateTime(long timestamp) {
        return DATETIME_FORMAT.format(new Date(timestamp));
    }

    /** Formats milliseconds as HH:mm. */
    public static String formatTime(long timestamp) {
        return TIME_FORMAT.format(new Date(timestamp));
    }

    /** Returns current time in milliseconds. */
    public static long getCurrentDate() {
        return System.currentTimeMillis();
    }

    /** Returns a timestamp after the given number of days. */
    public static long getDateAfterDays(long baseDate, int days) {
        return baseDate + (days * Constants.MILLISECONDS_PER_DAY);
    }

    /** Returns a timestamp before the given number of days. */
    public static long getDateBeforeDays(long baseDate, int days) {
        return baseDate - (days * Constants.MILLISECONDS_PER_DAY);
    }

    /** Returns absolute day distance between two timestamps. */
    public static int getDaysBetween(long startDate, long endDate) {
        long diff = Math.abs(endDate - startDate);
        return (int) (diff / Constants.MILLISECONDS_PER_DAY);
    }

    /** Returns true when timestamp is today. */
    public static boolean isToday(long timestamp) {
        long now = System.currentTimeMillis();
        return formatDate(now).equals(formatDate(timestamp));
    }

    /** Returns true when timestamp is in the past. */
    public static boolean isPastDate(long timestamp) {
        return timestamp < System.currentTimeMillis();
    }

    /** Returns true when timestamp is in the future. */
    public static boolean isFutureDate(long timestamp) {
        return timestamp > System.currentTimeMillis();
    }

    /** Returns first day of the month for the given date. */
    public static long getFirstDayOfMonth(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTimeInMillis();
    }

    /** Returns last day of the month for the given date. */
    public static long getLastDayOfMonth(long date) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

    /** Returns true when two timestamps are on the same day. */
    public static boolean isSameDay(long date1, long date2) {
        return formatDate(date1).equals(formatDate(date2));
    }
}

