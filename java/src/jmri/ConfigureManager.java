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
 * <li>"Tool" - (Not really clear yet, but present)
 * <li>"User" - typically information about panels and windows, these are
 * handled last during startup
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
 * @author Bob Jacobsen Copyright (C) 2002
 * @see jmri.InstanceManager
 * @see jmri.configurexml.ConfigXmlManager
 */
public interface ConfigureManager {

    public void registerPref(Object o);

    public void removePrefItems();

    public void registerConfig(Object o);

    public void registerConfig(Object o, int x);

    public void registerTool(Object o);

    public void registerUser(Object o);

    public void registerUserPrefs(Object o);

    public void deregister(Object o);

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
    public Object findInstance(Class<?> c, int index);

    /**
     * Returns a list of instances stored for a given class.
     *
     * @param c Class of the desired objects
     * @return an List of objects of class c or null
     */
    public List<Object> getInstanceList(Class<?> c);

    /**
     * Stores prefs, config, tools and user information.
     *
     * @param file Output file
     * @return true if succeeded
     */
    public boolean storeAll(File file);

    /**
     * Stores just preferences information.
     * <p>
     * Where that information is stored is implementation-specific.
     */
    public void storePrefs();

    /**
     * Stores just preferences information.
     *
     * @param file the to store preferences into
     */
    public void storePrefs(File file);

    /**
     * Stores just user preferences information.
     *
     * @param file the file to store user preferences into
     */
    public void storeUserPrefs(File file);

    /**
     * Stores just configuration information.
     *
     * @param file Output file
     * @return true if successful; false otherwise
     */
    public boolean storeConfig(File file);

    /**
     * Stores just user information.
     *
     * @param file Output file
     * @return true if succeeded
     */
    public boolean storeUser(File file);

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file Input file
     * @return true if succeeded
     * @throws jmri.JmriException if unable to load file due to internal error
     */
    public boolean load(File file) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file Input URL
     * @return true if succeeded
     * @throws jmri.JmriException if unable to load URL due to internal error
     */
    public boolean load(URL file) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file             Input file
     * @param registerDeferred true to register actions for deferred load
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @since 2.11.2
     */
    public boolean load(File file, boolean registerDeferred) throws JmriException;

    /**
     * Create the objects defined in a particular configuration file
     *
     * @param file             Input URL
     * @param registerDeferred true to register actions for deferred load
     * @return true if succeeded
     * @throws JmriException if problem during load
     * @since 2.11.2
     */
    public boolean load(URL file, boolean registerDeferred) throws JmriException;

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
    public boolean loadDeferred(File file) throws JmriException;

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
    public boolean loadDeferred(URL file) throws JmriException;

    /**
     * Provide a method-specific way of locating a file to be loaded from a
     * name.
     *
     * @param filename Local filename, perhaps without path information
     * @return Corresponding {@link java.net.URL}
     */
    public URL find(String filename);

    /**
     * Make a backup file.
     *
     * @param file to be backed up
     * @return true if successful
     */
    public boolean makeBackup(File file);

    /**
     * Control the scope of validation of XML files when loading.
     *
     * @param validate the validation scope
     */
    public void setValidate(XmlFile.Validate validate);

    /**
     * Get the scope of validation of XML files when loading.
     *
     * @return the validation scope
     */
    public XmlFile.Validate getValidate();
}
