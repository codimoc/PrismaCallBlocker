package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.prismaqf.callblocker.R;

/**
 * Created by ConteDiMonteCristo.
 */
public class PreferenceHelper {

    public static int GetToastVerbosity(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pk_toast_verbosity);
        String verbosity = prefs.getString(key, "medium verbosity");
        switch (verbosity){
            case "no toast messages":
                return 0;
            case "medium verbosity":
                return 1;
            default:
                return 2;
        }
    }

    public static int GetSqlQueryLimit(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pk_sql_limit);
        return Integer.parseInt(prefs.getString(key,"10"));
    }

    public static String GetLogLongevity(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String key = context.getString(R.string.pk_log_longevity);
        return prefs.getString(key, "no limit");
    }
}
