package com.prismaqf.callblocker.utils;

/**
 * Created by ConteDiMonteCristo
 */
public class DebugHelper {

    /**
     * This is the key that can only be constructed here and
     * hence accessible only to derived classes
     */
    protected static DbKey myKey = new DbKey();

    /**
     * This class acts as "friend" proxy to access
     * restricted functionality in DbHelper
     */
    public static class DbKey {
        private DbKey(){}
    }
}
