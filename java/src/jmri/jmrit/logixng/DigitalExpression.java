package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * DigitalExpressionBean is used in LogixNG to answer a question that can give
 * the answers 'true' or 'false'.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalExpression extends Base {

    /**
     * Evaluate this expression.
     * 
     * @return the result of the evaluation
     * @throws JmriException when an exception occurs
     */
    public boolean evaluate() throws JmriException;
    
    /**
     * Set whenether this expression should trigger the ConditionalNG if the
     * named beans it listens to changes state.
     * @param triggerOnChange true if trigger on change, false otherwise
     */
    public void setTriggerOnChange(boolean triggerOnChange);
    
    /**
     * Get whenether this expression should trigger the ConditionalNG if the
     * named beans it listens to changes state.
     * @return true if trigger on change, false otherwise
     */
    public boolean getTriggerOnChange();
    
}
