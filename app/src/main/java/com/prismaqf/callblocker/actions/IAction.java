package com.prismaqf.callblocker.actions;

/**
 * Interface for an action to be performed after a rule is triggered
 * upn an incoming number
 * @author ConteDiMonteCristo
 */
public interface IAction {

    /**
     * The action to be performed
     * @param number the incoming number
     * @param info logging information
     */
    void act (String number, LogInfo info);

    /**
     *
     * @return a short description
     */
    String shortDescription();
}
