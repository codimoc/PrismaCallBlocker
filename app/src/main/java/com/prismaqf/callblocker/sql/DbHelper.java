package com.prismaqf.callblocker.sql;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.prismaqf.callblocker.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public static final int DATABASE_VERSION = 8;
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

        Cursor c = ServiceRun.LatestRuns(db,-1,false);
        List<ServiceRun> serviceRuns = new ArrayList<>();
        while (c.moveToNext())
            serviceRuns.add(ServiceRun.deserialize(c));

        c = LoggedCall.LatestCalls(db,-1,false);
        List<LoggedCall> loggedCalls = new ArrayList<>();
        while (c.moveToNext())
            loggedCalls.add(LoggedCall.deserialize(c));

/*
        c = CalendarRule.AllCalendarRules(db);
        List<CalendarRule> calendarRules= new ArrayList<>();
        while (c.moveToNext())
            calendarRules.add(CalendarRule.deserialize(c));
*/
        dropAllTables(db);

        onCreate(db);

        //and now reserialize
        for (ServiceRun run : serviceRuns)
            ServiceRun.serialize(db,run);
        for (LoggedCall lc : loggedCalls)
            LoggedCall.serialize(db, lc);
 /*       for (CalendarRule cr : calendarRules)
            CalendarRule.serialize(db, cr);
*/
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String msg = "The version has changed and no implentation was found for a downgrade policy";
        Log.e(TAG, msg);
        throw new SQLException(msg);
    }

    public void dropAllTables(SQLiteDatabase db) {
        String msg = String.format("Dropping all tables from DB %s",db.getPath());
        Log.w(TAG, msg);
        db.execSQL(DbContract.CalendarRules.SQL_DROP_TABLE);
        db.execSQL(DbContract.LoggedCalls.SQL_DROP_TABLE);
        db.execSQL(DbContract.ServiceRuns.SQL_DROP_TABLE);
    }


}
