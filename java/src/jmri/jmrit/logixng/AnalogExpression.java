package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * Analog expression is used in LogixNG to answer a question that can give
 * an analog value as result.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface AnalogExpression extends Base {
    
    /**
     * Evaluate this expression.
     * 
     * @return the result of the evaluation. The male socket that holds this
     * expression throws an exception if this value is a Double.NaN or an
     * infinite number.
     * 
     * @throws jmri.JmriException when an exception occurs
     */
    public double evaluate() throws JmriException;
    
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
