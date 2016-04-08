package com.prismaqf.callblocker;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

/**
 * Created by ConteDiMonteCristo
 */
public class PickAction extends AppCompatActivity {
    private final String FRAGMENT = "PickActionFragment";
    private PickActionFragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);

        myFragment = new PickActionFragment();

        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, myFragment, FRAGMENT).
                commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            //todo: manage the help action here
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
