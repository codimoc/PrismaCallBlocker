package com.prismaqf.callblocker.actions;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.prismaqf.callblocker.utils.DebugDBFileName;
import com.prismaqf.callblocker.utils.DexClassScanner;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ActionsTest {

    private final String TEST_NUMBER="123";
    private final long TEST_RUNID = 1001;
    private final int TEST_RULEID=12;

    private static Context myCtx;

    @ClassRule
    public static final DebugDBFileName myDebugDB = new DebugDBFileName();

    @BeforeClass
    public static void classBefore() {
        myCtx = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void testAvailableActions() {
        Set<Class<?>> actions = DexClassScanner.findSubClassesWithAnnotation(myCtx,
                                                                             "com.prismaqf.callblocker.actions",
                                                                              IAction.class,
                                                                              AvailableAction.class);
        assertTrue("The set is not empty",actions.size()>0);
    }



}
