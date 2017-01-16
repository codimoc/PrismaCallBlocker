package com.prismaqf.callblocker.actions;

import android.content.Context;


/**
 * Interface for an action to be performed after a rule is triggered
 * upn an incoming number
 * @author ConteDiMonteCristo
 */
public interface IAction{

    /**
     *
     * @return the name
     */
    String getName();

    /**
     * The action to be performed
     * @param context the Android context
     * @param number the incoming number
     * @param info logging information
     */
    void act (Context context, String number, LogInfo info);

}
