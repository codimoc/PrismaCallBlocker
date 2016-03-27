package com.prismaqf.callblocker.actions;

import android.content.Context;

/**
 * Default action (do nothing) when not specified
 * @author ConteDiMonteCristo
 */
public class Nothing implements IAction{
    @Override
    public void act(Context context, String number, LogInfo info) {}
}
