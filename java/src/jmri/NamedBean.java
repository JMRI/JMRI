package jmri;

import java.util.ArrayList;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Provides common services for classes representing objects on the layout, and
 * allows a common form of access by their Managers.
 * <P>
 * Each object has a two names. The "user" name is entirely free form, and can
 * be used for any purpose. The "system" name is provided by the system-specific
 * implementations, and provides a unique mapping to the layout control system
 * (e.g. LocoNet, NCE, etc) and address within that system.
 *
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
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004
 * @see jmri.Manager
 */
public interface NamedBean {

    /**
     * Constant representing an "unknown" state, indicating that the object's
     * state is not necessarily that of the actual layout hardware. This is the
     * initial state of a newly created object before communication with the
     * layout.
     */
    public static final int UNKNOWN = 0x01;

    /**
     * Constant representing an "inconsistent" state, indicating that some
     * inconsistency has been detected in the hardware readback.
     */
    public static final int INCONSISTENT = 0x08;

    /**
     * User's identification for the item. Bound parameter so manager(s) can listen
     * @return null if not set
     */
    public @Nullable String getUserName();

    public void setUserName(@Nullable String s);

    /**
     * Get a system-specific name. This encodes the hardware addressing
     * information.
     */
    public @Nonnull String getSystemName();

    /**
     * return user name if it exists, otherwise return System name
     */
    public @Nonnull String getDisplayName();

    /**
     * Returns a fully formatted display that includes the SystemName and
     * UserName if set. UserName (SystemName) or SystemName
     */
    public @Nonnull String getFullyFormattedDisplayName();

    /**
     * Request a call-back when a bound property changes. Bound properties are
     * the known state, commanded state, user and system names.
     *
     * @param l           - Listener - although the standard call to 
     *                    addPropertyChangeListener(PropertyChangeListener) accepts a null, that
     *                    doesn't make a lot of sense here.  We've annotated it out.
     *                    A future improvement would be to create a NamedPropertyChangeListener
     *                    subclass that carries the name and listenerRef values internally
     * @param name        - The name (either system or user) that the listener
     *                    uses for this namedBean, this parameter is used to
     *                    help determine when which listeners should be moved
     *                    when the username is moved from one bean to another.
     * @param listenerRef - A textual reference for the listener, that can be
     *                    presented to the user when a delete is called
     */
    public void addPropertyChangeListener(@Nonnull java.beans.PropertyChangeListener l, String name, String listenerRef);

    public void addPropertyChangeListener(@Nullable java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(@Nullable java.beans.PropertyChangeListener l);

    public void updateListenerRef(@Nonnull java.beans.PropertyChangeListener l, String newName);

    public void vetoableChange(@Nonnull java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException;

    /**
     * Get the textual reference for the specific listener
     *
     */
    public String getListenerRef(@Nonnull java.beans.PropertyChangeListener l);

    /**
     * Returns a list of all the listerners references
     */
    public ArrayList<String> getListenerRefs();

    /**
     * Number of current listeners. May return -1 if the information is not
     * available for some reason.
     */
    public int getNumPropertyChangeListeners();

    /**
     * Get a list of all the property change listeners that are registered using
     * a specific name
     *
     * @param name - The name (either system or user) that the listener has
     *             registered as referencing this namedBean
     * @return empty list if none
     */
    public @Nonnull java.beans.PropertyChangeListener[] getPropertyChangeListeners(@Nonnull String name);

    /**
     * Deactivate this object, so that it releases as many resources as possible
     * and no longer effects others.
     * <P>
     * For example, if this object has listeners, after a call to this method it
     * should no longer notify those listeners. Any native or system-wide
     * resources it maintains should be released, including threads, files, etc.
     * <P>
     * It is an error to invoke any other methods on this object once dispose()
     * has been called. Note, however, that there is no guarantee about behavior
     * in that case.
     * <P>
     * Afterwards, references to this object may still exist elsewhere,
     * preventing its garbage collection. But it's formally dead, and shouldn't
     * be keeping any other objects alive. Therefore, this method should null
     * out any references to other objects that this NamedBean contained.
     */
    public void dispose();  // remove _all_ connections!

    /**
     * Provide generic access to internal state.
     * <P>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead. (E.g. setCommandedState in Turnout) This provided to make
     * Jython script access easier to read.
     *
     * @throws JmriException general error when cant do the needed operation
     */
    public void setState(int s) throws JmriException;

    /**
     * Provide generic access to internal state.
     * <P>
     * This generally shouldn't be used by Java code; use the class-specific
     * form instead. (E.g. getCommandedState in Turnout) This provided to make
     * Jython script access easier to read.
     */
    public int getState();

    /**
     * Get associated comment text.
     * @return null if no comment
     */
    public @Nullable String getComment();

    /**
     * Set associated comment text.
     * <p>
     * Comments can be any valid text.
     *
     * @param comment Null means no comment associated.
     */
    public void setComment(@Nullable String comment);

    /**
     * Attach a key/value pair to the NamedBean, which can be retrieved later.
     * These are not bound properties as yet, and don't throw events on
     * modification. Key must not be null.
     *<p>
     * Prior to JMRI 4.3, the key was of Object type.  It was 
     * constrained to String to make these more like normal Java Beans.
     */
    public void setProperty(@Nonnull String key, Object value);

    /**
     * Retrieve the value associated with a key. If no value has been set for
     * that key, returns null.
     */
    public @Nullable Object getProperty(@Nonnull String key);

    /**
     * Remove the key/value pair against the NamedBean.
     */
    public void removeProperty(@Nonnull String key);

    /**
     * Retrieve the complete current set of keys.
     * @return empty set if none
     */
    public @Nonnull java.util.Set<String> getPropertyKeys();

    /**
     * For instances in the code where we are dealing with just a bean and a
     * message needs to be passed to the user or in a log.
     *
     * @return a string of the bean type, eg Turnout, Sensor etc; never null
     */
    public @Nonnull String getBeanType();
}

