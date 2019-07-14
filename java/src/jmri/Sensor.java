package jmri;

import javax.annotation.CheckForNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General input device representation. Often subclassed for specific types of
 * sensors.
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface Sensor extends DigitalIO {

    // states are parameters; both closed and thrown is possible!
    public static final int ACTIVE = DigitalIO.ON;
    public static final int INACTIVE = DigitalIO.OFF;

    // MAx value for Debounce Parameter
    public static final Long MAX_DEBOUNCE = 9999999L;


    /** {@inheritDoc} */
    @Override
    default public boolean isConsistentState() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    default public void setCommandedState(int s) {
        try {
            setState(s);
        } catch (JmriException ex) {
            log.error("setCommandedState", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    default public int getCommandedState() {
        return getState();
    }
    
    /**
     * Set the known state on the layout. This might not always be available, or
     * effective, depending on the limits of the underlying system and
     * implementation.
     *
     * @param newState the state to set
     * @throws jmri.JmriException if unable to set the state
     */
    @InvokeOnLayoutThread
    public void setKnownState(int newState) throws jmri.JmriException;

    /**
     * Control whether the actual sensor input is considered to be inverted,
     * such that the normal electrical signal that normally results in an ACTIVE
     * state now results in an INACTIVE state.
     * <p>
     * Changing this changes the state from ACTIVE to INACTIVE and vice-versa,
     * with notifications; UNKNOWN and INCONSISTENT are left unchanged.
     *
     * @param inverted true if the sensor should be inverted; false otherwise
     */
    @InvokeOnLayoutThread
    public void setInverted(boolean inverted);

    /**
     * Get the inverted state.
     *
     * @return true if the electrical signal that normally results in an ACTIVE
     *         state now results in an INACTIVE state; false otherwise
     */
    public boolean getInverted();

    /**
     * Determine if sensor can be inverted. When a turnout is inverted the
     * {@link #ACTIVE} and {@link #INACTIVE} states are inverted on the layout.
     *
     * @return true if can be inverted; false otherwise
     */
    public boolean canInvert();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose();  // remove _all_ connections!

    /**
     * Used to return the Raw state of a sensor prior to the known state of a
     * sensor being set. The raw state value can be different from the known
     * state when the sensor debounce option is used.
     *
     * @return raw state value
     */
    public int getRawState();

    /**
     * Set the active debounce delay.
     *
     * @param timer delay in milliseconds; set to zero to de-activate debounce
     */
    public void setSensorDebounceGoingActiveTimer(long timer);

    /**
     * Get the active debounce delay.
     *
     * @return delay in milliseconds
     */
    public long getSensorDebounceGoingActiveTimer();

    /**
     * Set the inactive debounce delay.
     *
     * @param timer delay in milliseconds; set to zero to de-activate debounce
     */
    public void setSensorDebounceGoingInActiveTimer(long timer);

    /**
     * Get the inactive debounce delay.
     *
     * @return delay in milliseconds
     */
    public long getSensorDebounceGoingInActiveTimer();

    /**
     * Use the timers specified in the {@link jmri.SensorManager} for the
     * debounce delay.
     * @since 4.9.2
     *
     * @param flag true to set to current defaults if not previously true
     */
    public void setUseDefaultTimerSettings(boolean flag);

    /**
     * Does this sensor use the default timers values? (A remarkably unfortunate
     * name given the one above)
     * @since 4.9.2
     *
     * @return true if using default debounce values from the
     *         {@link jmri.SensorManager}
     */
    public boolean getUseDefaultTimerSettings();

    /**
     * Some sensor boards also serve the function of being able to report back
     * train identities via such methods as RailCom. The setting and creation of
     * the reporter against the sensor should be done when the sensor is
     * created. This information is not saved.
     *
     * @param re the reporter to associate with the sensor
     */
    public void setReporter(@CheckForNull Reporter re);

    /**
     * Retrieve the reporter associated with this sensor if there is one.
     *
     * @return the reporter or null if there is no associated reporter
     */
    @CheckForNull
    public Reporter getReporter();

    /*
     * Some sensor types allow us to configure a pull up and/or pull down 
     * resistor at runtime.  The PullResistance enum provides valid values
     * for the pull resistance.  The short name is used in xml files.
     */
    public enum PullResistance {
        PULL_UP("up","PullResistanceUp"), // NOI18N
        PULL_DOWN("down","PullResistanceDown"), // NOI18N
        PULL_OFF("off","PullResistanceOff"); // NOI18N

        PullResistance(String shName, String peopleKey) {
           this.shortName = shName;
           this.peopleName = Bundle.getMessage(peopleKey);
        }

        String shortName;
        String peopleName;

        public String getShortName() {
           return shortName;
        }

        public String getPeopleName() {
           return peopleName;
        }

        static public PullResistance getByShortName(String shName) {
            for (PullResistance p : PullResistance.values()) {
                if (p.shortName.equals(shName)) {
                    return p;
                }
            }
            throw new java.lang.IllegalArgumentException("argument value " + shName + " not valid");
        }

        static public PullResistance getByPeopleName(String pName) {
            for (PullResistance p : PullResistance.values()) {
                if (p.peopleName.equals(pName)) {
                    return p;
                }
            }
            throw new java.lang.IllegalArgumentException("argument value " + pName + " not valid");
        }
 
       @Override
       public String toString(){
          return( peopleName );
       }

    }

    /**
     * Set the pull resistance
     *
     * @param r PullResistance value to use.
     */
    @InvokeOnLayoutThread
    public void setPullResistance(PullResistance r);

    /**
     * Get the pull resistance
     *
     * @return the currently set PullResistance value.
     */
    public PullResistance getPullResistance();


    final static Logger log = LoggerFactory.getLogger(Sensor.class);
    
}
