package com.prismaqf.callblocker;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author ConteDiMonteCristo
 */
public class EditFilterPatterns extends ActionBarActivity {

    private final String FRAGMENT = "EditFilterPatternsFragment";
    private EditPatternsFragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);

        myFragment = new EditPatternsFragment();
        if (savedInstanceState == null)
            myFragment.setArguments(getIntent().getExtras());
        else
            myFragment.setArguments(savedInstanceState);


        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, myFragment, FRAGMENT).
                commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_patterns, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_pattern:
                add();
                return true;
            case R.id.action_pick_pattern:
                pick();
                return true;
            case R.id.action_delete_pattern:
                delete();
                return true;
            case R.id.action_update_patterns:
                update();
                return true;
            case R.id.action_help_patterns:
                help();
                return true;
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        //todo: implement this when ptRule is not saved
        super.onBackPressed();
    }
    
    private void pick() {
        //todo: implement this
    }

    private void update() {
        //todo: implement this
    }

    private void add() {
        //todo: implement this
    }

    private void delete() {
        //todo: implement this
    }

    private void help() {
        //todo: implement this
    }
}
