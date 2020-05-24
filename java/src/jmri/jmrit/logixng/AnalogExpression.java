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
     * Reset the evaluation.
     * 
     * A parent expression must to call reset() on its child when the parent
     * is reset().
     */
    public void reset();
    
}
