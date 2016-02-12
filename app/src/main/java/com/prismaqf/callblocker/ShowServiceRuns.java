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
import android.widget.ListAdapter;
import android.widget.ListView;

import com.prismaqf.callblocker.sql.DbContract;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.ServiceRun;

/**
 * Activity to show a list of recent service runs with
 * start time, end time and total number of call received
 * and event triggered
 * @author ConteDiMonteCristo
 */
public class ShowServiceRuns extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String TAG = ShowServiceRuns.class.getCanonicalName();
    private static final int URL_LOADER = 0; // Identifies a particular Loader being used in this component
    private SimpleCursorAdapter myAdapter;
    private SQLiteDatabase myDbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.service_runs_activity);
        myDbConnection = new DbHelper(this).getReadableDatabase();

        myAdapter = new SimpleCursorAdapter(this,
                R.layout.service_run_record,
                null,  //no cursor yet
                new String[] {DbContract.ServiceRuns.COLUMN_NAME_START,
                        DbContract.ServiceRuns.COLUMN_NAME_STOP,
                        DbContract.ServiceRuns.COLUMN_NAME_TOTAL_RECEIVED,
                        DbContract.ServiceRuns.COLUMN_NAME_TOTAL_TRIGGERED},
                new int[] {R.id.text_start_time,
                        R.id.text_end_time,
                        R.id.text_num_received,
                        R.id.text_num_triggered}, 0);

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
                        return ServiceRun.LatestRuns(myDbConnection, limit);
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
