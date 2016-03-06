package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.prismaqf.callblocker.R;
import com.prismaqf.callblocker.rules.FilterRule;

import java.util.ArrayList;

/**
 * @author ConteDiMonteCristo
 */
public class PatternAdapter extends ArrayAdapter<String> {
    private final FilterRule myRule;

    public PatternAdapter(Context context, FilterRule rule) {
        super(context, 0, new ArrayList<>(rule.getPatternKeys()));
        myRule = rule;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String pattern = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.filter_pattern_record, parent, false);
        }
        // Lookup view for data population
        TextView tv_pattern = (TextView) convertView.findViewById(R.id.text_pattern);
        // Populate the data into the template view using the data object
        tv_pattern.setText(pattern);
        return convertView;
    }

    @Override
    public void add(String pattern) {
        myRule.addPattern(pattern);
        super.clear();
        super.addAll(myRule.getPatternKeys());
    }

    @Override
    public void remove(String pattern) {
        myRule.removePattern(pattern);
        super.clear();
        super.addAll(myRule.getPatternKeys());
    }
}
