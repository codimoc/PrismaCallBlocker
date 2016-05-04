package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.util.ArrayList;

/**
 * Base activity for editable list with action bar
 * @author ConteDiMonteCristo
 */
public class EditFilterRules extends AppCompatActivity {

    private static final int PICK = 1002;

    private class DbOperation extends AsyncTask<SQLiteDatabase, Void ,ArrayList<String>> {

        private final String myContext;

        public DbOperation(String context) {
            myContext = context;
        }

        @Override
        protected ArrayList<String> doInBackground(SQLiteDatabase... dbs) {
            try {
                return FilterRuleProvider.AllRuleNames(dbs[0]);
            }
            finally {
                dbs[0].close();
            }
        }

        @Override
        protected void onPostExecute (ArrayList<String> names) {
            Intent intent = new Intent(EditFilterRules.this, NewEditFilterRule.class);
            intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_CREATE);
            intent.putStringArrayListExtra(NewEditActivity.KEY_RULENAMES, names);
            if (myContext !=null && myContext.equals(NewEditActivity.CONTEXT_PICK)) {
                intent.putExtra(NewEditActivity.KEY_CONTEXT, myContext);
                startActivityForResult(intent,PICK);
            } else
                startActivity(intent);
        }

    }

    private String myContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FilterRulesFragment fragment = new FilterRulesFragment();
        if (savedInstanceState == null) {
            Bundle b = getIntent().getExtras();
            fragment.setArguments(b);
            if (b!= null)
                myContext = b.getString(NewEditActivity.KEY_CONTEXT,"none");
            else
                myContext = "none";
        }
        else {
            fragment.setArguments(savedInstanceState);
            myContext = savedInstanceState.getString(NewEditActivity.KEY_CONTEXT,"none");
        }

        setContentView(R.layout.data_bound_edit_activity);


        String FRAGMENT = "EditFilterRulesFragment";
        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_NONE).
                replace(R.id.list_fragment_holder, fragment, FRAGMENT).
                commit();
        if (getSupportActionBar()!= null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_list, menu);
        menu.findItem(R.id.action_new_item).setTitle(R.string.mn_new_filter_rule);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_new_item:
                newFilterRule();
                return true;
            case android.R.id.home:
                if (myContext.equals(NewEditActivity.CONTEXT_PICK)) {
                    onBackPressed();
                    return true;
                } else {
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == PICK && data.hasExtra(NewEditActivity.KEY_RULENAME)) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra(NewEditActivity.KEY_RULENAME, data.getStringExtra(NewEditActivity.KEY_RULENAME));
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }
    }

    private void newFilterRule() {
        SQLiteDatabase db = new DbHelper(this).getReadableDatabase();
        (new DbOperation(myContext)).execute(db);
    }
}
