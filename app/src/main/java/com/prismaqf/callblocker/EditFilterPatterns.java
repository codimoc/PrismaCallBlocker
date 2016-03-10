package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.utils.PatternAdapter;

/**
 * @author ConteDiMonteCristo
 */
public class EditFilterPatterns extends ActionBarActivity {

    private final String FRAGMENT = "EditFilterPatternsFragment";
    private EditPatternsFragment myFragment;
    private final int RESULT_PICK = 1001;
    private MenuItem mi_update, mi_delete;
    private FilterRule myOrigRule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.data_bound_edit_activity);

        myFragment = new EditPatternsFragment();
        if (savedInstanceState == null) {
            myFragment.setArguments(getIntent().getExtras());
            if (myOrigRule == null)
                myOrigRule = getIntent().getParcelableExtra(NewEditActivity.KEY_ORIG);
        } else {
            myFragment.setArguments(savedInstanceState);
            myOrigRule = getIntent().getParcelableExtra(NewEditActivity.KEY_ORIG);
        }


        getFragmentManager().
                beginTransaction().
                setTransition(FragmentTransaction.TRANSIT_ENTER_MASK).
                replace(R.id.list_fragment_holder, myFragment, FRAGMENT).
                commit();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(NewEditActivity.KEY_PTRULE, myFragment.getAdapter().getRule());
        savedInstanceState.putParcelable(NewEditActivity.KEY_ORIG, myOrigRule);
        savedInstanceState.putStringArrayList(NewEditActivity.KEY_CHECKED, myFragment.getAdapter().getMyChecked());
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_patterns, menu);
        mi_update = menu.findItem(R.id.action_update_patterns);
        mi_delete = menu.findItem(R.id.action_delete_pattern);
        validateActions();
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
                FilterRule currentRule = myFragment.getAdapter().getRule();
                if (currentRule !=null && !currentRule.equals(myOrigRule)) {
                    update();
                    return true;
                }
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(this, intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        FilterRule currentRule = myFragment.getAdapter().getRule();
        if (currentRule !=null && !currentRule.equals(myOrigRule)) {
            update();
            return;
        }
        super.onBackPressed();
    }

    private void pick() {
        Intent intent = new Intent(this, ShowLoggedCalls.class);
        intent.putExtra(NewEditActivity.KEY_ACTION, NewEditActivity.ACTION_PICK);
        startActivityForResult(intent, RESULT_PICK);
        validateActions();
    }

    private void update() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(NewEditActivity.KEY_PTRULE, myFragment.getAdapter().getRule());
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void add() {
        //todo: restrict input to what allowed
        //todo: what to do with screen rotation?
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type a pattern (* and digits)");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myFragment.getAdapter().add(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        validateActions();
    }

    private void delete() {
        PatternAdapter adapter = myFragment.getAdapter();
        for (String pattern : adapter.getMyChecked())
            adapter.remove(pattern);
        adapter.resetChecked();
        validateActions();
    }

    private void help() {
        //todo: implement this
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == RESULT_PICK) {
            String number = data.getStringExtra(NewEditActivity.KEY_NUMBER);

            myFragment.getAdapter().add(number);
        }
    }

    public void validateActions() {
        FilterRule currentRule = myFragment.getAdapter().getRule();
        mi_update.setVisible(!currentRule.equals(myOrigRule));
        mi_delete.setVisible(myFragment.getAdapter().getMyChecked().size() > 0);
    }

}