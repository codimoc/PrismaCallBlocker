package com.prismaqf.callblocker.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private static final String TAG = DexClassScanner.class.getCanonicalName();

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
            List<String> apkClassNames = Collections.list(dex.entries());
            //deal with instant run here
            List<String> sourcePaths = new ArrayList<>();
            File instantRunFilePath = new File(ai.dataDir,"files" + File.separator + "instant-run" + File.separator + "dex");
            if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                File[] sliceFiles = instantRunFilePath.listFiles();
                for (File sliceFile : sliceFiles) {
                    if (null != sliceFile && sliceFile.exists() && sliceFile.isFile() && sliceFile.getName().endsWith(".dex")) {
                        sourcePaths.add(sliceFile.getAbsolutePath());
                    }
                }
            }
            for (String sp : sourcePaths) {
                dex = new DexFile(sp);
                apkClassNames.addAll(Collections.list(dex.entries()));
            }
            //finish instant run
            for (String className : apkClassNames) {
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
