package com.prismaqf.callblocker;

import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.LoggedCallProvider;

/**
 * Activity to show a list of recent logged calls with
 * timestamp, calling number, description
 * and name of the rule matched (if any
 * @author ConteDiMonteCristo
 */public class ShowLoggedCalls extends ShowListActivity {

    private final String TAG = ShowLoggedCalls.class.getCanonicalName();
    private static final int URL_LOADER = 1; // Identifies a particular Loader being used in this component

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
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String key = getString(R.string.pk_sql_limit);
                        int limit = Integer.parseInt(prefs.getString(key,"10"));
                        return LoggedCallProvider.LatestCalls(myDbConnection, limit);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public SimpleCursorAdapter getAdapter() {
        // TODO: change ruleid with rule description by joining tables
        return new SimpleCursorAdapter(this,
                R.layout.logged_call_record,
                null,  //no cursor yet
                new String[] {DbContract.LoggedCalls.COLUMN_NAME_TIMESTAMP,
                        DbContract.LoggedCalls.COLUMN_NAME_NUMBER,
                        DbContract.LoggedCalls.COLUMN_NAME_DESCRIPTION,
                        DbContract.LoggedCalls.COLUMN_NAME_RULEID},
                new int[] {R.id.text_timestamp,
                        R.id.text_calling_number,
                        R.id.text_description,
                        R.id.text_rule_matched}, 0);
    }

    @Override
    public void initLoader() {
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (myAction!=null && myAction.equals(NewEditActivity.ACTION_PICK)) {
            Cursor c = (Cursor) myAdapter.getItem(position);
            String number = c.getString(c.getColumnIndexOrThrow(DbContract.LoggedCalls.COLUMN_NAME_NUMBER));
            Intent returnIntent = new Intent();
            returnIntent.putExtra(NewEditActivity.KEY_NUMBER,number);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
    }
}
