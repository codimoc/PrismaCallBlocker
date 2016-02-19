package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Base activity for editable list with action bar
 * @author ConteDiMonteCristo
 */
public class EditCalendarRules extends ActionBarActivity {

    private final String FRAGMENT = "EditCalendarRulesFragment";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);


        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, new CalendarRulesFragment(), FRAGMENT).
                commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_list, menu);
        menu.findItem(R.id.action_new_item).setTitle(R.string.menu_new_calendar_rule);
        return super.onCreateOptionsMenu(menu);
    }
}
