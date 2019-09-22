package jmri.jmrit.logixng;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import jmri.Manager;

/**
 * Manager for LogixEmulatorActionBean
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface LogixEmulatorActionManager extends Manager<MaleLogixEmulatorActionSocket> {

    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleLogixEmulatorActionSocket for the action.
     *
     * @param action the bean
     * @return the male socket for this action
     * @throws IllegalArgumentException if the action has an invalid system name
     */
    public MaleLogixEmulatorActionSocket registerAction(@Nonnull LogixEmulatorActionBean action)
            throws IllegalArgumentException;
    
    /**
     * Create a new system name for an LogixEmulatorActionBean.
     * @return a new system name
     */
    public String getNewSystemName();

    public FemaleLogixEmulatorActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName);

    /**
     * Get a set of classes that implements the LogixEmulatorActionBean interface.
     * 
     * @return a set of entries with category and class
     */
    public Map<Category, List<Class<? extends Base>>> getActionClasses();

    /*.*
     * Add an LogixEmulatorActionBean.
     *
     * @param action the action to add
     * @throws IllegalArgumentException if the action has an invalid system name
     */
//    public void addAction(LogixEmulatorActionBean action)
//            throws IllegalArgumentException;

    /*.*
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
//    public LogixEmulatorActionBean getAction(String name);

//    public LogixEmulatorActionBean getByUserName(String s);

//    public LogixEmulatorActionBean getBySystemName(String s);

    /*.*
     * Delete LogixEmulatorActionBean by removing it from the manager. The LogixEmulatorActionBean must first be
     * deactivated so it stops processing.
     *
     * @param x the LogixEmulatorActionBean to delete
     */
//    void deleteAction(LogixEmulatorActionBean x);
    
}
