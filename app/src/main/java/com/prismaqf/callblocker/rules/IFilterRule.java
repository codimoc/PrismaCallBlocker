package com.prismaqf.callblocker.rules;

/**
 * Interface for checking if a telephone number matches the pattern
 * set in the filter
 * @author ConteDiMonteCristo
 */
public interface IFilterRule {

    /**
     *
     * @return the name
     */
    String getName();

    /**
     * Check a match against a list of numbers and patternd
     * @param number the telephone number
     * @return a flag indicating a match
     */
    boolean Matches(String number);
}
