package com.prismaqf.callblocker;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbContract;

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
        //todo: implement here
    }
}
