package com.prismaqf.callblocker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Fragment to manage the stiings
 * @author ConteDiMonteCristo
 */
public class SettingFragment extends PreferenceFragment{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
    }

}
