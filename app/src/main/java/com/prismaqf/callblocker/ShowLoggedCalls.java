package com.prismaqf.callblocker;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.LoggedCall;

/**
 * Activity to show a list of recent logged calls with
 * timestamp, calling number, description
 * and name of the rule matched (if any
 * @author ConteDiMonteCristo
 */public class ShowLoggedCalls extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = ShowLoggedCalls.class.getCanonicalName();
    private static final int URL_LOADER = 1; // Identifies a particular Loader being used in this component
    private SimpleCursorAdapter myAdapter;
    private SQLiteDatabase myDbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_bound_list_activity);
        myDbConnection = new DbHelper(this).getReadableDatabase();

        //// TODO: change ruleid with rule description by joining tables
        myAdapter = new SimpleCursorAdapter(this,
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

        setListAdapter(myAdapter);
        getLoaderManager().initLoader(URL_LOADER, null, this);

    }

    @Override
    public void onStop() {
        super.onStop();
        myDbConnection.close();
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
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                        String key = getString(R.string.prefs_key_sql_limit);
                        int limit = Integer.parseInt(prefs.getString(key,"10"));
                        return LoggedCall.LatestCalls(myDbConnection, limit);
                    }
                };
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        myAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        myAdapter.swapCursor(null);
    }
}
