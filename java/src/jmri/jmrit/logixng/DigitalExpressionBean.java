package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * DigitalExpressionBean is a DigitalExpression that also implements NamedBean.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalExpressionBean extends NamedBean, DigitalExpression {
    
    /**
     * Constant representing an "false" state. It's returned from the method
     * getState() if the method evaluate() returned false the last time it was
     * called.
     */
    int FALSE = 0x02;

    /**
     * Constant representing an "false" state. It's returned from the method
     * getState() if the method evaluate() returned false the last time it was
     * called.
     */
    int TRUE = 0x04;
    
    /**
     * Notify property change listeners that the result of the expression
     * has changed.
     * @param oldResult the old last result
     * @param newResult the new last result
     */
    void notifyChangedResult(boolean oldResult, boolean newResult);
    
}
