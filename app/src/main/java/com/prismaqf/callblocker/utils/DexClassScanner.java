package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexFile;

/**
 * This utility class replaces org.reflections that is not working
 * properly for Android. This is because of the DexClassLoader
 * behaving differently from the system class loader
 * @author ConteDiMonteCristo
 * @see 'http://stackoverflow.com/questions/11421085/implementing-spring-like-package-scanning-in-android'
 */
public class DexClassScanner {
    private static String TAG = DexClassScanner.class.getCanonicalName();

    /**
     * Scanning for classes which are subtype of a given type and having a specified annotaion
     * @param context the Android context
     * @param prefix the first part of the package name
     * @param parent the parent type
     * @param annotation the expected annotation
     * @return the set of classes searched
     */
    public static Set<Class<?>> findSubClassesWithAnnotation(Context context, String prefix, Class parent, Class annotation) {
        Set<Class<?>> classes = new HashSet<>();
        ApplicationInfo ai = context.getApplicationInfo();
        String classPath = ai.sourceDir;
        ClassLoader loader = context.getClassLoader();
        DexFile dex = null;
        try {
            dex = new DexFile(classPath);
            Enumeration<String> apkClassNames = dex.entries();
            while (apkClassNames.hasMoreElements()) {
                String className = apkClassNames.nextElement();
                if (!className.startsWith(prefix)) continue;
                try {
                    Class c = loader.loadClass(className);
                    if (annotation!=null && c.getAnnotation(annotation)==null) continue;
                    if (parent.isAssignableFrom(c)) {
                        classes.add(c);
                    }
                } catch (ClassNotFoundException e) {
                    Log.e(TAG,e.getMessage());
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (dex!= null) dex.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return classes;
    }
}
