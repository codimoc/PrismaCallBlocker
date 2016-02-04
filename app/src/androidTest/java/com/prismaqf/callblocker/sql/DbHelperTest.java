package com.prismaqf.callblocker.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DbHelperTest {

    private static final String DB_NAME = "unitTest.db";

    private Context myContext;
    private DbHelper myDbHelper;
    private SQLiteDatabase myDb;

    @Before
    public void before() {
        myContext = InstrumentationRegistry.getTargetContext();
        myDbHelper = new DbHelper(myContext,DB_NAME);
        myDb = myDbHelper.getWritableDatabase();
        myDb.delete(DbContract.ServiceRuns.TABLE_NAME,null,null);
    }

    @Test
    public void dbSmokeTest() {
        assertEquals("DB version", 1, myDb.getVersion());
    }

    @Test
    public void ServiceRunsInsertRow() {
        ContentValues vals = new ContentValues();
        Calendar cal = Calendar.getInstance();
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        Date end = cal.getTime();
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_START,start.toString());
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_STOP,start.toString());
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED, 2);
        vals.put(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED, 1);
        long rid = myDb.insert(DbContract.ServiceRuns.TABLE_NAME,
                               DbContract.ServiceRuns.COLUMN_NAME_STOP,
                               vals);
        Cursor c = myDb.rawQuery("select * from serviceruns", null);
        assertEquals("one row expected", 1, c.getCount());
        c.moveToFirst();
        long id = c.getLong(c.getColumnIndexOrThrow(DbContract.ServiceRuns._ID));
        assertEquals("First id equal to 1", 1, id);
        long received  = c.getLong(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED));
        assertEquals("Expected calls received",2,received);
        long triggered  = c.getLong(c.getColumnIndexOrThrow(DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED));
        assertEquals("Expected calls triggered",1,triggered);
    }
}