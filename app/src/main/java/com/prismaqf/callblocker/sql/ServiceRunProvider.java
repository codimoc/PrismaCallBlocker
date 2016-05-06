package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.utils.PreferenceHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author ConteDiMonteCristo.
 */
public class ServiceRunProvider {

    public static class ServiceRun {
        private final long runId;
        private final int numTriggered;
        private final int numReceived;
        private final Date start;
        private final Date stop;

        public ServiceRun(long runId, Date start, Date stop, int numReceived, int numTriggered) {
            this.runId = runId;
            this.start = start;
            this.stop = stop;
            this.numReceived = numReceived;
            this.numTriggered = numTriggered;
        }
        public long getId() {return runId;}

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

    }

    private static final String TAG = ServiceRunProvider.class.getCanonicalName();
    private static final String RUNNING = "running";



    public static ServiceRun deserialize(Cursor c) {
        long runId = c.getInt(c.getColumnIndexOrThrow(DbContract.ServiceRuns._ID));
        int received  = c.getInt(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED));
        int triggered  = c.getInt(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED));
        Date start = null;
        Date stop = null;
        try {

            DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
            String sstart = c.getString(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_START));
            String sstop = c.getString(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_STOP));
            if (sstart != null) start = format.parse(sstart);
            if (sstop != null && !sstop.equals(RUNNING)) stop = format.parse(sstop);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            //throw new SQLException(e.getMessage());
        }
        return new ServiceRun(runId, start,stop,received,triggered);
    }

    public static void serialize(SQLiteDatabase db, ServiceRun sr) {
        InsertRow(db,sr);
    }



    /**
     * Find the latest run before the current one
     * @param db the SQLite connection
     * @return the new run id
     */
    public static synchronized ServiceRun LatestRun(SQLiteDatabase db) {
        String orderby = String.format("%s desc",DbContract.ServiceRuns._ID);
        String limit = "1";
        Cursor c = db.query(DbContract.ServiceRuns.TABLE_NAME, null, null, null, null, null, orderby, limit);
        if (c.getCount() > 0) {
            c.moveToFirst();
            return deserialize(c);
        }
        return new ServiceRun(0,null,null,0,0);
    }

    /**
     * Find the latest run before the current one
     * @param db the SQLite connection
     * @return the new run id
     */
    public static synchronized ServiceRun LatestCompletedRun(SQLiteDatabase db) {
        String orderby = String.format("%s desc",DbContract.ServiceRuns._ID);
        String limit = "1";
        String selection = DbContract.ServiceRuns.COLUMN_NAME_STOP + "<> ?";
        String[] selectionArgs = { RUNNING };
        Cursor c = db.query(DbContract.ServiceRuns.TABLE_NAME, null, selection, selectionArgs, null, null, orderby, limit);
        if (c.getCount() > 0) {
            c.moveToFirst();
            return deserialize(c);
        }
        return new ServiceRun(0,null,null,0,0);
    }

    /**
     * Retrieves the latest service runs
     * @param db the SQLite connection
     * @param maxRecords the total number of records returned
     * @return a cursor
     */
    public static synchronized Cursor LatestRuns(SQLiteDatabase db, int maxRecords) {
        return LatestRuns(db, maxRecords, true);
    }

    /**
     * Retrieves the latest service runs
     * @param db the SQLite connection
     * @param maxRecords the total number of records returned
     * @param descending a flag to inicate the sorting order, descending when the flag is true
     * @return a cursor
     */
    public static synchronized Cursor LatestRuns(SQLiteDatabase db, int maxRecords, boolean descending) {
        String orderby;
        if (descending)
            orderby= String.format("%s desc",DbContract.ServiceRuns._ID);
        else
            orderby= String.format("%s asc",DbContract.ServiceRuns._ID);

        String limit = null;
        if (maxRecords > 0)
            limit = String.valueOf(maxRecords);

        return db.query(DbContract.ServiceRuns.TABLE_NAME, null, null, null, null, null, orderby, limit);
    }

    /**
     * Insert a row in the serviceruns table
     * @param db the SQLite connection
     * @param sr the service run
     * @return the new run id
     */
    public static synchronized long InsertRow(SQLiteDatabase db, ServiceRun sr) {
        ContentValues vals = new ContentValues();
        DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
        if (sr.getStart() != null)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_START,format.format(sr.getStart()));
        if (sr.getStop() != null)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP,format.format(sr.getStop()));
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, sr.getNumReceived());
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, sr.getNumTriggered());
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
    private static synchronized int UpdateRow(SQLiteDatabase db, long runid, Date stop, int numReceived, int numTriggered) {
        ContentValues vals = new ContentValues();
        DateFormat format = new SimpleDateFormat(DbContract.DATE_FORMAT, Locale.getDefault());
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP,format.format(stop));
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, numReceived);
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, numTriggered);
        String selection = DbContract.ServiceRuns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(runid) };

        return db.update(DbContract.ServiceRuns.TABLE_NAME, vals, selection, selectionArgs);
    }

    /**
     * Update a row in the srviceruns table while running the service
     * @param db db the SQLite connection
     * @param runid the run id
     * @param numReceived the number of calls received during the service run (negative number to skip this value update)
     * @param numTriggered the number of events triggered during the service run (negative number to skip this value update)
     */
    public static synchronized void UpdateWhileRunning(SQLiteDatabase db, long runid, int numReceived, int numTriggered) {
        ContentValues vals = new ContentValues();
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP, RUNNING);
        if (numReceived >= 0)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, numReceived);
        if (numTriggered >=0)
            vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, numTriggered);
        String selection = DbContract.ServiceRuns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(runid) };

        db.update(DbContract.ServiceRuns.TABLE_NAME,vals,selection,selectionArgs);
    }

    /**
     * This should be called to initialize the run record on db at service start
     * @param db the SQLite connection
     * @return the run id
     */
    public static synchronized long InsertAtServiceStart(SQLiteDatabase db) {
        ServiceRun lrun = LatestRun(db);
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date start = cal.getTime();
        return InsertRow(db, new ServiceRun(lrun.getId()+1, start, null, 0, 0));
    }

    /**
     * This should be called to complete a service run
     * @param db the SQLite connection
     * @param runid the run id
     * @param numReceived the number of calls received during the service run
     * @param numTriggered the number of events triggered during the service run
     */
    public static synchronized void UpdateAtServiceStop(SQLiteDatabase db, long runid, int numReceived, int numTriggered) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date end = cal.getTime();
        UpdateRow(db,runid, end,numReceived,numTriggered);
    }

    private static synchronized void DeleteServiceRun(SQLiteDatabase db, long runid) {
        String where = DbContract.ServiceRuns._ID + " = ?";
        String[] args = {String.valueOf(runid)};
        db.delete(DbContract.ServiceRuns.TABLE_NAME, where, args);
    }

    public static synchronized int PurgeLog(SQLiteDatabase db, Context context, String longevity) {
        String theLongevity = longevity!=null ? longevity : PreferenceHelper.GetLogLongevity(context);
        if (theLongevity.equals("no limit")) return 0;
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTime(new Date());
        if (theLongevity.contains("day")) cal.add(Calendar.DATE,-1);
        else if (theLongevity.contains("week")) cal.add(Calendar.DATE,-7);
        else if (theLongevity.contains("month")) cal.add(Calendar.MONTH,-1);
        else if (theLongevity.contains("year")) cal.add(Calendar.YEAR,-1);
        //now delete:
        //1. first create a map of records to delete
        Set<Long> candidates = new HashSet<>();
        Cursor c = LatestRuns(db,-1,false); //everything in ascending order
        while (c.moveToNext()) {
            ServiceRun r = deserialize(c);
            if (r.getStop() != null && r.getStop().before(cal.getTime()))
                candidates.add(r.getId());
        }
        c.close();
        //2. now delete service runs and associated calls
        for (long id : candidates) {
            DeleteServiceRun(db,id);
            LoggedCallProvider.DeleteLoggedCallInRun(db,id);
        }
        return candidates.size();
    }


}
