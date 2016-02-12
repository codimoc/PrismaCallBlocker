package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author ConteDiMonteCristo.
 */
public class LoggedCall {
    private static final String TAG = LoggedCall.class.getCanonicalName();

    private final long id;
    private final long runid;
    private final Date timestamp;
    private final String number;
    private final String description;
    private final long ruleid;


    public LoggedCall(long id, long runid, long ruleid, Date timestamp, String number, String description) {
        this.id = id;
        this.runid = runid;
        this.ruleid = ruleid;
        this.timestamp = timestamp;
        this.number = number;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public long getRunid() {
        return runid;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getNumber() {
        return number;
    }

    public String getDescription() {
        return description;
    }

    public long getRuleid() {
        return ruleid;
    }

    /**
     * Insert a new record (logged call) in the database
     * @param db the SQLite connection
     * @param runid the service run id
     * @param timestamp the current timestamp
     * @param number the calling number
     * @param description (nullable) the description like the contact name corresponding to the number
     * @param ruleid (nullable) the rule id if applicable
     * @return the new call id
     */
    public static long InsertRow(SQLiteDatabase db, long runid, Date timestamp, String number, String description, Integer ruleid) {
        ContentValues vals = new ContentValues();
        DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
        if (timestamp != null)
            vals.put(DbContract.LoggedCalls.COLUMN_NAME_TIMESTAMP, format.format(timestamp));
        else {
            Log.e(TAG, "Timestamp required for a logged call");
        }
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
        String orderby = String.format("%s desc",DbContract.LoggedCalls._ID);
        String limit = String.valueOf(maxRecords);
        return db.query(DbContract.LoggedCalls.TABLE_NAME, null, null, null, null, null, orderby, limit);
    }
}
