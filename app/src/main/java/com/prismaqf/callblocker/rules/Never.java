package com.prismaqf.callblocker.rules;

import java.util.Date;

/**
 * Simple class to make a rule never active
 */
public class Never implements ICalendarRule{
    @Override
    public String getName() {
        return "Never";
    }

    @Override
    public boolean IsActive(Date currentTime) {
        return false;
    }

    @Override
    public boolean IsActive() {
        return false;
    }
}
