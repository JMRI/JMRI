// Manager.java

package jmri;

import java.util.List;

/**
 * Basic interface for access to named, managed objects.
 * <P>
 * {@link NamedBean} objects represent various real elements, and
 * have a "system name" and perhaps "user name".  A specific Manager
 * object provides access to them by name, and serves as a factory for
 * new objects.
 * <P>
 * Right now, this interface just contains the members needed
 * by {@link InstanceManager} to handle
 * managers for more than one system.
 * <P>
 * Although they are not defined here because their return type differs, any
 * specific Manager subclass
 * provides "get" methods to locate specific objects, and a "new" method
 * to create a new one via the Factory pattern.
 * The "get" methods will
 * return an existing object or null, and will never create a new object.
 * The "new" method will log a warning if an object already exists with
 * that system name.
 * <P>
 * add/remove PropertyChangeListener methods are provided. At a minimum,
 * subclasses must notify of changes to the list of available NamedBeans;
 * they may have other properties that will also notify.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author      Bob Jacobsen Copyright (C) 2003
 * @version	$Revision$
 */
public interface Manager {

    /**
     * @return The system-specific prefix letter for a specific implementation
     * @deprecated 2.9.5 Use getSystemPrefix
     */
    @Deprecated
    public char systemLetter();

    /**
     * Provides access to the system prefix string.
     * This was previously called the "System letter"
     */
    public String getSystemPrefix();

    /**
     * @return The type letter for a specific implementation
     */
    public char typeLetter();

    /**
     * @return A system name from a user input, typically a number.
     */
    public String makeSystemName(String s);

    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose();

    public String[] getSystemNameArray();
    public List<String> getSystemNameList();
    public List<NamedBean> getNamedBeanList();
    
    /**
     * Locate an instance based on a system name.  Returns null if no
     * instance already exists.
     * @param systemName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getBeanBySystemName(String systemName);
    
    /**
     * Locate an instance based on a user name.  Returns null if no
     * instance already exists.
     * @param userName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getBeanByUserName(String userName);
    
    /**
     * Locate an instance based on a name.  Returns null if no
     * instance already exists.
     * @param name System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    public NamedBean getNamedBean(String name);
    
	/**
	 * At a minimum,
 	 * subclasses must notify of changes to the list of available NamedBeans;
     * they may have other properties that will also notify.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);
	/**
	 * At a minimum,
 	 * subclasses must notify of changes to the list of available NamedBeans;
     * they may have other properties that will also notify.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);
    
    /**
	 * Add a VetoableChangeListener to the listener list.
     */
    public void addVetoableChangeListener(java.beans.VetoableChangeListener l);
	/**
	 * Remove a VetoableChangeListener to the listener list.
     */
    public void removeVetoableChangeListener(java.beans.VetoableChangeListener l);
    
    /**
     * Method for a UI to delete a bean, the UI should first request a "CanDelete", this will return a 
     * list of locations (and descriptions) where the bean is in use via throwing a VetoException, then
     * if that comes back clear, or the user agrees with the actions, then a "DoDelete" can be called
     * which inform the listeners to delete the bean, then it will be deregistered and disposed of.
     *
     * if a property name of "DoNotDelete" is thrown back in the VetoException then the delete process
     * should be aborted.
     *
     * @param bean The NamedBean to be deleted
     * @param property The programmatic name of the request
     *                 "CanDelete" will enquire with all listerners if the item can be deleted
     *                 "DoDelete" tells the listerner to delete the item
     * @throws PropertyVetoException - if the recipients wishes the delete to be aborted (see above).
     */
    public void deleteBean(NamedBean n, String property) throws java.beans.PropertyVetoException;
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers
     * use this method extensively.
     */
    public void register(NamedBean n);

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager
     * uses this method.
     */
    public void deregister(NamedBean n);
    
    /**
    * The order in which things get saved to the xml file.
    */
    public static final int SENSORS = 10;
    public static final int TURNOUTS = SENSORS + 10;
    public static final int LIGHTS = TURNOUTS + 10;
    public static final int REPORTERS = LIGHTS + 10;
    public static final int MEMORIES = REPORTERS + 10;
    public static final int SENSORGROUPS = MEMORIES + 10;
    public static final int SIGNALHEADS = SENSORGROUPS + 10;
    public static final int SIGNALMASTS = SIGNALHEADS + 10;
    public static final int SIGNALGROUPS = SIGNALMASTS + 10;
    public static final int BLOCKS = SIGNALGROUPS +10;
    public static final int OBLOCKS = BLOCKS +10;
    public static final int LAYOUTBLOCKS = OBLOCKS +10;
    public static final int SECTIONS = LAYOUTBLOCKS +10;
    public static final int TRANSITS = SECTIONS +10;
    public static final int BLOCKBOSS = TRANSITS +10;
    public static final int ROUTES = BLOCKBOSS + 10;
    public static final int WARRANTS = ROUTES + 10;
    public static final int SIGNALMASTLOGICS = WARRANTS + 10;
    public static final int IDTAGS = SIGNALMASTLOGICS + 10;
    public static final int LOGIXS = IDTAGS + 10;
    public static final int CONDITIONALS = LOGIXS + 10;
    public static final int AUDIO = LOGIXS + 10;
    public static final int TIMEBASE = AUDIO + 10;
    public static final int PANELFILES = TIMEBASE + 10;
    public static final int ENTRYEXIT = PANELFILES + 10;
    
    public int getXMLOrder();
    
    
}


/* @(#)Manager.java */
