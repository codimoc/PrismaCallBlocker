package com.prismaqf.callblocker.actions;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.utils.DebugDBFileName;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ActionsTest {

    private final String TEST_NUMBER="123";
    private final long TEST_RUNID = 1001;
    private final int TEST_RULEID=12;

    private Context myCtx;

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @BeforeClass
    public void classBefore() {
        myCtx = InstrumentationRegistry.getTargetContext();
    }

}
