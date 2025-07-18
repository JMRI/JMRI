package jmri.jmrit.logixng;

import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.Category;

/**
 * Manager for DigitalBooleanActionBean
 *
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public interface DigitalBooleanActionManager extends BaseManager<MaleDigitalBooleanActionSocket> {

    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalBooleanAction for the action.
     *
     * @param action the bean
     * @return the male socket for this action
     * @throws IllegalArgumentException if the action has an invalid system name
     */
    MaleDigitalBooleanActionSocket registerAction(@Nonnull DigitalBooleanActionBean action)
            throws IllegalArgumentException;

    /**
     * Create a new system name for an DigitalBooleanActionBean.
     * @return a new system name
     */
    String getAutoSystemName();

    FemaleDigitalBooleanActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName);

    /**
     * Get a set of classes that implements the DigitalBooleanActionBean interface.
     *
     * @return a set of entries with category and class
     */
    Map<Category, List<Class<? extends Base>>> getActionClasses();

    /*.*
     * Add an DigitalBooleanActionBean.
     *
     * @param action the action to add
     * @throws IllegalArgumentException if the action has an invalid system name
     */
//    void addAction(DigitalBooleanActionBean action)
//            throws IllegalArgumentException;

    /*.*
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
//    DigitalBooleanActionBean getAction(String name);

//    DigitalBooleanActionBean getByUserName(String s);

//    DigitalBooleanActionBean getBySystemName(String s);

    /**
     * {@inheritDoc}
     *
     * The sub system prefix for the DigitalActionManager is
     * {@link #getSystemNamePrefix() } and "DA";
     */
    @Override
    default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "DB";
    }

    /**
     * Delete DigitalBooleanAction by removing it from the manager. The DigitalBooleanActionBean must first be
     * deactivated so it stops processing.
     *
     * @param x the DigitalBooleanAction to delete
     */
    void deleteDigitalBooleanAction(MaleDigitalBooleanActionSocket x);

}
