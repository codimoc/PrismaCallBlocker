package com.prismaqf.callblocker;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ScrollView;

/**
 * Created by ConteDiMonteCristo
 */
public class PickAction extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);

        PickActionFragment myFragment = new PickActionFragment();

        String FRAGMENT = "PickActionFragment";
        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, myFragment, FRAGMENT).
                commit();
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_help:
                help();
                return true;
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void help() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(R.string.tx_actions_help_title);

        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_asset/html/pick_action.html");
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(true);
        scroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        scroll.addView(wv);

        alert.setView(scroll);
        alert.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

}
