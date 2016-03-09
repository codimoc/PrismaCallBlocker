package com.prismaqf.callblocker.utils;


import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher to check the total count of String matching a condituin
 * @author ConteDiMonteCristo
 */
public class CountingMatcher extends TypeSafeMatcher<String>{

    private int count;
    private final String match;

    public CountingMatcher(String txtToMatch) {
        count = 0;
        match = txtToMatch;
    }

    public int getCount() {return count;}

    public void resetCount() {count = 0;}


    @Override
    public void describeTo(Description description) {

    }

    @Override
    protected boolean matchesSafely(String item) {
        if (item.contains(match) || match.equals("*")) {
            count++;
            return true;
        }
        return false;
    }
}
