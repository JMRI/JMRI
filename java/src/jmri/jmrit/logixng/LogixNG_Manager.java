package jmri.jmrit.logixng;

import java.util.List;
import jmri.Manager;

/**
 * Manager for LogixNG
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface LogixNG_Manager extends Manager<LogixNG> {

    /**
     * Create a new LogixNG if the LogixNG does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new LogixNG or null if unable to create
     */
    public LogixNG createLogixNG(String systemName, String userName)
            throws IllegalArgumentException;
    
    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new LogixNG or null if unable to create
     */
    public LogixNG createLogixNG(String userName)
            throws IllegalArgumentException;
    
    /*.*
     * Creates the initial items in the LogixNG tree.
     * 
     * By default, this is as following:
     * + ActionMany
     *   + ActionHoldAnything
     *   + ActionDoIf
     * 
     * @param conditionalNG the ConditionalNG to be initialized with a tree
     *./
    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG);
*/
    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    public LogixNG getLogixNG(String name);
    
    /** {@inheritDoc} */
    @Override
    public LogixNG getByUserName(String name);
    
    /** {@inheritDoc} */
    @Override
    public LogixNG getBySystemName(String name);
    
    /**
     * Create a new system name for a LogixNG.
     * @return a new system name
     */
    public String getAutoSystemName();
    
    /**
     * Resolve all the LogixNG trees.
     * <P>
     * This method ensures that everything in the LogixNG tree has a pointer
     * to its parent.
     */
    public void resolveAllTrees();

    /**
     * Setup all LogixNGs. This method is called after a configuration file is
     * loaded.
     */
    public void setupAllLogixNGs();

    /**
     * Activate all LogixNGs, starts LogixNG processing by connecting all
     * inputs that are included the ConditionalNGs in this LogixNG.
     * <p>
     * A LogixNG must be activated before it will calculate any of its
     * ConditionalNGs.
     */
    public void activateAllLogixNGs();
    
    /**
     * Delete LogixNG by removing it from the manager. The LogixNG must first
     * be deactivated so it stops processing.
     *
     * @param x the LogixNG to delete
     */
    void deleteLogixNG(LogixNG x);

    /**
     * Support for loading LogixNGs in a disabled state
     * 
     * @param s true if LogixNG should be disabled when loaded
     */
    public void setLoadDisabled(boolean s);
    
    /**
     * Register a FemaleSocketFactory.
     * @param factory the factory
     */
    public void registerFemaleSocketFactory(FemaleSocketFactory factory);
    
    /**
     * Register a FemaleSocketFactory.
     * @return list of factories
     */
    public List<FemaleSocketFactory> getFemaleSocketFactories();
    
}
