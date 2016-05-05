package com.prismaqf.callblocker.utils;

/**
 * Created by ConteDiMonteCristo
 */
public class DebugKey {

    /**
     * This is the key that can only be constructed here and
     * hence accessible only to derived classes
     */
    private final DbKey myKey = new DbKey();

    DbKey getKey() {return myKey;}

    /**
     * This class acts as "friend" proxy to access
     * restricted functionality in DbHelper
     */
    public static class DbKey {
        private DbKey(){}
    }
}
