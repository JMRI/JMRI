package jmri.jmrit.logixng;

import jmri.NamedBean;
import jmri.jmrit.logixng.util.LogixNG_Thread;

/**
 * ConditionalNG.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public interface ConditionalNG extends Base, NamedBean {

    /**
     * Get the thread that this conditionalNG executes on.
     * @return the thread
     */
    public LogixNG_Thread getCurrentThread();
    
    /**
     * Get the thread id that this conditionalNG should execute on when JMRI
     * starts next time.
     * It's not currently possible to move a ConditionalNG from one thread to
     * another without restarting JMRI.
     * @return the thread ID
     */
    public int getStartupThreadId();
    
    /**
     * Set the thread id that this conditionalNG should execute on when JMRI
     * starts next time.
     * It's not currently possible to move a ConditionalNG from one thread to
     * another without restarting JMRI.
     * @param threadId the thread ID
     */
    public void setStartupThreadId(int threadId);
    
    /**
     * Get the female socket of this ConditionalNG.
     * @return the female socket
     */
    public FemaleSocket getFemaleSocket();
    
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
     * Set whenether execute() should run on the LogixNG thread at once or
     * should dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     * @param value true if execute() should run on LogixNG thread delayed,
     * false otherwise.
     */
    public void setRunDelayed(boolean value);
    
    /**
     * Get whenether execute() should run on the LogixNG thread at once or
     * should dispatch the call until later.
     * Most tests turns off the delay to simplify the tests.
     * @return true if execute() should run on LogixNG thread delayed,
     * false otherwise.
     */
    public boolean getRunDelayed();
    
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
