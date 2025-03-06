package jmri.clock;

import java.beans.PropertyChangeListener;
import java.util.Calendar;

import javax.annotation.Nonnull;

/**
 * The time of a clock.
 *
 * @author Bob Jacobsen      Copyright (C) 2004, 2007, 2008
 * @author Daniel Bergqvist  Copyright (C) 2025
 */
public interface ClockTime extends Clock {

    /**
     * Property Change sent when the minute value changes.
     */
    String PROPERTY_CHANGE_MINUTES = "minutes";

    /**
     * Property Change sent when the minute value changes.
     */
    String PROPERTY_CHANGE_TIME = "time";

    /**
     * Set the current time.
     *
     * @param d the new time
     */
    void setTime(@Nonnull Calendar d);

    /**
     * Set the current time.
     *
     * @param d the new time in milliseconds
     */
    void setTime(@Nonnull long d);

    /**
     * Set the current time and force a synchronization with the DCC system.
     *
     * @param time the new time
     */
    void userSetTime(@Nonnull Calendar time);

    /**
     * Set the current time in milliseconds and force a synchronization with the DCC system.
     *
     * @param time the new time
     */
    void userSetTime(@Nonnull long time);

    /**
     * Get the current time.
     * @return current time.
     */
    @Nonnull
    Calendar getTime();

    /**
     * Set time at start up option, and start up time.
     * @param set true for set time at startup, else false.
     * @param time startup time.
     */
    void setStartSetTime(boolean set, Calendar time);

    /**
     * Get if to use a set start time.
     * @return true if using set start time.
     */
    boolean getStartSetTime();

    /**
     * Get the Clock Start Time.
     * @return Clock Start Time.
     */
    @Nonnull
    Calendar getStartTime();

    /**
     * Request a callback when the minutes place of the time changes. This is
     * the same as calling
     * {@link #addPropertyChangeListener(String, PropertyChangeListener)} with
     * the propertyName {@code minutes}.
     *
     * @param l the listener to receive the callback
     */
    void addMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Remove a request for callback when the minutes place of the time changes.
     * This is the same as calling
     * {@link #removePropertyChangeListener(String, PropertyChangeListener)}
     * with the propertyName {@code minutes}.
     *
     * @param l the listener to receive the callback
     */
    void removeMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Get the list of minute change listeners. This is the same as calling
     * {@link #getPropertyChangeListeners(String)} with the propertyName
     * {@code minutes}.
     *
     * @return the list of listeners
     */
    @Nonnull
    PropertyChangeListener[] getMinuteChangeListeners();

}
