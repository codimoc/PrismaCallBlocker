package com.prismaqf.callblocker.rules;

/**
 * This is the dafult behaviour if the filter rule
 * is not defined
 * @author ConteDiMonteCristo
 */
public class NoMatches implements IFilterRule {
    @Override
    public String getName() {
        return "NoMatches";
    }

    @Override
    public boolean Matches(String number) {
        return false;
    }
}
