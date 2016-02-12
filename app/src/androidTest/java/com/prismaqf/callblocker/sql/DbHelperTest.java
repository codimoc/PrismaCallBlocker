package com.prismaqf.callblocker.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        Date end = cal.getTime();
        ServiceRun.InsertRow(myDb,start,end,2,1);
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

    @Test
    public void GetLatestServiceRunWhenTableIsEmpty() {
        ServiceRun latest = ServiceRun.LatestRun(myDb);
        assertEquals("dummy id",0, latest.getId());
        assertEquals("received",0, latest.getNumReceived());
        assertEquals("triggered",0, latest.getNumTriggered());
    }

    @Test
    public void GetLatestServiceRunWhenTableIsNotEmpty() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        Date end = cal.getTime();
        ServiceRun.InsertRow(myDb,start,end,2,1);
        cal.add(Calendar.HOUR, 1);
        start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        end = cal.getTime();
        ServiceRun.InsertRow(myDb,start,end,4,1);
        ServiceRun latest = ServiceRun.LatestRun(myDb);
        assertEquals("id",2, latest.getId());
        assertEquals("received",4, latest.getNumReceived());
        assertEquals("triggered",1, latest.getNumTriggered());
    }

    @Test
    public void TestRecordWhenStartStoppingTheService() {
        //...when the service starts
        long id = ServiceRun.InsertAtServiceStart(myDb);
        //end of service
        ServiceRun.UpdateAtServiceStop(myDb,id,2,1);
        //service starts again
        id = ServiceRun.InsertAtServiceStart(myDb);
        //2nd end of service
        ServiceRun.UpdateAtServiceStop(myDb, id, 3, 1);
        ServiceRun latest = ServiceRun.LatestRun(myDb);
        //tests
        assertEquals("There should be two records",2, latest.getId());
        assertEquals("Total of 3 calls received",3, latest.getNumReceived());
        assertEquals("Total of 1 event triggered",1, latest.getNumTriggered());
    }
}