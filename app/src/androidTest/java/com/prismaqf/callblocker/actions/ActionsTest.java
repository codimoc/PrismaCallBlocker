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

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ActionsTest {

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

    @Test
    public void constructActionGivenDescription() throws ReflectiveOperationException {
        Set<Class<?>> actions = DexClassScanner.findSubClassesWithAnnotation(myCtx,
                "com.prismaqf.callblocker.actions",
                IAction.class,
                AvailableAction.class);
        Class actionClass=null;
        for (Class c : actions) {
            AvailableAction a = (AvailableAction) c.getAnnotation(AvailableAction.class);
            if (a != null) {
                String desc = a.description();
                if (desc.contains("button down")) {
                    actionClass = c;
                    break;
                }
            }
        }
        assertNotNull("Found action with proper description", actionClass);
        IAction action = (IAction) actionClass.newInstance();
        assertNotNull("I can construct the IAction object", action);
    }

    @Test
    public void testActionRegistry() throws ReflectiveOperationException {
        Collection<IAction> actions = ActionRegistry.getAvailableActions(myCtx);
        assertTrue("The collection of actions is not null", actions.size()>0);
        IAction action1 = (IAction)actions.toArray()[0];
        IAction action2 = ActionRegistry.getAvailableAction(myCtx,action1.getClass().getCanonicalName());
        assertTrue("The two objects are identical",action1==action2);
        assertTrue("The two objects have the same hash",action1.hashCode()==action2.hashCode());
    }



}
