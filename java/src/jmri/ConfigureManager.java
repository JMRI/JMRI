package jmri;

import java.io.File;
import java.net.URL;
import java.util.List;
import jmri.jmrit.XmlFile;

/**
 * Provide load/store capabilities for general configuration.
 * <p>
 * Areas of responsibility:
 * <ul>
 * <li>Register and deregister configuration objects so they can eventually be
 * stored.
 * <li>Invoke the load and store operations as needed
 * <li>Give access to the configuration objects for independent GUIs
 * </ul>
 * <p>
 * The managed items are divided into four types:
 * <ol>
 * <li>"Prefs" - handled first on read, these are the general preferences
 * controlling how the program starts up
 * <li>"Config" - layout configuration information, e.g. turnout, signal, etc
 *   - generally, all NamedBeanManagers
 * <li>"Tool" - (Not really clear yet, but present)
 * <li>"User" - typically information about panels and windows, these are
 * handled last during startup - all the jmri.display panel types
 * </ol>
 * <p>
 * The configuration manager is generally located through the InstanceManager.
 * <p>
 * The original implementation was via the {@link jmri.configurexml} package.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2010, 2017, 2020
 * @author Matthew Harris Copyright (C) 2010, 2016
 * @author Randall Wood Coyright (C) 2013, 2015, 2017
 * @see jmri.InstanceManager
 * @see jmri.configurexml.ConfigXmlManager
 */
public interface ConfigureManager {

    void registerPref(Object o);

    void removePrefItems();

    void registerConfig(Object o);

    void registerConfig(Object o, int x);

    void registerTool(Object o);

    void registerUser(Object o);

    void registerUserPrefs(Object o);

    void deregister(Object o);

    /**
     * Find the ith instance of an object of particular class that's been
     * registered for storage.
     * <p>
     * Note that the index of an object can change when other objects are stored
     * or removed. The index is for indexing over the objects stored at a
     * moment, not for use as an identification number.
     * <p>
     * There may be synchronization issues associated with this, although they
     * are expected to be rare in practice.
     *
     * @param c     Class of the desired objects
     * @param index a 1-based index of the object to return
     * @return an object of class c or null
     */
    Object findInstance(Class<?> c, int index);

    /**
     * Returns a list of instances stored for a given class.
     *
     * @param c Class of the desired objects
     * @return an List of objects of class c or null
     */
    List<Object> getInstanceList(Class<?> c);

    /**
     * Stores just preferences information.
     * <p>
     * Where that information is stored is implementation-specific.
     */
    void storePrefs();

    /**
     * Stores just preferences information.
     *
     * @param file the to store preferences into
     */
    void storePrefs(File file);

    /**
     * Stores just user preferences information.
     *
     * @param file the file to store user preferences into
     */
    void storeUserPrefs(File file);

    /**
     * Stores just configuration information.
     *
     * @param file Output file
     * @return true if successful; false otherwise
     */
    boolean storeConfig(File file);

    /**
     * Stores user and config information.
     *
     * @param file Output file
     * @return true if succeeded
     */
    boolean storeUser(File file);

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file Input file
     * @return true if succeeded
     * @throws jmri.JmriException if unable to load file due to internal error
     */
    boolean load(File file) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file Input URL
     * @return true if succeeded
     * @throws jmri.JmriException if unable to load URL due to internal error
     */
    boolean load(URL file) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file             Input file
     * @param registerDeferred true to register actions for deferred load
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @since 2.11.2
     */
    boolean load(File file, boolean registerDeferred) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file             Input URL
     * @param registerDeferred true to register actions for deferred load
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @since 2.11.2
     */
    boolean load(URL file, boolean registerDeferred) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file that have
     * been deferred until after basic GUI construction completed
     *
     * @param file Input file
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    boolean loadDeferred(File file) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file that have
     * been deferred until after basic GUI construction completed
     *
     * @param file Input URL
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @see jmri.configurexml.XmlAdapter#loadDeferred()
     * @since 2.11.2
     */
    boolean loadDeferred(URL file) throws JmriException;

    /**
     * Provide a method-specific way of locating a file to be loaded from a
     * name.
     *
     * @param filename Local filename, perhaps without path information
     * @return Corresponding {@link java.net.URL}
     */
    URL find(String filename);

    /**
     * Make a backup file.
     *
     * @param file to be backed up
     * @return true if successful
     */
    boolean makeBackup(File file);

    /**
     * Control the scope of validation of XML files when loading.
     *
     * @param validate the validation scope
     */
    void setValidate(XmlFile.Validate validate);

    /**
     * Get the scope of validation of XML files when loading.
     *
     * @return the validation scope
     */
    XmlFile.Validate getValidate();
}
