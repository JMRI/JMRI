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
 * @version     $Revision: 1.6 $
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
     * @param file
     */
    public void storeAll(File f);

    /**
     * Stores just preferences information.
     * @param file
     */
    public void storePrefs(File f);

    /**
     * Stores just configuration information.
     * @param file
     */
    public void storeConfig(File f);

    /**
     * Stores just user information.
     * @param file
     */
    public void storeUser(File f);

    /**
     * Create the objects defined in a particular configuration
     * file
     * @param f
     * @return true if succeeded
     */
    public boolean load(File f);

    /**
     * Provide a method-specific way of locating a file to be
     * loaded from a name.
     * @param f Local filename, perhaps without path information
     * @return Corresponding File object
     */
    public File find(String f);

}


/* @(#)ConfigureManager.java */
