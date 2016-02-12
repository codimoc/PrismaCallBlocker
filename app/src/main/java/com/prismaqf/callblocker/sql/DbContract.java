package com.prismaqf.callblocker.sql;

import android.provider.BaseColumns;

/**
 * DB Contract class for schema in DB interaction
 * @author ConteDiMonteCristo
 */
public class DbContract {
    private DbContract() {}

    //general purpose tokens
    public static final String COMMA_SEP = ", ";
    public static final String OPEN_BRAC = " (";
    public static final String CLOSE_BRAC = ") ";
    public static final String IPK = " INTEGER PRIMARY KEY NOT NULL, ";
    public static final String TYPE_TEXT = " TEXT";
    public static final String TYPE_INT = " INTEGER";
    public static final String NOT_NULL = " NOT NULL";
    public static final String DATE_FORMAT = "d MMM yyyy 'at' HH:mm:ss z";

    /**
     * Schema for a table on service runs: a service run contains the
     * stats for a run since the service was started until it was
     * stopped. The time of start and stop should be reported
     * together with the total number of calls received and events
     * triggered including those in the current run
     */
    public static abstract class ServiceRuns implements BaseColumns {
        public static final String TABLE_NAME = "serviceruns";
        public static final String COLUMN_NAME_START = "start";
        public static final String COLUMN_NAME_STOP = "stop";
        public static final String COLUMN_NAME_TOTAL_RECEIVED = "totaldreceived";
        public static final String COLUMN_NAME_TOTAL_TRIGGERED = "totaltriggered";
        //sql table creation and deletion
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + OPEN_BRAC +
                _ID + IPK +
                COLUMN_NAME_START + TYPE_TEXT + NOT_NULL + COMMA_SEP +
                COLUMN_NAME_STOP + TYPE_TEXT + COMMA_SEP +
                COLUMN_NAME_TOTAL_RECEIVED + TYPE_INT + COMMA_SEP +
                COLUMN_NAME_TOTAL_TRIGGERED + TYPE_INT +
                CLOSE_BRAC;
        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Schema for a table for looging calls. It contains the run id of the
     * service session, the time-stamp when the call was received, the calling number,
     * an optional descriotion (contact name from contacts) and an optional
     * rule id if the number has triggered any rule
     */
    public static abstract class LoggedCalls implements BaseColumns {
        public static final String TABLE_NAME = "loggedcalls";
        public static final String COLUMN_NAME_RUNID = "runid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_NUMBER = "number";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_RULEID = "ruleid";
        //sql table creation and deletion
        public static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + OPEN_BRAC +
                _ID + IPK +
                COLUMN_NAME_RUNID + TYPE_INT + NOT_NULL + COMMA_SEP +
                COLUMN_NAME_TIMESTAMP + TYPE_TEXT + NOT_NULL + COMMA_SEP +
                COLUMN_NAME_NUMBER + TYPE_TEXT + NOT_NULL + COMMA_SEP +
                COLUMN_NAME_DESCRIPTION + TYPE_TEXT + COMMA_SEP +
                COLUMN_NAME_RULEID + TYPE_INT +
                CLOSE_BRAC;
        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    }
}
