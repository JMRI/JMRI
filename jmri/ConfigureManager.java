// ConfigureManager.java

package jmri;

import java.io.File;

/**
 * Provide load/store capabilities for general configuration.
 * <P>
 * Areas of responsibility:
 * <UL>
 * <LI>Register and deregister configuration objects so they can
 * eventually be stored.
 * <LI>Invoke the load and store operations as needed
 * <LI>Give access to the configuration objects for independent GUIs
 * </UL>
 *<P>
 *The managed items are divided into four types:
 *<OL>
 *<LI>"Prefs" - handled first on read, these are the general preferences
 *controlling how the program starts up
 *<LI>"Config" - layout configuration information, e.g. turnout, signal, etc
 *<LI>"Tool" - (Not really clear yet, but present)
 *<LI>"User" - typically information about panels and windows, these are handled
 *last during startup
 *</OL>
 *<P>
 *The configuration manager is generally located through the InstanceManager.
 *<P>
 *The original implementation was via the {@link jmri.configurexml} package.
 * @author	Bob Jacobsen Copyright (C) 2002
 * @version     $Revision: 1.7 $
 * @see jmri.InstanceManager
 * @see jmri.configurexml.ConfigXmlManager
 */
public interface ConfigureManager {

    public void registerPref(Object o);
    public void removePrefItems();

    public void registerConfig(Object o);
    public void registerTool(Object o);
    public void registerUser(Object o);

    public void deregister(Object o);

    public Object findInstance(Class c, int index);

    /**
     * Stores prefs, config, tools and user information.
     * @param file Output file
     */
    public void storeAll(File file);

    /**
     * Stores just preferences information.
     * @param file Output file
     */
    public void storePrefs(File file);

    /**
     * Stores just configuration information.
     * @param file Output file
     */
    public void storeConfig(File file);

    /**
     * Stores just user information.
     * @param file Output file
     */
    public void storeUser(File file);

    /**
     * Create the objects defined in a particular configuration
     * file
     * @param file Input file
     * @return true if succeeded
     */
    public boolean load(File file);

    /**
     * Provide a method-specific way of locating a file to be
     * loaded from a name.
     * @param filename Local filename, perhaps without path information
     * @return Corresponding File object
     */
    public File find(String filename);

}


/* @(#)ConfigureManager.java */
