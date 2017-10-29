package jmri;

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Basic interface for access to named, managed objects.
 * <P>
 * {@link NamedBean} objects represent various real elements, and have a "system
 * name" and perhaps "user name". A specific Manager object provides access to
 * them by name, and serves as a factory for new objects.
 * <P>
 * Right now, this interface just contains the members needed by
 * {@link InstanceManager} to handle managers for more than one system.
 * <P>
 * Although they are not defined here because their return type differs, any
 * specific Manager subclass provides "get" methods to locate specific objects,
 * and a "new" method to create a new one via the Factory pattern. The "get"
 * methods will return an existing object or null, and will never create a new
 * object. The "new" method will log a warning if an object already exists with
 * that system name.
 * <P>
 * add/remove PropertyChangeListener methods are provided. At a minimum,
 * subclasses must notify of changes to the list of available NamedBeans; they
 * may have other properties that will also notify.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2003
 */
public interface Manager<E extends NamedBean> {

    /**
     * Provides access to the system prefix string. This was previously called
     * the "System letter"
     *
     * @return the system prefix
     */
    @CheckReturnValue
    @Nonnull
    public String getSystemPrefix();

    /**
     * @return The type letter for a specific implementation
     */
    @CheckReturnValue
    public char typeLetter();

    /**
     * @param s the item to make the system name for
     * @return A system name from a user input, typically a number.
     * @throws IllegalArgumentException if a valid name can't be created
     */
    @Nonnull
    public String makeSystemName(@Nonnull String s);

    /**
     * Code the validity (including just as a prefix)
     * of a proposed name string.
     * @since 4.9.5
     */
    enum NameValidity {
        /**
         * Indicates the name is valid as is,
         * and can also be a valid prefix for longer names
         */
        VALID,
         /**
          * Indicates name is not valid as-is, nor
          * can it be made valid by adding more characters;
          * just a bad start.
          */
        INVALID,
        /**
         * Indicates that adding additional characters might (or might not)
         * turn this into a valid name; it is not a valid name now.
         */
        VALID_AS_PREFIX_ONLY
    }

    /**
     * Test if parameter is a properly formatted system name.
     *
     * @since 4.9.5, although similar methods existed previously in lower-level classes
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    @CheckReturnValue
    public NameValidity validSystemNameFormat(@Nonnull String systemName);

    /**
     * Free resources when no longer used. Specifically, remove all references
     * to and from this object, so it can be garbage-collected.
     */
    public void dispose();

    @CheckReturnValue
    @Nonnull
    public String[] getSystemNameArray();

    @CheckReturnValue
    @Nonnull
    public List<String> getSystemNameList();

    @CheckReturnValue
    @Nonnull
    public List<E> getNamedBeanList();

    /**
     * Locate an instance based on a system name. Returns null if no instance
     * already exists.
     *
     * @param systemName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     * @throws IllegalArgumentException if provided name is invalid
     */
    @CheckReturnValue
    @CheckForNull
    public E getBeanBySystemName(@Nonnull String systemName);

    /**
     * Locate an instance based on a user name. Returns null if no instance
     * already exists.
     *
     * @param userName System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public E getBeanByUserName(@Nonnull String userName);

    /**
     * Locate an instance based on a name. Returns null if no instance already
     * exists.
     *
     * @param name System Name of the required NamedBean
     * @return requested NamedBean object or null if none exists
     */
    @CheckReturnValue
    @CheckForNull
    public E getNamedBean(@Nonnull String name);

    /**
     * At a minimum, subclasses must notify of changes to the list of available
     * NamedBeans; they may have other properties that will also notify.
     *
     * @param l the listener
     */
    public void addPropertyChangeListener(@CheckForNull PropertyChangeListener l);

    /**
     * At a minimum, subclasses must notify of changes to the list of available
     * NamedBeans; they may have other properties that will also notify.
     *
     * @param l the listener
     */
    public void removePropertyChangeListener(@CheckForNull PropertyChangeListener l);

    /**
     * Add a VetoableChangeListener to the listener list.
     *
     * @param l the listener
     */
    public void addVetoableChangeListener(@CheckForNull VetoableChangeListener l);

    /**
     * Remove a VetoableChangeListener to the listener list.
     *
     * @param l the listener
     */
    public void removeVetoableChangeListener(@CheckForNull VetoableChangeListener l);

    /**
     * Method for a UI to delete a bean, the UI should first request a
     * "CanDelete", this will return a list of locations (and descriptions)
     * where the bean is in use via throwing a VetoException, then if that comes
     * back clear, or the user agrees with the actions, then a "DoDelete" can be
     * called which inform the listeners to delete the bean, then it will be
     * deregistered and disposed of.
     * <p>
     * If a property name of "DoNotDelete" is thrown back in the VetoException
     * then the delete process should be aborted.
     *
     * @param n        The NamedBean to be deleted
     * @param property The programmatic name of the request. "CanDelete" will
     *                 enquire with all listeners if the item can be deleted.
     *                 "DoDelete" tells the listener to delete the item.
     * @throws java.beans.PropertyVetoException - If the recipients wishes the
     *                                          delete to be aborted (see
     *                                          above).
     */
    public void deleteBean(@Nonnull E n, @Nonnull String property) throws java.beans.PropertyVetoException;

    /**
     * Remember a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific SignalHeadManagers use this method extensively.
     *
     * @param n the bean
     */
    public void register(@Nonnull E n);

    /**
     * Forget a NamedBean Object created outside the manager.
     * <P>
     * The non-system-specific RouteManager uses this method.
     *
     * @param n the bean
     */
    public void deregister(@Nonnull E n);

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
    public static final int BLOCKS = SIGNALGROUPS + 10;
    public static final int OBLOCKS = BLOCKS + 10;
    public static final int LAYOUTBLOCKS = OBLOCKS + 10;
    public static final int SECTIONS = LAYOUTBLOCKS + 10;
    public static final int TRANSITS = SECTIONS + 10;
    public static final int BLOCKBOSS = TRANSITS + 10;
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

    /**
     * Determine the order that types should be written when storing panel
     * files. Uses one of the constants defined in this class.
     * <p>
     * Yes, that's an overly-centralized methodology, but it works for now.
     *
     * @return write order for this Manager; larger is later.
     */
    @CheckReturnValue
    public int getXMLOrder();

    /**
     * For instances in the code where we are dealing with just a bean and a
     * message needs to be passed to the user or in a log.
     *
     * @return a string of the bean type that the manager handles, eg Turnout,
     *         Sensor etc
     */
    @CheckReturnValue
    @Nonnull
    public String getBeanTypeHandled();

    /**
     * Enforces, and as a user convenience converts to, the standard form for a
     * system name for the NamedBeans handled by this manager.
     *
     * @param inputName System name to be normalized
     * @throws NamedBean.BadSystemNameException If the inputName can't be
     *                                          converted to normalized form
     * @return A system name in standard normalized form
     */
    @CheckReturnValue
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) throws NamedBean.BadSystemNameException;

    /**
     * Get a manager-specific tool tip for adding an entry to the manager.
     *
     * @return the tool tip or null to disable the tool tip
     */
    public default String getEntryToolTip() {
        return null;
    }

}
