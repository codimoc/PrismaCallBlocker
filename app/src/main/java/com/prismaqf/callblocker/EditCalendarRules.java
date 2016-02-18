package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

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
}
