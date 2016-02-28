package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author ConteDiMonteCristo.
 */
public class LoggedCall {
    private static final String TAG = LoggedCall.class.getCanonicalName();

    private final long id;
    private final long runid;
    private final String number;
    private final String description;
    private final int ruleid;


    private LoggedCall(long id, long runid, int ruleid, String number, String description) {
        this.id = id;
        this.runid = runid;
        this.ruleid = ruleid;
        this.number = number;
        this.description = description;
    }

    public static LoggedCall deserialize(Cursor c) {
        long myId = c.getLong(c.getColumnIndexOrThrow(DbContract.LoggedCalls._ID));
        long myRunId = c.getLong(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RUNID));
        int myRuleId = -1;
        if (!c.isNull(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RULEID)))
            myRuleId = c.getInt(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RULEID));
        String myNumber = c.getString(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_NUMBER));
        String myDescription = c.getString(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION));
        return new LoggedCall(myId,myRunId,myRuleId,myNumber,myDescription);
    }

    public static void serialize(SQLiteDatabase db, LoggedCall lc) {
        Integer ruleId = null;
        if (lc.getRuleid() > 0)
            ruleId = lc.getRuleid();
        InsertRow(db,lc.getRunid(),lc.getNumber(), lc.getDescription(), ruleId);
    }

    public long getId() {
        return id;
    }

    public long getRunid() {
        return runid;
    }

    public String getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }

    public int getRuleid() {
        return ruleid;
    }

    /**
     * Insert a new record (logged call) in the database
     * @param db the SQLite connection
     * @param runid the service run id
     * @param number the calling number
     * @param description (nullable) the description like the contact name corresponding to the number
     * @param ruleid (nullable) the rule id if applicable
     * @return the new call id
     */
    public static long InsertRow(SQLiteDatabase db, long runid, String number, String description, Integer ruleid) {
        ContentValues vals = new ContentValues();
        vals.put(DbContract.LoggedCalls.COLUMN_NAME_RUNID,runid);
        if (number == null)
            Log.e(TAG, "Phone number required for a logged call");
        vals.put(DbContract.LoggedCalls.COLUMN_NAME_NUMBER,number);
        if (description != null)
            vals.put(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION,description);
        if (ruleid != null)
            vals.put(DbContract.LoggedCalls.COLUMN_NAME_RULEID,ruleid);
        return db.insert(DbContract.LoggedCalls.TABLE_NAME, DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION, vals);
    }

    /**
     * Retrieves the latest calls logged
     * @param db the SQLite connection
     * @param maxRecords the total number of records returned
     * @return a cursor
     */
    public static Cursor LatestCalls(SQLiteDatabase db, int maxRecords) {
        return LatestCalls(db, maxRecords, true);
    }

    /**
     * Retrieves the latest calls logged
     * @param db the SQLite connection
     * @param maxRecords the total number of records returned
     * @param descending a flag to inicate the sorting order, descending when the flag is true
     * @return a cursor
     */
    public static Cursor LatestCalls(SQLiteDatabase db, int maxRecords, boolean descending) {
        String orderby;
        if (descending)
            orderby= String.format("%s desc",DbContract.LoggedCalls._ID);
        else
            orderby= String.format("%s asc",DbContract.LoggedCalls._ID);

        String limit = null;
        if (maxRecords > 0)
            limit = String.valueOf(maxRecords);
        return db.query(DbContract.LoggedCalls.TABLE_NAME, null, null, null, null, null, orderby, limit);
    }
}
