package com.prismaqf.callblocker;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;

import com.prismaqf.callblocker.sql.DbHelper;


/**
 * Fragment for editing lists bound to cursors
 * @author ConteDiMonteCristo
 */
public abstract class EditCursorListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private SimpleCursorAdapter myAdapter;
    SQLiteDatabase myDbConnection;

    protected abstract SimpleCursorAdapter getAdapter();
    protected abstract void initLoader();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        return inflater.inflate(R.layout.data_bound_list_activity,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        myDbConnection = new DbHelper(getActivity()).getWritableDatabase();
        myAdapter = getAdapter();

        setListAdapter(myAdapter);
        initLoader();
    }
    @Override
    public void onStop() {
        super.onStop();
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
}
