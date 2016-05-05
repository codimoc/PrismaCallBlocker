package com.prismaqf.callblocker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.actions.DropCallByDownButton;
import com.prismaqf.callblocker.actions.Nothing;
import com.prismaqf.callblocker.filters.Filter;
import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.Always;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.rules.NoMatches;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;
import com.prismaqf.callblocker.utils.DebugDBFileName;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class FilterCreationTest {

    private static final String CAL_RULE = "my calendar rule";
    private static final String FIL_RULE = "my filter rule";

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @Before
    public void before() {
        Context myContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = new DbHelper(myContext).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, CAL_RULE);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE);
        CalendarRuleProvider.InsertRow(db, new CalendarRule(CAL_RULE, CalendarRule.makeMask(9), 1, 2, 23, 22));
        FilterRuleProvider.InsertRow(db, new FilterRule(FIL_RULE, FIL_RULE));
        db.close();
    }

    @After
    public void after() {
        Context myContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = new DbHelper(myContext).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, CAL_RULE);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE);
        db.close();
    }

    @Test
    public void CreationTest() throws SQLException, ReflectiveOperationException {
        Context myContext = InstrumentationRegistry.getTargetContext();
        String actionName = DropCallByDownButton.class.getCanonicalName();
        FilterHandle fh = new FilterHandle("myFilter",CAL_RULE,FIL_RULE,actionName);
        Filter f = Filter.makeFilter(myContext,fh);
        assertNotNull("The filter has been created", f);
    }

    @Test
    public void CreationTestWithDefaults() throws SQLException, ReflectiveOperationException {
        Context myContext = InstrumentationRegistry.getTargetContext();
        FilterHandle fh = new FilterHandle("myFilter",null,null,null);
        Filter f = Filter.makeFilter(myContext,fh);
        Assert.assertEquals("Default calendar", Always.class,f.getCalendarRule().getClass());
        Assert.assertEquals("Default filter rule", NoMatches.class,f.getFilterRule().getClass());
        Assert.assertEquals("Default action", Nothing.class,f.getAction().getClass());
    }

    @Test
    public void ParcelableTest() {
        FilterHandle fh1 = new FilterHandle("myFilter",CAL_RULE,FIL_RULE,DropCallByDownButton.class.getCanonicalName());
        Parcel parcel = Parcel.obtain();
        fh1.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        FilterHandle fh2 = FilterHandle.CREATOR.createFromParcel(parcel);
        assertEquals("Equal after Parcelization", fh1, fh2);
    }

}
