package com.prismaqf.callblocker;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Generic validator for rule names
 * @author ConteDiMonteCristo
 */
abstract class RuleNameValidator implements TextWatcher {

    private final TextView mySource, myTarget;
    private final ArrayList<String> myUsedNames;

    public RuleNameValidator(TextView source, TextView target, ArrayList<String> names) {
        mySource = source;
        myTarget = target;
        myUsedNames = names;
    }

    public abstract void validate(TextView source, TextView target, ArrayList<String> names, String text);

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String text = mySource.getText().toString();
        validate(mySource, myTarget, myUsedNames, text);
    }
}
