package com.prismaqf.callblocker.actions;

import android.content.Context;

import com.prismaqf.callblocker.utils.DexClassScanner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A registry containg a singleton version of the available actions
 * @author ConteDiMonteCristo
 */
public class ActionRegistry {
    private static Map<String,IAction> registry = null;

    private static void initialise(Context ctx) throws ReflectiveOperationException {
        Set<Class<?>> actions = DexClassScanner.findSubClassesWithAnnotation(ctx,
                "com.prismaqf.callblocker.actions",
                IAction.class,
                AvailableAction.class);
        for (Class c : actions) {
            IAction action = (IAction) c.newInstance();
            registry.put(c.getCanonicalName(),action);
        }
    }

    public static IAction getAvailableAction(Context ctx, String canonicalName) throws ReflectiveOperationException {
        if (registry==null) {
            registry = new HashMap<>();
            initialise(ctx);
        }
        return registry.get(canonicalName);
    }

    public static Collection<IAction> getAvailableActions(Context ctx) throws ReflectiveOperationException {
        if (registry==null) {
            registry = new HashMap<>();
            initialise(ctx);
        }
        return registry.values();
    }
}
