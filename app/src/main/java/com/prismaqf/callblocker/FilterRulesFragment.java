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

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

/**
 * Fragment for editing filter rules
 * @author ConteDiMonteCristo
 */
public class FilterRulesFragment extends EditCursorListFragment {

    private final String TAG = FilterRulesFragment.class.getCanonicalName();
    private static final int URL_LOADER = 3; // Identifies a particular Loader being used in this component

    @Override
    protected SimpleCursorAdapter getAdapter() {
        return new SimpleCursorAdapter(getActivity(),
                                       R.layout.filter_rule_record,
                                       null,  //no cursor yet
                                       new String[] {DbContract.FilterRules.COLUMN_NAME_RULENAME,
                                                     DbContract.FilterRules.COLUMN_NAME_DESCRIPTION},
                                       new int[] {R.id.text_rule_name,
                                                  R.id.text_rule_description},
                                       0);
    }

    @Override
    protected void initLoader() {
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
                        null) {
                    @Override
                    public Cursor loadInBackground() {
                        return myDbConnection.query(DbContract.FilterRules.TABLE_NAME,
                                null, null, null, null, null, null, null);
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
                    FilterRule rule = FilterRuleProvider.FindFilterRule(db, ruleid);
                    Intent intent = new Intent(getActivity(),NewEditFilterRule.class);
                    intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_UPDATE);
                    intent.putExtra(NewEditActivity.KEY_ORIG,rule);
                    intent.putExtra(NewEditActivity.KEY_RULEID,ruleid);
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
