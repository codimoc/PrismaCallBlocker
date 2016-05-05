package com.prismaqf.callblocker.utils;

import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.DbHelperTest;

import org.junit.rules.ExternalResource;

/**
 * JUnit Rule to inject the DB file name in DBHelper
 * @author ConteDiMonteCristo
 */
public class DebugDBFileName extends ExternalResource {

    static class MyKey extends DebugKey {}

    private final MyKey myKey = new MyKey();

    @Override
    protected void before() throws Throwable {
        DbHelper.SetDebugDb(myKey.getKey(), DbHelperTest.DB_NAME);
    }

    @Override
    protected void after() {
        DbHelper.SetDebugDb(myKey.getKey(),null);
    }
}
