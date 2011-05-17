// Sensor.java

package jmri;

/**
 * General input device representation.  Often subclassed for specific
 * types of sensors.
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
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.11 $
 */
public interface Sensor extends NamedBean {

    // states are parameters; both closed and thrown is possible!
    public static final int ACTIVE       = 0x02;
    public static final int INACTIVE     = 0x04;

    /**
     * Known state on layout is a bound parameter
     * @return known state value
     */
    public int getKnownState();

    /**
     * Potentially allow the user to set the known state on the layout.
     * This might not always be available, depending on the limits of the
     * underlying system and implementation.
     */
    public void setKnownState(int newState) throws jmri.JmriException;

    /**
     * Request an update from the layout soft/hardware.  May not even
     * happen, and if it does it will happen later; listen for the result.
     */
    public void requestUpdateFromLayout();

    /**
     * Control whether the actual sensor input is
     * considered to be inverted, e.g. the normal
     * electrical signal that results in an ACTIVE state
     * now results in an INACTIVE state.
     * <p>
     * Changing this changes the state from
     * ACTIVE to INACTIVE and vice-versa, with notifications;
     * UNKNOWN and INCONSISTENT are left unchanged.
     */
    public void setInverted(boolean inverted);
    
    /**
     * Get the inverted state.  If true, the 
     * electrical signal that results in an ACTIVE state
     * now results in an INACTIVE state.
     */
    public boolean getInverted();
    
    /**
     * Request a call-back when the bound KnownState property changes.
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener l);

    /**
     * Remove references to and from this object, so that it can
     * eventually be garbage-collected.
     */
    public void dispose();  // remove _all_ connections!
    
    public void setSensorDebounceGoingActiveTimer(long timer);
    public long getSensorDebounceGoingActiveTimer();
    
    public void setSensorDebounceGoingInActiveTimer(long timer);
    public long getSensorDebounceGoingInActiveTimer();
    
    public void useDefaultTimerSettings(boolean boo);
    public boolean useDefaultTimerSettings();

}


/* @(#)Sensor.java */
