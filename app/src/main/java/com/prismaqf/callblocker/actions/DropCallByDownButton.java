package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import java.io.Serializable;

/**
 * @author ConteDiMonteCristo
 * @see 'http://www.codeproject.com/Tips/578817/Reject-and-Accept-an-Incoming-Call'
 */
@AvailableAction(description="Drop by button down")
public class DropCallByDownButton implements IAction, Serializable{
    private final static String TAG = DropCallByDownButton.class.getCanonicalName();
    private final static String DESCRIPTION = "Drop call by down button + headset hook";
    private final static long serialVersionUID = 1L; //for serialization consistency

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }


    @Override
    public void act(Context ctx, String number, LogInfo info) {
        Log.i(TAG, "Dropping a call by down button");
        Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
        buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
        ctx.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

}
