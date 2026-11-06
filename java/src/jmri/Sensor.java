package jmri;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

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
    static final int ACTIVE = DigitalIO.ON;
    static final int INACTIVE = DigitalIO.OFF;

    // Max value for Debounce Parameter
    static final Long MAX_DEBOUNCE = 9999999L;

    /**
     * String constant for the Active Timer property.
     */
    String PROPERTY_ACTIVE_TIMER = "ActiveTimer";

    /**
     * String constant for the InActive Timer property.
     */
    String PROPERTY_INACTIVE_TIMER = "InActiveTimer";

    /**
     * String constant for the Global Timer property.
     */
    String PROPERTY_GLOBAL_TIMER = "GlobalTimer";

    /**
     * String constant for the inverted property.
     */
    String PROPERTY_SENSOR_INVERTED = "inverted";

    /** {@inheritDoc} */
    @Override
    default boolean isConsistentState() {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    default void setCommandedState(int s) {
        try {
            setState(s);
        } catch (JmriException ex) {
            log.error("setCommandedState", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    default int getCommandedState() {
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
    void setKnownState(int newState) throws jmri.JmriException;

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
    void setInverted(boolean inverted);

    /**
     * Get the inverted state.
     *
     * @return true if the electrical signal that normally results in an ACTIVE
     *         state now results in an INACTIVE state; false otherwise
     */
    boolean getInverted();

    /**
     * Determine if sensor can be inverted. When a turnout is inverted the
     * {@link #ACTIVE} and {@link #INACTIVE} states are inverted on the layout.
     *
     * @return true if can be inverted; false otherwise
     */
    boolean canInvert();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    void dispose();  // remove _all_ connections!

    /**
     * Used to return the Raw state of a sensor prior to the known state of a
     * sensor being set. The raw state value can be different from the known
     * state when the sensor debounce option is used.
     *
     * @return raw state value
     */
    int getRawState();

    /**
     * Told of every change of a Sensor's raw state, before any debounce, and
     * when it happened.
     * <p>
     * A raw state change is what the layout reported, after any inversion.
     * With a debounce, the known state follows it only if it lasts; without
     * one, the known state changes at once, just after this listener is
     * told. Unlike a property change listener, this is told nothing else, so
     * nothing that listens to a Sensor's properties is affected by it.
     *
     * @since 5.17.5
     */
    @FunctionalInterface
    interface RawStateListener {

        /**
         * The raw state of a Sensor has changed. Called on the thread that
         * set the state, usually the layout thread; keep it short.
         *
         * @param sensor    the Sensor
         * @param oldState  the raw state before
         * @param newState  the raw state now
         * @param timeNanos when the change was received, on the
         *                  {@link System#nanoTime()} scale: as soon as the
         *                  connection read it where the connection records
         *                  that, otherwise when the Sensor was set. Only
         *                  differences between such times have a meaning.
         */
        void rawStateChanged(@Nonnull Sensor sensor, int oldState, int newState, long timeNanos);
    }

    /**
     * Ask to be told of every raw state change of this Sensor, with when it
     * happened. Does nothing for a Sensor that doesn't keep a raw state.
     *
     * @param listener the listener
     * @since 5.17.5
     */
    default void addRawStateListener(@Nonnull RawStateListener listener) {
    }

    /**
     * Stop telling a listener of raw state changes.
     *
     * @param listener the listener
     * @since 5.17.5
     */
    default void removeRawStateListener(@Nonnull RawStateListener listener) {
    }

    /**
     * Set the active debounce delay.
     *
     * @param timer delay in milliseconds; set to zero to de-activate debounce
     */
    void setSensorDebounceGoingActiveTimer(long timer);

    /**
     * Get the active debounce delay.
     *
     * @return delay in milliseconds
     */
    long getSensorDebounceGoingActiveTimer();

    /**
     * Set the inactive debounce delay.
     *
     * @param timer delay in milliseconds; set to zero to de-activate debounce
     */
    void setSensorDebounceGoingInActiveTimer(long timer);

    /**
     * Get the inactive debounce delay.
     *
     * @return delay in milliseconds
     */
    long getSensorDebounceGoingInActiveTimer();

    /**
     * Use the timers specified in the {@link jmri.SensorManager} for the
     * debounce delay.
     * @since 4.9.2
     *
     * @param flag true to set to current defaults if not previously true
     */
    void setUseDefaultTimerSettings(boolean flag);

    /**
     * Does this sensor use the default timers values? (A remarkably unfortunate
     * name given the one above)
     * @since 4.9.2
     *
     * @return true if using default debounce values from the
     *         {@link jmri.SensorManager}
     */
    boolean getUseDefaultTimerSettings();

    /**
     * Some sensor boards also serve the function of being able to report back
     * train identities via such methods as RailCom. The setting and creation of
     * the reporter against the sensor should be done when the sensor is
     * created. This information is not saved.
     *
     * @param re the reporter to associate with the sensor
     */
    void setReporter(@CheckForNull Reporter re);

    /**
     * Retrieve the reporter associated with this sensor if there is one.
     *
     * @return the reporter or null if there is no associated reporter
     */
    @CheckForNull
    Reporter getReporter();

    /*
     * Some sensor types allow us to configure a pull up and/or pull down 
     * resistor at runtime.  The PullResistance enum provides valid values
     * for the pull resistance.  The short name is used in xml files.
     */
    enum PullResistance {
        PULL_UP("up", "PullResistanceUp"), // NOI18N
        PULL_DOWN("down", "PullResistanceDown"), // NOI18N
        PULL_OFF("off", "PullResistanceOff"); // NOI18N

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

        public static PullResistance getByShortName(String shName) {
            for (PullResistance p : PullResistance.values()) {
                if (p.shortName.equals(shName)) {
                    return p;
                }
            }
            throw new java.lang.IllegalArgumentException("argument value " + shName + " not valid");
        }

        public static PullResistance getByPeopleName(String pName) {
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
    void setPullResistance(PullResistance r);

    /**
     * Get the pull resistance
     *
     * @return the currently set PullResistance value.
     */
    PullResistance getPullResistance();

    /**
     * What a sensor's detector sees of a train, which says how far the train
     * reaches beyond the place where the detector saw it.
     * <ul>
     * <li>{@link #WHEELS}, the default: current detection, axle counters and
     * track contacts see the first and last of a vehicle's wheels that they
     * can detect, so the vehicle overhangs them at each end.
     * <li>{@link #BODY}: optical and infrared detectors see the vehicle
     * itself, so little or nothing of it overhangs them.
     * <li>{@link #TAG}: RFID and RailCom readers, and reed and Hall sensors,
     * see one point on one vehicle: a tag, a decoder or a magnet.
     * </ul>
     * It describes the detector only. Nothing in JMRI acts on it by itself;
     * features that work out where a train is can use it.
     *
     * @since 5.19.1
     */
    enum Detects {
        WHEELS("wheels", "SensorDetectsWheels"), // NOI18N
        BODY("body", "SensorDetectsBody"), // NOI18N
        TAG("tag", "SensorDetectsTag"); // NOI18N

        private final String shortName;
        private final String peopleName;

        Detects(String shortName, String peopleKey) {
            this.shortName = shortName;
            this.peopleName = Bundle.getMessage(peopleKey);
        }

        /**
         * @return the name stored in a panel file, which doesn't change with
         *         the language
         */
        @Nonnull
        public String getShortName() {
            return shortName;
        }

        /**
         * @return the name shown to users, in their language
         */
        @Nonnull
        public String getPeopleName() {
            return peopleName;
        }

        /**
         * Get the value for a stored name.
         *
         * @param shortName the name, as {@link #getShortName()} gives it
         * @return the value, or {@link #WHEELS} if the name is null or not
         *         known, so a value stored by a later version reads as the
         *         default
         */
        @Nonnull
        public static Detects getByShortName(@CheckForNull String shortName) {
            for (Detects d : values()) {
                if (d.shortName.equals(shortName)) {
                    return d;
                }
            }
            return WHEELS;
        }

        @Override
        public String toString() {
            return peopleName;
        }
    }

    /**
     * The key of the bean property that stores what this sensor's detector
     * sees, and the name of the property change fired when it changes. Stored
     * as a bean property, it is saved in the panel file with the sensor's
     * other properties, and is kept by versions of JMRI that don't know it.
     *
     * @since 5.19.1
     */
    String PROPERTY_DETECTS = "Detects"; // NOI18N

    /**
     * Get what this sensor's detector sees of a train.
     *
     * @return the value set, or {@link Detects#WHEELS} if none has been
     * @since 5.19.1
     */
    @Nonnull
    @InvokeOnLayoutThread
    default Detects getDetects() {
        Object v = getProperty(PROPERTY_DETECTS);
        return Detects.getByShortName(v == null ? null : v.toString());
    }

    /**
     * Set what this sensor's detector sees of a train. {@link Detects#WHEELS},
     * the default, is not stored, so a panel file only gains an entry for a
     * sensor set to something else. Fires a property change named
     * {@link #PROPERTY_DETECTS}, with the stored names, when the value
     * changes.
     *
     * @param detects the value
     * @since 5.19.1
     */
    @InvokeOnLayoutThread
    default void setDetects(@Nonnull Detects detects) {
        if (detects == Detects.WHEELS) {
            if (getProperty(PROPERTY_DETECTS) != null) {
                // fire the change, then drop the default so it isn't stored
                setProperty(PROPERTY_DETECTS, Detects.WHEELS.getShortName());
                removeProperty(PROPERTY_DETECTS);
            }
        } else {
            setProperty(PROPERTY_DETECTS, detects.getShortName());
        }
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "SLF4J_LOGGER_SHOULD_BE_PRIVATE",
        justification="Private not available in interface")
    static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Sensor.class);

}
