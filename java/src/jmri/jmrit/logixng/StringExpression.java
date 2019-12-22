package jmri.jmrit.logixng;

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
     */
    public String evaluate() throws Exception;
    
    /**
     * Reset the evaluation.
     * 
     * A parent expression must to call reset() on its child when the parent
     * is reset().
     */
    public void reset();
    
}
