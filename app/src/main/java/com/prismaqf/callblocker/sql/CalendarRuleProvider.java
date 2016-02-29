package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.rules.CalendarRule;

import java.util.ArrayList;


/**
 * @author ConteDiMonteCristo
 */
public class CalendarRuleProvider {
    private static final String TAG = CalendarRuleProvider.class.getCanonicalName();


    public static CalendarRule deserialize(Cursor c) {
        String name = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME));
        int dayMask = c.getInt(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_DAYMASK));
        String fromTime = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FROM));
        String toTime = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TO));

        return CalendarRule.makeRule(name,dayMask,fromTime,toTime);
    }

    public static void serialize(SQLiteDatabase db, CalendarRule cr) {
        InsertRow(db,cr);
    }

    /**
     * Insert a row in the calendarrule table
     * @param db the SQLite connection
     * @param rule the calendar rule
     * @return the new calendar rule id
     */
    public static long InsertRow(SQLiteDatabase db, CalendarRule rule) {
        ContentValues vals = new ContentValues();
        int binMask = rule.getBinaryMask();
        String fromTime = rule.getBareStartTime();
        String toTime = rule.getBareEndTime();
        vals.put(DbContract.CalendarRules.COLUMN_NAME_RULENAME,rule.getName());
        vals.put(DbContract.CalendarRules.COLUMN_NAME_DAYMASK, binMask);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FROM, fromTime);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_TO, toTime);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FORMAT, makeRuleFormat(binMask,fromTime,toTime));
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

    /**
     * Return all names of the rule to prevent re-inserting a rule with a given name
     * @param db the SQLite connection
     * @return a list of names
     */
    public static ArrayList<String> AllRuleNames(SQLiteDatabase db) {
        Cursor c = AllCalendarRules(db);
        ArrayList<String> names = new ArrayList<String>();
        while (c.moveToNext()) {
            String name = c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME));
            names.add(name);
        }

        return names;
    }

    public static void UpdateCalendarRule(SQLiteDatabase db, long ruleId, CalendarRule cr) {
        ContentValues vals = new ContentValues();
        int binMask = cr.getBinaryMask();
        String fromTime = cr.getBareStartTime();
        String toTime = cr.getBareEndTime();
        vals.put(DbContract.CalendarRules.COLUMN_NAME_DAYMASK, binMask);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FROM, fromTime);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_TO, toTime);
        vals.put(DbContract.CalendarRules.COLUMN_NAME_FORMAT, makeRuleFormat(binMask,fromTime,toTime));
        String selection = DbContract.CalendarRules._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ruleId) };
        db.update(DbContract.CalendarRules.TABLE_NAME,vals,selection,selectionArgs);
    }

    /**
     * Delete a rule by id
     * @param db the SQlite connection
     * @param ruleid the rule id
     */
    public static void DeleteCalendarRule(SQLiteDatabase db, long ruleid) {
        String where = DbContract.CalendarRules._ID + " = ?";
        String[] args = {String.valueOf(ruleid)};
        db.delete(DbContract.CalendarRules.TABLE_NAME, where, args);
    }

    /**
     * Delete a rule by name
     * @param db the SQlite connection
     * @param name the rule name
     */
    public static void DeleteCalendarRule(SQLiteDatabase db, String name) {
        String where = DbContract.CalendarRules.COLUMN_NAME_RULENAME + " = ?";
        String[] args = {name};
        db.delete(DbContract.CalendarRules.TABLE_NAME, where, args);
    }

    public static CalendarRule FindCalendarRule(SQLiteDatabase db, long ruleid) {
        String selection = DbContract.CalendarRules._ID + " = ?";
        String[] selectionArgs = { String.valueOf(ruleid) };
        Cursor c = db.query(DbContract.CalendarRules.TABLE_NAME,null,selection,selectionArgs,null,null,null,null);
        if (c.getCount() >0) {
            c.moveToFirst();
            return deserialize(c);
        }
        String msg = String.format("Could not find a calendar rule with id=%d",ruleid);
        Log.e(TAG,msg);
        throw new SQLException(msg);
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
