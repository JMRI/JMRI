package jmri.jmrit.logixng;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nonnull;

/**
 * DigitalExpressionBean is used in LogixNG to answer a question that can give
 * the answers 'true' or 'false'.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalExpression extends Base {
    
    public enum TriggerCondition {
        TRUE,
        FALSE,
        CHANGE
    }
    
    /**
     * Evaluate this expression.
     * 
     * @return the result of the evaluation
     */
    public boolean evaluate();
    
    /**
     * Reset the evaluation.
     * 
     * A parent expression must to call reset() on its child when the parent
     * is reset().
     */
    public void reset();
    
}
