package jmri.jmrit.logixng;

import jmri.Manager;

/**
 * Manager for ConditionalNG
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface ConditionalNG_Manager extends Manager<ConditionalNG> {

    /**
     * Create a new ConditionalNG if the ConditionalNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(String systemName, String userName)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(String userName)
            throws IllegalArgumentException;
    
    /**
     * Create a new ConditionalNG if the ConditionalNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @param threadID   the thread ID that this ConditionalNG will execute on
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(
            String systemName, String userName, int threadID)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @param threadID   the thread ID that this ConditionalNG will execute on
     * @return a new ConditionalNG or null if unable to create
     */
    public ConditionalNG createConditionalNG(String userName, int threadID)
            throws IllegalArgumentException;
    
    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public ConditionalNG getConditionalNG(String name);
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG getByUserName(String name);
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG getBySystemName(String name);
    
    /**
     * {@inheritDoc}
     * 
     * The sub system prefix for the ConditionalNG_Manager is
     * {@link #getSystemNamePrefix() } and "C";
     */
    @Override
    public default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "C";
    }
    
    /**
     * Create a new system name for a ConditionalNG.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * Resolve all the ConditionalNG trees.
     * <P>
     * This method ensures that everything in the ConditionalNG tree has a pointer
     * to its parent.
     */
    public void resolveAllTrees();

    /**
     * Setup all ConditionalNGs. This method is called after a configuration file is
     * loaded.
     */
    public void setupAllConditionalNGs();

    /**
     * Delete ConditionalNG by removing it from the manager. The ConditionalNG must first
     * be deactivated so it stops processing.
     *
     * @param x the ConditionalNG to delete
     */
    void deleteConditionalNG(ConditionalNG x);

    /**
     * Support for loading ConditionalNGs in a disabled state
     * 
     * @param s true if ConditionalNG should be disabled when loaded
     */
    public void setLoadDisabled(boolean s);
    
    /**
     * Set whenether execute() should run on the GUI thread at once or should
     * dispatch the call until later, for all ConditionalNGs registered in this
     * manager.
     * Most tests turns off the delay to simplify the tests.
     * @param value true if execute() should run on GUI thread delayed,
     * false otherwise.
     */
    public void setRunOnGUIDelayed(boolean value);

}
