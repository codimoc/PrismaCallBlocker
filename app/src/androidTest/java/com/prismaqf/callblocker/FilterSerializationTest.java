package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.actions.DropCallByDownButton;
import com.prismaqf.callblocker.actions.DropCallByEndCall;
import com.prismaqf.callblocker.filters.Filter;
import com.prismaqf.callblocker.filters.FilterHandle;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterProvider;
import com.prismaqf.callblocker.sql.FilterRuleProvider;
import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.InstrumentTestHelper;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ConteDiMonteCristo on 28/12/16.
 */

@RunWith(AndroidJUnit4.class)
public class FilterSerializationTest {

    private static final String CAL_RULE = "my calendar rule";
    private static final String FIL_RULE_1 = "first filter rule";
    private static final String FIL_RULE_2 = "second filter rule";
    private static final String FILTER_1 = "first filter";
    private static final String FILTER_2 = "second filter";

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @Rule
    public final ActivityTestRule<CallBlockerManager> mActivityRule = new ActivityTestRule<>(CallBlockerManager.class);


    @Before
    public void before() {
        Context myContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = new DbHelper(myContext).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, CAL_RULE);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_1);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_2);
        CalendarRuleProvider.InsertRow(db, new CalendarRule(CAL_RULE, CalendarRule.makeMask(9), 1, 2, 23, 22));
        FilterRule fr1 = new FilterRule(FIL_RULE_1, FIL_RULE_1);
        fr1.addPattern("123");
        FilterRuleProvider.InsertRow(db,fr1);
        FilterRule fr2 = new FilterRule(FIL_RULE_2, FIL_RULE_2);
        fr1.addPattern("456");
        FilterRuleProvider.InsertRow(db,fr2);
        db.close();
        Activity myActivity = mActivityRule.getActivity();
        /*       Intent intent = new Intent(myActivity, CallDetectService.class);
        myActivity.stopService(intent);*/
        if (!CallBlockerManager.isServiceRunning(myActivity)) {
            onView(withId(R.id.buttonDetectToggle)).perform(click());
        }
    }

    @After
    public void after() {
        Context myContext = InstrumentationRegistry.getTargetContext();
        SQLiteDatabase db = new DbHelper(myContext).getWritableDatabase();
        CalendarRuleProvider.DeleteCalendarRule(db, CAL_RULE);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_1);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_2);
        db.close();
        Activity myActivity = mActivityRule.getActivity();
        /*       Intent intent = new Intent(myActivity, CallDetectService.class);
        myActivity.stopService(intent);*/
        if (CallBlockerManager.isServiceRunning(myActivity)) {
            onView(withId(R.id.buttonDetectToggle)).perform(click());
        }
    }


    @Test
    public void SerializationTest() throws IOException, ReflectiveOperationException, SQLException {
        Context myContext = InstrumentationRegistry.getTargetContext();
        String actionName1 = DropCallByDownButton.class.getCanonicalName();
        String actionName2 = DropCallByEndCall.class.getCanonicalName();
        FilterHandle fh1 = new FilterHandle("myFilter1",CAL_RULE,FIL_RULE_1,actionName1);
        FilterHandle fh2 = new FilterHandle("myFilter2",CAL_RULE,FIL_RULE_2,actionName2);
        SQLiteDatabase db = new DbHelper(myContext).getWritableDatabase();
        FilterProvider.InsertRow(db,fh1);
        FilterProvider.InsertRow(db,fh2);
        assertEquals("Two filters are present in the db",2,FilterProvider.AllFilters(db).size());
        Activity act = InstrumentTestHelper.getCurrentActivity();
        List<Filter> myFilters = CallHelper.GetHelper(act).getFilters(act);
        assertEquals("Two filters in memory",2,myFilters.size());
        ApplicationInfo ai = myContext.getApplicationInfo();
        String filePath = ai.dataDir + "/test.bin";
        File f = new File(filePath);
        assertFalse("The file was not there at first",f.delete());
        //now serialize
        FileOutputStream fOut = new FileOutputStream(f);
        ObjectOutputStream oOut = new ObjectOutputStream(fOut);
        oOut.writeObject(myFilters);
        oOut.flush();
        oOut.close();
        //now roundtrip, remove filters from db and desrialize
        CalendarRuleProvider.DeleteCalendarRule(db, CAL_RULE);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_1);
        FilterRuleProvider.DeleteFilterRule(db, FIL_RULE_2);
        FilterProvider.DeleteFilter(db,"myFilter1");
        FilterProvider.DeleteFilter(db,"myFilter2");
        assertEquals("No filters are present in the db",0,FilterProvider.AllFilters(db).size());
        //now deserialize
        FileInputStream fIn = new FileInputStream(f);
        ObjectInputStream oIn = new ObjectInputStream(fIn);
        List<Filter> myFiltersIn = (List<Filter>) oIn.readObject();
        oIn.close();
        assertEquals("Two deserialized filters",2,myFiltersIn.size());
        //now save to db
        for(Filter ff : myFiltersIn) {
            FilterProvider.SaveFilter(db,ff);
        }
        assertEquals("Two filters are present in the db",2,FilterProvider.AllFilters(db).size());
        FilterHandle fh1_in = FilterProvider.FindFilter(db,"myFilter1");
        FilterHandle fh2_in = FilterProvider.FindFilter(db,"myFilter2");
        Filter f1 = Filter.makeFilter(myContext,fh1_in);
        Filter f2 = Filter.makeFilter(myContext,fh2_in);
        assertEquals("Deserialized first filter",myFilters.get(0),f1);
        assertEquals("Deserialized second filter",myFilters.get(1),f2);

        assertTrue("Now the file exists",f.exists());
        assertTrue("I can delete",f.delete());
        db.close();
    }
}
