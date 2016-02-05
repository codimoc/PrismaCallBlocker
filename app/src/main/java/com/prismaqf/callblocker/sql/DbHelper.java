package com.prismaqf.callblocker.sql;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

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
    public static final int DATABASE_VERSION = 1;
    public static final String TAG = DbHelper.class.getCanonicalName();

    public DbHelper(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.ServiceRuns.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String msg = "The version has changed and no implentation was found for an upgrade policy";
        Log.e(TAG, msg);
        throw new SQLException(msg);
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
        db.execSQL(DbContract.ServiceRuns.SQL_DROP_TABLE);
    }


}
