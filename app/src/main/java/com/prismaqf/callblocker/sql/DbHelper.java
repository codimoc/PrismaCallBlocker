package com.prismaqf.callblocker.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prismaqf.callblocker.R;
import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.utils.DebugKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helper class extending SQLiteOpenHelper to manage the DB in Android fashion
 * @author ConteDiMonteCristo
 */
public class DbHelper extends SQLiteOpenHelper{

    /**
     * When changing the DB schema, one should up the version number.
     * At this stage it is important to properly implement onUpgrade and
     * and onDowngrade which otherwise might be called. In the current
     * implementation they throw an exception becase a single version is
     * assumed. The proper implentation should try to preserve the data
     */
    private static final int DATABASE_VERSION = 14;
    private static final String TAG = DbHelper.class.getCanonicalName();

    private static String debugDb = null;
    private static final Object lock = new Object();

    /**
     * To force locking write operation from outside
     * @return the lock object
     */
    public static Object getDbHelperLock() {return lock;}

    public DbHelper(Context context) {
        super(context, debugDb==null? context.getString(R.string.db_file_name) : debugDb, null, DATABASE_VERSION);
    }

    public DbHelper(Context context, String dbname) {
        super(context, dbname, null, DATABASE_VERSION);
    }

    public synchronized static void SetDebugDb(DebugKey.DbKey key, String dbname) {
        debugDb = dbname;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        synchronized (lock) {
            db.execSQL(DbContract.ServiceRuns.SQL_CREATE_TABLE);
            db.execSQL(DbContract.LoggedCalls.SQL_CREATE_TABLE);
            db.execSQL(DbContract.CalendarRules.SQL_CREATE_TABLE);
            db.execSQL(DbContract.FilterRules.SQL_CREATE_TABLE);
            db.execSQL(DbContract.FilterPatterns.SQL_CREATE_TABLE);
            db.execSQL(DbContract.Filters.SQL_CREATE_TABLE);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        synchronized (lock) {
            String msg = String.format(Locale.getDefault(),"The DB version has changed from v.%d to v.%d and a destructive upgrade (drop/recreate) is performed",oldVersion,newVersion);
            Log.w(TAG, msg);

            Cursor c = ServiceRunProvider.LatestRuns(db, -1, false);
            List<ServiceRunProvider.ServiceRun> serviceRuns = new ArrayList<>();
            while (c.moveToNext())
                serviceRuns.add(ServiceRunProvider.deserialize(c));

            c = LoggedCallProvider.LatestCalls(db, -1, false);
            List<LoggedCallProvider.LoggedCall> loggedCalls = new ArrayList<>();
            while (c.moveToNext())
                loggedCalls.add(LoggedCallProvider.deserialize(c));


            c = CalendarRuleProvider.AllCalendarRules(db);
            List<CalendarRule> calendarRules= new ArrayList<>();
            while (c.moveToNext())
                calendarRules.add(CalendarRuleProvider.deserialize(c));

            List<FilterRule> filterRules = FilterRuleProvider.AllFilterRules(db);

            List<FilterHandle> filters = FilterProvider.AllFilters(db);

            dropAllTables(db);
            onCreate(db);

            //and now reserialize
            for (ServiceRunProvider.ServiceRun run : serviceRuns)
                ServiceRunProvider.serialize(db, run);
            for (LoggedCallProvider.LoggedCall lc : loggedCalls)
                LoggedCallProvider.serialize(db, lc);
            for (CalendarRule cr : calendarRules)
                CalendarRuleProvider.serialize(db, cr);
            for (FilterRule fr : filterRules)
                FilterRuleProvider.InsertRow(db, fr);
            for (FilterHandle fh : filters)
                FilterProvider.InsertRow(db, fh);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String msg = "The version has changed and no implentation was found for a downgrade policy";
        Log.e(TAG, msg);
        throw new SQLException(msg);
    }

    private void dropAllTables(SQLiteDatabase db) {
        String msg = String.format("Dropping all tables from DB %s",db.getPath());
        Log.w(TAG, msg);

        synchronized (lock) {
            db.execSQL(DbContract.Filters.SQL_DROP_TABLE);
            db.execSQL(DbContract.FilterPatterns.SQL_DROP_TABLE);
            db.execSQL(DbContract.FilterRules.SQL_DROP_TABLE);
            db.execSQL(DbContract.CalendarRules.SQL_DROP_TABLE);
            db.execSQL(DbContract.LoggedCalls.SQL_DROP_TABLE);
            db.execSQL(DbContract.ServiceRuns.SQL_DROP_TABLE);
        }

    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        //so that if the program acquires the lock
        //when doing backup, the DB can not be written
        synchronized (lock) {
            return super.getWritableDatabase();
        }
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        //so that if the program acquires the lock
        //when doing backup, the DB can not be written
        synchronized (lock) {
            return super.getReadableDatabase();
        }
    }
}
