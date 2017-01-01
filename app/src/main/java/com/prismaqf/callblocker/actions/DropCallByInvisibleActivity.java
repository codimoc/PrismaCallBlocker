package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prismaqf.callblocker.utils.CallAcceptDrop;

import java.io.Serializable;

/**
 * Hack to display an invisible activity,
 * @author ConteDiMonteCristo
 * @see 'Valter Strods in http://stackoverflow.com/questions/26924618/how-can-incoming-calls-be-answered-programmatically-in-android-5-0-lollipop'
 */
public class DropCallByInvisibleActivity implements IAction, Serializable {

    private final static String TAG = DropCallByInvisibleActivity.class.getCanonicalName();
    private final static String DESCRIPTION = "Reject by simulating headset hook (requires special permission)";
    private final static long serialVersionUID = 1L; //for serialization consistency

    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }

    @Override
    public void act(final Context ctx, final String number, final LogInfo info) {
        Log.i(TAG, "Dropping a call using an invisible activity");
        Intent intent = new Intent(ctx, CallAcceptDrop.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK  |
                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ctx.startActivity(intent);
    }

    @Override
    public String toString() {
        return DESCRIPTION;
    }

}
