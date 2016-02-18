package com.prismaqf.callblocker;

import android.app.Activity;
import android.os.Bundle;

/**
 * The main activity for the settings
 * @author ConteDiMonteCristo
 */
public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingFragment())
                .commit();
        //PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    }

}
