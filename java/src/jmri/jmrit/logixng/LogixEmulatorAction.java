package jmri.jmrit.logixng;

/**
 * A LogixNG logix emulator action.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixEmulatorAction extends Base {

    public enum OnChangeType {
        ON_CHANGE_TO_TRUE,
        ON_CHANGE_TO_FALSE,
        ON_CHANGE,
    }
    
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
     * @param hasChangedToTrue true if the expression has changed to true.
     * false if the expression has changed to false
     */
    public void execute(boolean hasChangedToTrue);
    
}
