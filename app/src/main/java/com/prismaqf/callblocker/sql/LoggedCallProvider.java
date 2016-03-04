package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * @author ConteDiMonteCristo.
 */
public class LoggedCallProvider {

    private static final String TAG = LoggedCallProvider.class.getCanonicalName();

    public static class LoggedCall{
        private final long runid;
        private final String number;
        private final String description;
        private final int ruleid;


        public LoggedCall(long runid, int ruleid, String number, String description) {
            this.runid = runid;
            this.ruleid = ruleid;
            this.number = number;
            this.description = description;
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

    }

    public static LoggedCall deserialize(Cursor c) {
        long runId = c.getLong(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RUNID));
        int ruleId = -1;
        if (!c.isNull(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RULEID)))
            ruleId = c.getInt(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_RULEID));
        String number = c.getString(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_NUMBER));
        String description = c.getString(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION));
        return new LoggedCall(runId,ruleId,number,description);
    }

    public static void serialize(SQLiteDatabase db, LoggedCall lc) {
        InsertRow(db,lc);
    }

    /**
     * Insert a new record (logged call) in the database
     * @param db the SQLite connection
     * @param lc the logged call
     * @return the new call id
     */
    public static synchronized long InsertRow(SQLiteDatabase db, LoggedCall lc) {
        ContentValues vals = new ContentValues();
        vals.put(DbContract.LoggedCalls.COLUMN_NAME_RUNID,lc.getRunid());
        vals.put(DbContract.LoggedCalls.COLUMN_NAME_NUMBER,lc.getNumber());
        if (lc.getDescription() != null)
            vals.put(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION,lc.getDescription());
        if (lc.getRuleid() > 0)
            vals.put(DbContract.LoggedCalls.COLUMN_NAME_RULEID,lc.getRuleid());
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
    public static synchronized Cursor LatestCalls(SQLiteDatabase db, int maxRecords, boolean descending) {
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
