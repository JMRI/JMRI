package jmri.jmrit.logixng;

import java.io.PrintWriter;
import java.util.Locale;

import jmri.Manager;

/**
 * Manager for GlobalVariable
 *
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2022
 */
public interface GlobalVariableManager extends Manager<GlobalVariable> {

    /**
     * Create a new GlobalVariable if the GlobalVariable does not exist.
     *
     * @param systemName the system name
     * @param userName   the user name
     * @return a new GlobalVariable or null if unable to create
     */
    GlobalVariable createGlobalVariable(String systemName, String userName)
            throws IllegalArgumentException;

    /**
     * For use with User GUI, to allow the auto generation of systemNames, where
     * the user can optionally supply a username.
     *
     * @param userName the user name
     * @return a new GlobalVariable or null if unable to create
     */
    GlobalVariable createGlobalVariable(String userName)
            throws IllegalArgumentException;

    /**
     * Locate via user name, then system name if needed. Does not create a new
     * one if nothing found
     *
     * @param name User name or system name to match
     * @return null if no match found
     */
    GlobalVariable getGlobalVariable(String name);

    /** {@inheritDoc} */
    @Override
    GlobalVariable getByUserName(String name);

    /** {@inheritDoc} */
    @Override
    GlobalVariable getBySystemName(String name);

    /**
     * Create a new system name for a GlobalVariable.
     * @return a new system name
     */
    String getAutoSystemName();

    /**
     * {@inheritDoc}
     *
     * The sub system prefix for the GlobalVariableManager is
     * {@link #getSystemNamePrefix() } and "GV";
     */
    @Override
    default String getSubSystemNamePrefix() {
        return getSystemNamePrefix() + "GV";
    }

    /**
     * Delete GlobalVariable by removing it from the manager. The GlobalVariable must first
     * be deactivated so it stops processing.
     *
     * @param x the GlobalVariable to delete
     */
    void deleteGlobalVariable(GlobalVariable x);

    /**
     * Print the tree to a stream.
     *
     * @param locale The locale to be used
     * @param writer the stream to print the tree to
     * @param indent the indentation of each level
     */
    void printTree(Locale locale, PrintWriter writer, String indent);

}
