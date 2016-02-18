package com.prismaqf.callblocker;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.CalendarRule;
import com.prismaqf.callblocker.sql.DbContract;

/**
 * Activity to show a edit the list of calendar rules
 * with day mask and time window
 * @author ConteDiMonteCristo
 */

public class ShowCalendarRules extends ShowListActivity {

    private final String TAG = ShowCalendarRules.class.getCanonicalName();
    private static final int URL_LOADER = 2; // Identifies a particular Loader being used in this component


    @Override
    public SimpleCursorAdapter getAdapter() {
        return new SimpleCursorAdapter(this,
                R.layout.calendar_rule_record,
                null,  //no cursor yet
                new String[] {DbContract.CalendarRules.COLUMN_NAME_RULENAME,
                              DbContract.CalendarRules.COLUMN_NAME_FORMAT},
                new int[] {R.id.text_rule_name,
                           R.id.text_rule_format}, 0);
    }

    @Override
    public void initLoader() {
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(this,   // Parent activity context
                        null,   // All the following params
                        null,   // are null
                        null,   // because the Cursor is created
                        null,   // below
                        null)
                {
                    @Override
                    public Cursor loadInBackground() {
                        return CalendarRule.AllCalendarRules(myDbConnection);
                    }
                };
            default:
                return null;
        }
    }
}
