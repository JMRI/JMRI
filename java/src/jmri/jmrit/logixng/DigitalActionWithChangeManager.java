package jmri.jmrit.logixng;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import jmri.Manager;

/**
 * Manager for DigitalActionWithChangeBean
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public interface DigitalActionWithChangeManager extends Manager<MaleDigitalActionWithChangeSocket> {

    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalActionWithChangeSocket for the action.
     *
     * @param action the bean
     * @return the male socket for this action
     * @throws IllegalArgumentException if the action has an invalid system name
     */
    public MaleDigitalActionWithChangeSocket registerAction(@Nonnull DigitalActionWithChangeBean action)
            throws IllegalArgumentException;
    
    /**
     * Create a new system name for an DigitalActionWithChangeBean.
     * @return a new system name
     */
    public String getNewSystemName();

    public FemaleDigitalActionWithChangeSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName);

    /**
     * Get a set of classes that implements the DigitalActionWithChangeBean interface.
     * 
     * @return a set of entries with category and class
     */
    public Map<Category, List<Class<? extends Base>>> getActionClasses();

    /*.*
     * Add an DigitalActionWithChangeBean.
     *
     * @param action the action to add
     * @throws IllegalArgumentException if the action has an invalid system name
     */
//    public void addAction(DigitalActionWithChangeBean action)
//            throws IllegalArgumentException;

    /*.*
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
//    public DigitalActionWithChangeBean getAction(String name);

//    public DigitalActionWithChangeBean getByUserName(String s);

//    public DigitalActionWithChangeBean getBySystemName(String s);

    /*.*
     * Delete DigitalActionWithChangeBean by removing it from the manager. The DigitalActionWithChangeBean must first be
     * deactivated so it stops processing.
     *
     * @param x the DigitalActionWithChangeBean to delete
     */
//    void deleteAction(DigitalActionWithChangeBean x);
    
}
