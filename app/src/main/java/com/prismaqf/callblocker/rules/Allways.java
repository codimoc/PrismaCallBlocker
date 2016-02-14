package com.prismaqf.callblocker.rules;

import java.util.Date;

/**
 * Simple class to make a rule always active
 */
public class Allways implements ICalendarRule {

    @Override
    public boolean IsActive(Date currentTime) {
        return true;
    }

    @Override
    public boolean IsActive() {
        return true;
    }
}
