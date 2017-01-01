package com.prismaqf.callblocker.rules;

import java.util.Date;

/**
 * Simple class to make a rule always active
 * This is the default behaviour if the calendar rule
 * is not specified
 * @author ConteDiMonteCristo
 */
public class Always implements ICalendarRule {

    @Override
    public String getName() {
        return "Always";
    }

    @Override
    public boolean IsActive(Date currentTime) {
        return true;
    }

    @Override
    public boolean IsActive() {
        return true;
    }
}
