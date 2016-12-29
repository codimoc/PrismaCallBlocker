package com.prismaqf.callblocker.rules;

import java.util.Date;

/**
 * Interface for checking if a rule is active at a given moment in time
 * @author ConteDiMonteCristo
 */
public interface ICalendarRule {

    /**
     *
     * @return the name of the rule
     */
    String getName();

    /**
     * Check if a rule is currently active
     * @param currentTime the current time
     * @return a boolean flag to check if the rule is active
     */
    boolean IsActive(Date currentTime);

    /**
     * Check if a rule is currently active
     * @return a boolean flag to check if the rule is active
     */
    boolean IsActive();
}


