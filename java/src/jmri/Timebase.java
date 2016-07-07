package jmri;

import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Date;
import javax.annotation.Nonnull;

/**
 * Provide access to clock capabilities in hardware or software.
 * <P>
 * The Rate parameter determines how much faster than real time this InstanceManager.getDefault(jmri.Timebase.class)
 * runs. E.g. a value of 2.0 means that the value returned by getTime will
 * advance an hour for every half-hour of wall-clock time elapsed.
 * <P>
 * The Rate and Run parameters are bound, so you can listen for them changing.
 * The Time parameters is not bound, because it changes continuously. Ask for
 * its value when needed, or add a a listener for the changes in the "minute"
 * value using {@link #addMinuteChangeListener}
 * <P>
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
 * @author	Bob Jacobsen Copyright (C) 2004, 2007, 2008
 */
public interface Timebase extends NamedBean {

    /**
     * Set the current time
     */
    public void setTime(@Nonnull Date d);

    /**
     * Set the current time
     * @param i java.time.Instant
     */
    public void setTime(@Nonnull Instant i);

    /**
     * Special method for when the user changes fast clock time in Setup Fast
     * Clock.
     */
    public void userSetTime(@Nonnull Date d);

    public @Nonnull Date getTime();

    public void setRun(boolean y);

    public boolean getRun();

    /**
     * Set fast clock rate factor
     *
     * @throws InstanceManager.getDefault(jmri.Timebase.class)RateException if the implementation can't do the
     *                               requested rate
     */
    public void setRate(double factor) throws TimebaseRateException;

    /**
     * Used when the user changes fast clock rate in Setup Fast Clock and by
     * hardware ClockControl implementations that fiddle with the fast clock
     * rate to synchronize
     */
    public void userSetRate(double factor) throws TimebaseRateException;

    /**
     * Caution: This method may return a fiddled clock rate if certain hardware
     * clocks are the Time Source. Use "userGetRate" if you want the real clock
     * rate instead.
     */
    public double getRate();

    /**
     * This method is used by Setup Fast Clock when an external change in fast
     * clock rate occurs because of the peculiar way some hardware clocks
     * attempt to synchronize with the JMRI fast clock. This call will return
     * the "true" rate even if the master InstanceManager.getDefault(jmri.Timebase.class) rate has been fiddled by a
     * hardware clock.
     */
    public double userGetRate();

    // methods for setting and getting master time source
    public void setInternalMaster(boolean master, boolean update);

    public boolean getInternalMaster();

    // the following provide for choosing among hardware clocks if hardware master		
    public void setMasterName(@Nonnull String name);

    public String getMasterName();

    // methods for setting and getting synchronize option
    public void setSynchronize(boolean synchronize, boolean update);

    public boolean getSynchronize();

    // methods for setting and getting hardware correction option
    public void setCorrectHardware(boolean correct, boolean update);

    public boolean getCorrectHardware();

    /**
     * Set 12 or 24 hour display option
     *
     * @param display true if a 12-hour display is requested, false for 24-hour
     *                display
     */
    public void set12HourDisplay(boolean display, boolean update);

    public boolean use12HourDisplay();

    // methods for start up with clock stopped option
    public void setStartStopped(boolean stopped);

    public boolean getStartStopped();

    // methods to get set time at start up option, and start up time		
    public void setStartSetTime(boolean set, Date time);

    public boolean getStartSetTime();

    public @Nonnull Date getStartTime();

    // methods to get set clock start start up option		
    public void setStartClockOption(int option);

    public int getStartClockOption();

    // Note the following method should only be invoked at start up
    public void initializeClock();

    // clock start options
    public static final int NONE = 0x00;
    public static final int NIXIE_CLOCK = 0x01;
    public static final int ANALOG_CLOCK = 0x02;
    public static final int LCD_CLOCK = 0x04;

    /**
     * Initialize hardware clock at start up after all options are set up.<p>
     * Note: This method is always called at start up. It should be ignored if
     * there is no communication with a hardware clock
     */
    public void initializeHardwareClock();

    /**
     * @return true if call to initialize Hardware Clock has occurred
     */
    public boolean getIsInitialized();

    /**
     * Request a call-back when the bound Rate or Run property changes.
     */
    public void addPropertyChangeListener(PropertyChangeListener l);

    /**
     * Remove a request for a call-back when a bound property changes.
     */
    public void removePropertyChangeListener(PropertyChangeListener l);

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public void addMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Remove a request for call-back when the minutes place of the time
     * changes.
     */
    public void removeMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Get the list of minute change listeners.
     */
    public @Nonnull PropertyChangeListener[] getMinuteChangeListeners();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    public void dispose();

}
