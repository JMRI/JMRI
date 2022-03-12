package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * String expression is used in LogixNG to answer a question that can give
 * a string value as result.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface StringExpression extends Base {
    
    /**
     * Evaluate this expression.
     * 
     * @return the result of the evaluation
     * @throws JmriException when an exception occurs
     */
    public String evaluate() throws JmriException;
    
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
