package com.prismaqf.callblocker;

import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;

/**
 * Fragment for editing calendar rules
 * @author ConteDiMonteCristo
 */
public class CalendarRulesFragment extends EditListFragment{

    private final String TAG = CalendarRulesFragment.class.getCanonicalName();
    private static final int URL_LOADER = 2; // Identifies a particular Loader being used in this component

    @Override
    public SimpleCursorAdapter getAdapter() {
        return new SimpleCursorAdapter(getActivity(),
                                       R.layout.calendar_rule_record,
                                       null,  //no cursor yet
                                       new String[] {DbContract.CalendarRules.COLUMN_NAME_RULENAME,
                                                     DbContract.CalendarRules.COLUMN_NAME_FORMAT},
                                       new int[] {R.id.text_rule_name,
                                                  R.id.text_rule_format}, 0);
    }
    @Override
    public void initLoader() {
        getLoaderManager().initLoader(URL_LOADER, null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(getActivity(),   // Parent activity context
                                        null,   // All the following params
                                        null,   // are null
                                        null,   // because the Cursor is created
                                        null,   // below
                                        null)
                {
                    @Override
                    public Cursor loadInBackground() {
                        return CalendarRuleProvider.AllCalendarRules(myDbConnection);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        final long ruleid = id;
        new Thread(new Runnable() {
            @Override
            public void run() {
               SQLiteDatabase db = new DbHelper(getActivity()).getReadableDatabase();
                try {
                    CalendarRule rule = CalendarRuleProvider.FindCalendarRule(db, ruleid);
                    Intent intent = new Intent(getActivity(),NewEditCalendarRule.class);
                    intent.putExtra(NewEditCalendarRule.ACTION_KEY,NewEditCalendarRule.ACTION_UPDATE);
                    intent.putExtra(NewEditCalendarRule.KEY_ORIG,rule);
                    intent.putExtra(NewEditCalendarRule.KEY_RULEID,ruleid);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
                finally {
                    db.close();
                }
            }
        }).start();
    }
}
