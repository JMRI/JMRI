package jmri.jmrit.logixng;

import jmri.JmriException;

/**
 * A LogixNG DigitalAction that supports EnableExecution.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface DigitalActionWithEnableExecution {

    /**
     * Enables or disables execution for this digital action. An action for
     * which execution is disabled will evaluate its expressions, if it has that,
     * but not execute any actions.
     * <p>
     * Note that enable execution for LogixNG is the equivalent of enable for Logix.
     * 
     * @param b if true, enables execution, otherwise disables execution
     */
    public void setEnableExecution(boolean b);
    
    /**
     * Determines whether execution is enabled for this digital action. An
     * action for which execution is disabled will evaluate its expressions,
     * if it has that, but not execute any actions.
     * <p>
     * Note that EnableExecution for LogixNG is the equivalent of enable for Logix.
     * 
     * @return true if execution is enbaled for the digital action, false otherwise
     */
    public boolean isExecutionEnabled();
    
    /**
     * Evaluate the action without execution.
     * <p>
     * Note that enable execution for LogixNG is the equivalent of enable for Logix.
     * @throws jmri.JmriException when an exception occurs
     */
    void evaluateOnly() throws JmriException;
    
}
