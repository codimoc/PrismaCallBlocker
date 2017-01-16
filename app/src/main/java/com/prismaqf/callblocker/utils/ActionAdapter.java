package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.prismaqf.callblocker.R;
import com.prismaqf.callblocker.actions.ActionRegistry;
import com.prismaqf.callblocker.actions.AvailableAction;
import com.prismaqf.callblocker.actions.IAction;
import com.prismaqf.callblocker.actions.LogIncoming;

/**
 * Array adapter to chose an action out of a list of available actions
 * @author ConteDiMonteCristo
 */
public class ActionAdapter extends ArrayAdapter<IAction> {

    public ActionAdapter(Context context) {
        super(context, 0);
        try {
            super.addAll(ActionRegistry.getAvailableActions(context));
        } catch (Exception e) {
            String TAG = ActionAdapter.class.getCanonicalName();
            Log.e(TAG, e.getMessage());
            super.add(new LogIncoming());
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final IAction action = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.action_record, parent, false);
        }
        // Lookup view for data population
        TextView tv_name = (TextView) convertView.findViewById(R.id.text_action_class);
        TextView tv_description = (TextView) convertView.findViewById(R.id.text_action_description);
        tv_name.setText(action != null ? action.getClass().getSimpleName() : null);
        AvailableAction a = action.getClass().getAnnotation(AvailableAction.class);
        if (a!=null)
            tv_description.setText(a.description());
        else
            tv_description.setText(action.toString());

        return convertView;
    }
}
