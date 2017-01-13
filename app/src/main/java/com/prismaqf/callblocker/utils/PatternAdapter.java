package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.prismaqf.callblocker.EditFilterPatterns;
import com.prismaqf.callblocker.R;
import com.prismaqf.callblocker.rules.FilterRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ConteDiMonteCristo
 */
public class PatternAdapter extends ArrayAdapter<String> {
    private final FilterRule myRule;
    private final Set<String> myChecked;
    private final EditFilterPatterns myActivity;

    public PatternAdapter(Context context, FilterRule rule, ArrayList<String> checked) {
        super(context, 0, new ArrayList<>(rule.getPatternKeys()));
        myActivity = (EditFilterPatterns)context;
        myRule = rule;
        if (checked != null)
            myChecked = new HashSet<>(checked);
        else
            myChecked = new HashSet<>();
    }

    public void resetChecked() {
        myChecked.clear();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final String pattern = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.filter_pattern_record, parent, false);
        }
        // Lookup view for data population
        TextView tv_pattern = (TextView) convertView.findViewById(R.id.text_pattern);
        CheckBox cb_pattern = (CheckBox) convertView.findViewById(R.id.cb_pattern);
        // Populate the data into the template view using the data object
        tv_pattern.setText(pattern);
        if (myChecked.contains(pattern)) cb_pattern.setChecked(true);

        cb_pattern.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.isChecked())
                    myChecked.add(pattern);
                else
                    myChecked.remove(pattern);
                if (myActivity!= null) myActivity.validateActions();
            }
        });
        return convertView;
    }

    @Override
    public void add(String pattern) {
        myRule.addPattern(pattern);
        super.clear();
        super.addAll(myRule.getPatternKeys());
        if (myActivity!= null) myActivity.validateActions();
    }

    @Override
    public void remove(String pattern) {
        myRule.removePattern(pattern);
        super.clear();
        super.addAll(myRule.getPatternKeys());
        if (myActivity!= null) myActivity.validateActions();
    }

    public void replace(String oldPattern, String newPattern) {
        myRule.removePattern(oldPattern);
        myRule.addPattern(newPattern);
        super.clear();
        super.addAll(myRule.getPatternKeys());
        if (myActivity!= null) myActivity.validateActions();
    }

    public FilterRule getRule() {
        return myRule;
    }

    public ArrayList<String> getMyChecked() {
        return new ArrayList<>(myChecked);
    }
}
