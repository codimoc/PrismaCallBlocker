package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;


/**
 * @author ConteDiMonteCristo
 */
public class CalendarRule {
    private static final String TAG = CalendarRule.class.getCanonicalName();

    private final long id;
    private final String name;
    private final int daymask;
    private final String from;
    private final String to;
    private final Date timestamp;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDaymask() {
        return daymask;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Date getTimestamp() {
        return timestamp;
    }


    private CalendarRule(long id, String name, int daymask, String from, String to, Date timestamp) {
        this.id = id;
        this.name = name;
        this.daymask = daymask;
        this.from = from;
        this.to = to;
        this.timestamp = timestamp;
    }

    public static CalendarRule deserialize(Cursor c) {
        long myId = c.getLong(c.getColumnIndexOrThrow(DbContract.CalendarRules._ID));
        String myName = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME));
        int myDayMask = c.getInt(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_DAYMASK));
        String myFrom = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FROM));
        String myTo = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TO));

        Date myTimestamp = null;
        try {

            DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
            String sts = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TIMESTAMP));
            if (sts != null) myTimestamp = format.parse(sts);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            throw new SQLException(e.getMessage());
        }
        return new CalendarRule(myId,myName,myDayMask,myFrom,myTo,myTimestamp);
    }

    public static void serialize(SQLiteDatabase db, CalendarRule cr) {
        InsertRow(db,cr.getName(),cr.getDaymask(),cr.getFrom(),cr.getTo());
    }

    /**
     * Insert a row in the calendarrule table
     * @param db the SQLite connection
     * @param name the name of the rule
     * @param daymask the binary mask of days, Mo-Su
     * @param from the start of the rule in hh:mm format
     * @param to the end of the rule in hh:mm format
     * @return the new calendar rule id
     */
    public static long InsertRow(SQLiteDatabase db, String name, int daymask, String from, String to) {
        ContentValues vals = new ContentValues();
        if (name == null) {
            final String msg = "Rule name is required for a calendar rule";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.CalendarRules.COLUMN_NAME_RULENAME,name);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_DAYMASK, daymask);
        if (from == null) from = "00:00";
        if (!hasHHMMFormat(from)) {
            final String msg = "Rule from tag is not in required format hh:mm";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FROM, from);
        if (to == null) to="23:59";
        if (!hasHHMMFormat(to)) {
            final String msg = "Rule to tag is not in required format hh:mm";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.CalendarRules.COLUMN_NAME_TO, to);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FORMAT, makeRuleFormat(daymask,from,to));
        return db.insert(DbContract.CalendarRules.TABLE_NAME, null, vals);
    }



    /**
     * Get the latest calendar rules in a given order
     * @param db the SQLite connection
     * @param maxRecords the total number of records returned
     * @param descending a flag to inicate the sorting order, descending when the flag is true
     * @return a cursor
     */
    public static Cursor LatestCalendarRules(SQLiteDatabase db, int maxRecords, boolean descending) {
        String orderby;
        if (descending)
            orderby= String.format("%s desc",DbContract.CalendarRules._ID);
        else
            orderby= String.format("%s asc",DbContract.CalendarRules._ID);
        String limit = null;
        if (maxRecords > 0)
            limit = String.valueOf(maxRecords);
        return db.query(DbContract.CalendarRules.TABLE_NAME, null, null, null, null, null, orderby, limit);
    }

    /**
     * Return all the calendar rules
     * @param db the SQLite connection
     * @return a cursor
     */
    public static Cursor AllCalendarRules(SQLiteDatabase db) {
        return LatestCalendarRules(db, -1, false);
    }

    public static void UpdateCalendarRule(SQLiteDatabase db, long ruleid, int daymask, String from, String to) {
        ContentValues vals = new ContentValues();
        vals.put(DbContract.CalendarRules.COLUMN_NAME_DAYMASK, daymask);
        if (from == null || !hasHHMMFormat(from)) {
            final String msg = "Rule from tag is not in required format hh:mm";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FROM, from);
        if (to == null || !hasHHMMFormat(to)) {
            final String msg = "Rule to tag is not in required format hh:mm";
            Log.e(TAG, msg);
            throw new SQLException(msg);
        }
        vals.put(DbContract.CalendarRules.COLUMN_NAME_TO, to);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FORMAT, makeRuleFormat(daymask,from,to));
        String selection = DbContract.CalendarRules._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ruleid) };
        db.update(DbContract.CalendarRules.TABLE_NAME,vals,selection,selectionArgs);
    }

    private static boolean hasHHMMFormat(String in) {
        String regex = "\\d{2}:\\d{2}";
        return in.length() == 5 && Pattern.matches(regex, in);
    }

    private static String makeRuleFormat(int daymask, String from, String to) {
        StringBuilder buffer = new StringBuilder("Days ");
        if ((daymask & 1) == 1) buffer.append('M');
        else buffer.append('-');
        if ((daymask & 2) == 2) buffer.append('T');
        else buffer.append('-');
        if ((daymask & 4) == 4) buffer.append('W');
        else buffer.append('-');
        if ((daymask & 8) == 8) buffer.append('T');
        else buffer.append('-');
        if ((daymask & 16) == 16) buffer.append('F');
        else buffer.append('-');
        if ((daymask & 32) == 32) buffer.append('S');
        else buffer.append('-');
        if ((daymask & 64) == 64) buffer.append('S');
        else buffer.append('-');
        buffer.append(String.format(" from %s to %s",from,to));
        return buffer.toString();
    }

}
