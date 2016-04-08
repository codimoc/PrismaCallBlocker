package com.prismaqf.callblocker;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prismaqf.callblocker.utils.ActionAdapter;

import java.util.ArrayList;

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
}
