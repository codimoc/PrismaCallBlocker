package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author ConteDiMonteCristo.
 */
public class ServiceRun {

    private static final String TAG = ServiceRun.class.getCanonicalName();

    private final long id;
    private final int numTriggered;
    private final int numReceived;
    private final Date start;
    private final Date stop;

    private ServiceRun(long id, Date start, Date stop, int numReceived, int numTriggered) {
        this.id = id;
        this.start = start;
        this.stop = stop;
        this.numReceived = numReceived;
        this.numTriggered = numTriggered;
    }

    public long getId() {
        return id;
    }

    public int getNumReceived() {
        return numReceived;
    }

    public int getNumTriggered() {
        return numTriggered;
    }

    public Date getStart() {
        return start;
    }

    public Date getStop() {
        return stop;
    }


    /**
     * Find the latest run before the current one
     * @param db the SQLite connection
     * @return the new run id
     */
    public static ServiceRun LatestRun(SQLiteDatabase db) {
        String orderby = String.format("%s desc",DbContract.ServiceRuns._ID);
        String limit = "1";
        Cursor c = db.query(DbContract.ServiceRuns.TABLE_NAME, null, null, null, null, null, orderby, limit);
        if (c.getCount() > 0) {
            c.moveToFirst();
            long myId = c.getLong(c.getColumnIndexOrThrow(DbContract.ServiceRuns._ID));
            int myReceived  = c.getInt(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED));
            int myTriggered  = c.getInt(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED));
            Date myStart = null;
            Date myStop = null;
            try {

                DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
                String sstart = c.getString(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_START));
                String sstop = c.getString(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_STOP));
                if (sstart != null) myStart = format.parse(sstart);
                if (sstop != null) myStop = format.parse(sstop);
            } catch (ParseException e) {
                Log.e(TAG, e.getMessage());
            }
            return new ServiceRun(myId,myStart,myStop,myReceived,myTriggered);

        }
        return new ServiceRun(0,null,null,0,0);
    }

    public static Cursor LatestRuns(SQLiteDatabase db, int maxRecords) {
        String orderby = String.format("%s desc",DbContract.ServiceRuns._ID);
        String limit = String.valueOf(maxRecords);
        return db.query(DbContract.ServiceRuns.TABLE_NAME, null, null, null, null, null, orderby, limit);
    }

    /**
     * Insert a row in the serviceruns table
     * @param db the SQLite connection
     * @param start the starting time
     * @param stop the end time
     * @param numReceived the total number of calls received
     * @param numTriggered the total number of events triggered
     * @return the new run id
     */
    public static long InsertRow(SQLiteDatabase db, Date start, Date stop, int numReceived, int numTriggered) {
        ContentValues vals = new ContentValues();
        DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
        if (start != null)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_START,format.format(start));
        if (stop != null)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP,format.format(stop));
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, numReceived);
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, numTriggered);
        return db.insert(DbContract.ServiceRuns.TABLE_NAME, DbContract.ServiceRuns.COLUMN_NAME_STOP, vals);
    }

    /**
     * Update a row in the srviceruns table with the stop time and the number of calls and events triggered
     * @param db db the SQLite connection
     * @param runid the run id
     * @param stop the time when the service was stopped
     * @param numReceived the number of calls received during the service run
     * @param numTriggered the number of events triggered during the service run
     * @return the number of rows updated
     */
    private static int UpdateRow(SQLiteDatabase db, long runid, Date stop, int numReceived, int numTriggered) {
        ContentValues vals = new ContentValues();
        DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP,format.format(stop));
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, numReceived);
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, numTriggered);
        String selection = DbContract.ServiceRuns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(runid) };

        return db.update(DbContract.ServiceRuns.TABLE_NAME,vals,selection,selectionArgs);
    }

    /**
     * This should be called to initialize the run record on db at service start
     * @param db the SQLite connection
     * @return the run id
     */
    public static long InsertAtServiceStart(SQLiteDatabase db) {
        ServiceRun lrun = LatestRun(db);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date start = cal.getTime();
        return InsertRow(db, start, null, lrun.getNumReceived(), lrun.getNumTriggered());
    }

    /**
     * This should be called to complete a service run
     * @param db the SQLite connection
     * @param runid the run id
     * @param numReceived the number of calls received during the service run
     * @param numTriggered the number of events triggered during the service run
     */
    public static void UpdateAtServiceStop(SQLiteDatabase db, long runid, int numReceived, int numTriggered) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date end = cal.getTime();
        ServiceRun lrun = LatestRun(db);
        UpdateRow(db,runid, end,lrun.getNumReceived()+numReceived,lrun.getNumTriggered()+numTriggered);
    }

}
