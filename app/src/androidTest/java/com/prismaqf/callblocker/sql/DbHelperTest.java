package com.prismaqf.callblocker.sql;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;


import com.prismaqf.callblocker.rules.CalendarRule;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DbHelperTest {

    private static final String DB_NAME = "unitTest.db";

    private SQLiteDatabase myDb;

    @Before
    public void before() {
        Context myContext = InstrumentationRegistry.getTargetContext();
        DbHelper myDbHelper = new DbHelper(myContext, DB_NAME);
        myDb = myDbHelper.getWritableDatabase();
        myDb.delete(DbContract.ServiceRuns.TABLE_NAME,null,null);
        myDb.delete(DbContract.LoggedCalls.TABLE_NAME,null,null);
        myDb.delete(DbContract.CalendarRules.TABLE_NAME,null,null);
    }

    @Test
    public void dbSmokeTest() {
        assertEquals("DB version", 9, myDb.getVersion());
    }

    @Test
    public void ServiceRunsInsertRow() {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        Date end = cal.getTime();
        ServiceRunProvider.InsertRow(myDb, new ServiceRunProvider.ServiceRun(-1,start, end, 2, 1));
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
        ServiceRunProvider.ServiceRun latest = ServiceRunProvider.LatestRun(myDb);
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
        ServiceRunProvider.InsertRow(myDb, new ServiceRunProvider.ServiceRun(-1, start, end, 2, 1));
        cal.add(Calendar.HOUR, 1);
        start = cal.getTime();
        cal.add(Calendar.HOUR, 1);
        end = cal.getTime();
        ServiceRunProvider.InsertRow(myDb, new ServiceRunProvider.ServiceRun(-1,start, end, 4, 1));
        ServiceRunProvider.ServiceRun latest = ServiceRunProvider.LatestRun(myDb);
        assertEquals("id",2, latest.getId());
        assertEquals("received",4, latest.getNumReceived());
        assertEquals("triggered",1, latest.getNumTriggered());
    }

    @Test
    public void TestRecordWhenStartStoppingTheService() {
        //...when the service starts
        long id = ServiceRunProvider.InsertAtServiceStart(myDb);
        //end of service
        ServiceRunProvider.UpdateAtServiceStop(myDb, id, 2, 1);
        //service starts again
        id = ServiceRunProvider.InsertAtServiceStart(myDb);
        //2nd end of service
        ServiceRunProvider.UpdateAtServiceStop(myDb, id, 3, 1);
        ServiceRunProvider.ServiceRun latest = ServiceRunProvider.LatestRun(myDb);
        //tests
        assertEquals("There should be two records",2, latest.getId());
        assertEquals("Total of 3 calls received",3, latest.getNumReceived());
        assertEquals("Total of 1 event triggered",1, latest.getNumTriggered());
    }

    @Test
    public void LoggedCallsInsertRows() {
        LoggedCallProvider.InsertRow(myDb, new LoggedCallProvider.LoggedCall(15,-1, "123", "a dummy"));
        LoggedCallProvider.InsertRow(myDb, new LoggedCallProvider.LoggedCall(21,-1, "321", "another dummy"));
        Cursor c = LoggedCallProvider.LatestCalls(myDb, 5);
        assertEquals("There should be two records",2,c.getCount());
    }

    @Test
    public void LoggedCallsLatest() {
        LoggedCallProvider.InsertRow(myDb, new LoggedCallProvider.LoggedCall(15,-1, "123", "a dummy"));
        LoggedCallProvider.InsertRow(myDb, new LoggedCallProvider.LoggedCall(21,1, "321", "another dummy"));
        Cursor c = LoggedCallProvider.LatestCalls(myDb, 5);
        //they should appear in reverse order
        c.moveToFirst();
        assertEquals("Check the second run id", 21, c.getInt(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_RUNID)));
        assertEquals("Check the second number","321", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_NUMBER)));
        assertEquals("Check the second description", "another dummy", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION)));
        assertNotNull("The second rule id is not null", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_RULEID)));
        assertEquals("Check the second rule id", 1, c.getInt(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_RULEID)));
        c.moveToNext();
        assertEquals("Check the first run id", 15, c.getInt(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_RUNID)));
        assertEquals("Check the first number", "123", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_NUMBER)));
        assertEquals("Check the first description", "a dummy", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION)));
        assertNull("The first rule id is null", c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_RULEID)));
    }

    @Test
    public void LoggedCallCheckTimeStamp() {
        LoggedCallProvider.InsertRow(myDb, new LoggedCallProvider.LoggedCall(15,-1, "123", "a dummy"));
        Cursor c = LoggedCallProvider.LatestCalls(myDb, 5);
        c.moveToFirst();
        String ts = c.getString(c.getColumnIndex(DbContract.LoggedCalls.COLUMN_NAME_TIMESTAMP));
        assertNotNull("The timestamp is not null", ts);
        assertFalse("The timestamp is not empty", ts.isEmpty());
    }

    @Test
    public void InsertCalendarRule(){
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5,45,21,12));
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("second", CalendarRule.makeMask(96)));
        Cursor c = CalendarRuleProvider.AllCalendarRules(myDb);
        assertEquals("There should be two records", 2, c.getCount());
    }

    @Test
    public void RetrieveCalendarRules() {
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5,45,21,12));
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("second", CalendarRule.makeMask(96)));
        Cursor c = CalendarRuleProvider.AllCalendarRules(myDb);
        c.moveToFirst();
        assertEquals("Name of first", "first", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME)));
        assertEquals("Mask of first",9,c.getInt(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_DAYMASK)));
        assertEquals("From of first", "05:45", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FROM)));
        assertEquals("To of first", "21:12", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TO)));
        assertEquals("Format of first", "Days M--T--- from 05:45 to 21:12", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FORMAT)));
        c.moveToNext();
        assertEquals("Name of second", "second", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME)));
        assertEquals("Mask of second", 96, c.getInt(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_DAYMASK)));
        assertEquals("From of second", "00:00", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FROM)));
        assertEquals("To of second","23:59",c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TO)));
        assertEquals("Format of second", "Days -----SS from 00:00 to 23:59", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FORMAT)));
    }

    @Test
    public void UpdateCalendarRule() {
        long id =  CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5, 45, 21, 12));
        CalendarRuleProvider.UpdateCalendarRule(myDb, id, new CalendarRule("first",CalendarRule.makeMask(9), 6,5,21,12));
        Cursor c = CalendarRuleProvider.AllCalendarRules(myDb);
        assertEquals("There should be one record1", 1, c.getCount());
        c.moveToFirst();
        assertEquals("Name of first", "first", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_RULENAME)));
        assertEquals("Mask of first",9,c.getInt(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_DAYMASK)));
        assertEquals("From of first", "06:05", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_FROM)));
        assertEquals("To of first", "21:12", c.getString(c.getColumnIndexOrThrow(DbContract.CalendarRules.COLUMN_NAME_TO)));
    }

    @Test
    public void TestAllNames() {
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5,45,21,12));
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("second", CalendarRule.makeMask(96)));
        List<String> names = CalendarRuleProvider.AllRuleNames(myDb);
        assertEquals("Two names found",2,names.size());
        assertTrue("Name first found", names.contains("first"));
        assertTrue("Name second found", names.contains("second"));
    }

    @Test
    public void CalendarRuleCheckTimeStamp() {
        CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5,45,21,12));
        Cursor c = CalendarRuleProvider.LatestCalendarRules(myDb, 1, true);
        c.moveToFirst();
        String ts = c.getString(c.getColumnIndex(DbContract.CalendarRules.COLUMN_NAME_TIMESTAMP));
        assertNotNull("The timestamp is not null", ts);
        assertFalse("The timestamp is not empty", ts.isEmpty());
    }


    @Test
    public void MakeRuleFromSql() throws Exception {
        long id =  CalendarRuleProvider.InsertRow(myDb, new CalendarRule("first", CalendarRule.makeMask(9), 5,45,21,12));
        CalendarRule theRule = CalendarRuleProvider.FindCalendarRule(myDb, id);
        assertEquals("The name","first",theRule.getName());
        assertEquals("DayMask", 9, theRule.getBinaryMask());
        assertEquals("Start Hour", 5, theRule.getStartHour());
        assertEquals("Start Min", 45, theRule.getStartMin());
        assertEquals("End Hour", 21, theRule.getEndHour());
        assertEquals("Start Hour", 12, theRule.getEndMin());
    }
}