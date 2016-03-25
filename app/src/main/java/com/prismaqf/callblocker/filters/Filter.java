package com.prismaqf.callblocker.filters;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.prismaqf.callblocker.actions.ActionRegistry;
import com.prismaqf.callblocker.actions.IAction;
import com.prismaqf.callblocker.rules.CalendarRule;
import com.prismaqf.callblocker.rules.FilterRule;
import com.prismaqf.callblocker.rules.ICalendarRule;
import com.prismaqf.callblocker.rules.IFilterRule;
import com.prismaqf.callblocker.sql.CalendarRuleProvider;
import com.prismaqf.callblocker.sql.DbHelper;
import com.prismaqf.callblocker.sql.FilterRuleProvider;

import java.sql.SQLException;

/**
 * A filter is a combination of a calendar rule, a filter rule
 * and an action, and basically performs an action when the
 * two rules matches
 * @author ConteDiMonteCristo
 */
public class Filter {

    private static final String TAG = Filter.class.getCanonicalName();

    private final ICalendarRule calendarRule;
    private final IFilterRule filterRule;
    private final IAction action;

    public ICalendarRule getCalendarRule() {
        return calendarRule;
    }

    public IFilterRule getFilterRule() {
        return filterRule;
    }

    public IAction getAction() {
        return action;
    }

    private Filter(ICalendarRule calendarRule, IFilterRule filterRule, IAction action) {
        this.calendarRule = calendarRule;
        this.filterRule = filterRule;
        this.action = action;
    }

    public static Filter makeFilter(Context ctx, String calendarRuleName, String filterRuleName, String actionCanonicalClass )
            throws SQLException, ReflectiveOperationException {
        SQLiteDatabase db=null;
        try {
           db  = new DbHelper(ctx).getReadableDatabase();
            CalendarRule cr = CalendarRuleProvider.FindCalendarRule(db, calendarRuleName);
            if (cr==null) {
                String msg = String.format("Can't find a calendar rule with name %s", calendarRuleName);
                Log.e(TAG, msg);
                throw new SQLException(msg);
            }
            FilterRule fr = FilterRuleProvider.FindFilterRule(db,filterRuleName);
            if (fr==null) {
                String msg = String.format("Can't find a filter rule with name %s", filterRuleName);
                Log.e(TAG, msg);
                throw new SQLException(msg);
            }
            IAction action = ActionRegistry.getAvailableAction(ctx,actionCanonicalClass);
            if (action==null) {
                String msg = String.format("Can't find an action with class %s", actionCanonicalClass);
                Log.e(TAG, msg);
                throw new IllegalArgumentException(msg);
            }
            return new Filter(cr,fr,action);
        }
        finally {
            if (db != null) db.close();
        }
    }
}
