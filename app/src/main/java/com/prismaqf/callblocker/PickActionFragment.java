package com.prismaqf.callblocker;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.prismaqf.callblocker.actions.IAction;
import com.prismaqf.callblocker.utils.ActionAdapter;

/**
 * @author ConteDiMonteCristo
 */
public class PickActionFragment extends ListFragment {
    private ActionAdapter myAdapter;

    public ActionAdapter getAdapter() {return myAdapter;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        return inflater.inflate(R.layout.data_bound_list_activity,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        myAdapter = new ActionAdapter(getActivity());
        setListAdapter(myAdapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        IAction action = myAdapter.getItem(position);
        Intent returnIntent = new Intent();
        returnIntent.putExtra(NewEditActivity.KEY_ACTIONNAME, action != null ? action.getName() : null);
        getActivity().setResult(Activity.RESULT_OK, returnIntent);
        getActivity().finish();
    }
}
