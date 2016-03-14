package com.prismaqf.callblocker;

import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.utils.PatternAdapter;

import java.util.ArrayList;

/**
 * @author ConteDiMonteCristo
 */
public class EditPatternsFragment extends ListFragment {

    private final String TAG = EditPatternsFragment.class.getCanonicalName();
    private PatternAdapter myAdapter;

    public PatternAdapter getAdapter() {return myAdapter;}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        return inflater.inflate(R.layout.data_bound_list_activity,container,false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Bundle args = getArguments();
        FilterRule rule = args.getParcelable(NewEditActivity.KEY_PTRULE);
        if (rule==null)
            Log.e(TAG,"Missing FilterRule in the bundle when starting the fragment");
        ArrayList<String> checked = args.getStringArrayList(NewEditActivity.KEY_CHECKED);
        myAdapter = new PatternAdapter(getActivity(),rule, checked);
        setListAdapter(myAdapter);
    }
}
