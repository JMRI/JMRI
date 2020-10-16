package jmri.jmrit.logixng;

import jmri.NamedBean;

/**
 * ConditionalNG.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface ConditionalNG extends Base, NamedBean {

    /**
     * Get the female socket of this ConditionalNG.
     * @return the female socket
     */
    public FemaleSocket getFemaleSocket();
    
    /**
     * Determines whether this ConditionalNG supports enable execution. It does
     * that if its action supports enable execution. An action for which
     * execution is disabled will evaluate its expressions, if it has that, but
     * not execute any actions.
     * <p>
     * Note that EnableExecution for LogixNG is the equivalent of enable for Logix.
     * 
     * @return true if execution is enbaled for the digital action, false otherwise
     */
    public boolean supportsEnableExecution();
    
    /**
     * Set whenether this ConditionalNG is enabled or disabled.
     * <P>
     * This method must call registerListeners() / unregisterListeners().
     * 
     * @param enable true if this ConditionalNG should be enabled, false otherwise
     */
    public void setEnabled(boolean enable);
    
    /**
     * Determines whether this ConditionalNG is enabled.
     * 
     * @return true if the ConditionalNG is enabled, false otherwise
     */
    @Override
    public boolean isEnabled();
    
    /**
     * Set whenether execute() should run on the GUI thread at once or should
     * dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     * @param value true if execute() should run on GUI thread delayed,
     * false otherwise.
     */
    public void setRunOnGUIDelayed(boolean value);
    
    /**
     * Get whenether execute() should run on the GUI thread at once or should
     * dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     * @return true if execute() should run on GUI thread delayed,
     * false otherwise.
     */
    public boolean getRunOnGUIDelayed();
    
    /**
     * Enables or disables execution for the digital action of this ConditionalNG.
     * An action which is disabled execution will evaluate its expressions, if
     * it has that, but not execute any actions.
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
     * Execute the ConditionalNG.
     */
    public void execute();
    
    /**
     * Register listeners for the ConditionalNG tree.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not registered more than once.
     */
    public void registerListeners();
    
    /**
     * Unregister listeners for the ConditionalNG tree.
     * <P>
     * Important: This method may be called more than once. Methods overriding
     * this method must ensure that listeners are not unregistered more than once.
     */
    public void unregisterListeners();
    
}
