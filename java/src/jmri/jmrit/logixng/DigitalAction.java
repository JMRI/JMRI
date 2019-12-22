package jmri.jmrit.logixng;

/**
 * A LogixNG digitalaction.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalAction extends Base {

    /**
     * Determines whether this action supports enable execution for this
     * digital action. An action for which execution is disabled will evaluate
     * its expressions, if it has that, but not execute any actions.
     * <p>
     * Note that enable execution for LogixNG is the equivalent of enable for
     * Logix.
     * <p>
     * A digital action that supports enable execution must implement the
     * interface DigitalActionWithEnableExecution.
     * 
     * @return true if execution is enbaled for the digital action, false otherwise
     */
    default public boolean supportsEnableExecution() {
        return false;
    }
    
    /**
     * Execute this DigitalActionBean.
     */
    public void execute() throws Exception;
    
}
