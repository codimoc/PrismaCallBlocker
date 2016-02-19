package com.prismaqf.callblocker;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbHelper;

/**
 * Abstract base class for activity showing results as list based on query to DB
 * @author ConteDiMonteCristo
 */
public abstract class ShowListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>  {

    protected SimpleCursorAdapter myAdapter;
    protected SQLiteDatabase myDbConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.data_bound_list_activity);

        myDbConnection = new DbHelper(this).getReadableDatabase();
        myAdapter = getAdapter();

        setListAdapter(myAdapter);
        initLoader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myDbConnection.close();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        myAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        myAdapter.swapCursor(null);
    }

    public abstract SimpleCursorAdapter getAdapter();
    public abstract void initLoader();
}
