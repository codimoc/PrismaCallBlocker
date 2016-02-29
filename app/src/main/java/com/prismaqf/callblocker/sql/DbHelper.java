package com.prismaqf.callblocker.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prismaqf.callblocker.R;

import java.util.ArrayList;
import java.util.List;

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
    private static final int DATABASE_VERSION = 9;
    private static final String TAG = DbHelper.class.getCanonicalName();

    public DbHelper(Context context) {
        super(context, context.getString(R.string.db_file_name), null, DATABASE_VERSION);
    }

    public DbHelper(Context context, String dbname) {
        super(context, dbname, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.ServiceRuns.SQL_CREATE_TABLE);
        db.execSQL(DbContract.LoggedCalls.SQL_CREATE_TABLE);
        db.execSQL(DbContract.CalendarRules.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String msg = String.format("The DB version has changed from v.%d to v.%d and a destructive upgrade (drop/recreate) is performed",oldVersion,newVersion);
        Log.w(TAG, msg);

        Cursor c = ServiceRunProvider.LatestRuns(db, -1, false);
        List<ServiceRunProvider.ServiceRun> serviceRuns = new ArrayList<>();
        while (c.moveToNext())
            serviceRuns.add(ServiceRunProvider.deserialize(c));

        c = LoggedCallProvider.LatestCalls(db, -1, false);
        List<LoggedCallProvider.LoggedCall> loggedCalls = new ArrayList<>();
        while (c.moveToNext())
            loggedCalls.add(LoggedCallProvider.deserialize(c));

/*
        c = CalendarRuleProvider.AllCalendarRules(db);
        List<CalendarRuleProvider> calendarRules= new ArrayList<>();
        while (c.moveToNext())
            calendarRules.add(CalendarRuleProvider.deserialize(c));
*/
        dropAllTables(db);

        onCreate(db);

        //and now reserialize
        for (ServiceRunProvider.ServiceRun run : serviceRuns)
            ServiceRunProvider.serialize(db, run);
        for (LoggedCallProvider.LoggedCall lc : loggedCalls)
            LoggedCallProvider.serialize(db, lc);
 /*       for (CalendarRuleProvider cr : calendarRules)
            CalendarRuleProvider.serialize(db, cr);
*/
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
        db.execSQL(DbContract.CalendarRules.SQL_DROP_TABLE);
        db.execSQL(DbContract.LoggedCalls.SQL_DROP_TABLE);
        db.execSQL(DbContract.ServiceRuns.SQL_DROP_TABLE);
    }


}
