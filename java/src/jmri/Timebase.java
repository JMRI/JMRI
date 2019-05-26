package jmri;

import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Date;
import javax.annotation.Nonnull;

/**
 * Provide access to clock capabilities in hardware or software.
 * <p>
 * The {@code rate} property determines how much faster than real time this
 * runs. For example, a value of 2.0 means that the value returned by getTime
 * will advance an hour for every half-hour of wall-clock time elapsed.
 * <p>
 * The {@code rate} and {@code run} properties are bound, so you can listen for
 * changes to them. The {@code time} property is bound, but listeners only
 * receive change notifications if the change is a minute or more, because it
 * changes continuously; query to {@code time} property when needed to get the
 * current value.
 * 
 * @author Bob Jacobsen Copyright (C) 2004, 2007, 2008
 */
public interface Timebase extends NamedBean {

    /**
     * Set the current time.
     *
     * @param d the new time
     */
    public void setTime(@Nonnull Date d);

    /**
     * Set the current time.
     *
     * @param i the new time
     */
    public void setTime(@Nonnull Instant i);

    /**
     * Set the current time and force a synchronization with the DCC system.
     *
     * @param d the new time
     */
    public void userSetTime(@Nonnull Date d);

    @Nonnull
    public Date getTime();

    public void setRun(boolean y);

    public boolean getRun();

    /**
     * Set fast clock rate.
     *
     * @param factor the fast clock rate
     * @throws jmri.TimebaseRateException if the implementation can not use the
     *                                        requested rate
     */
    public void setRate(double factor) throws TimebaseRateException;

    /**
     * Set fast clock rate and force a synchronization with the DCC hardware.
     *
     * @param factor the fast clock rate
     * @throws jmri.TimebaseRateException if the implementation can not use the
     *                                        requested rate
     */
    public void userSetRate(double factor) throws TimebaseRateException;

    /**
     * Caution: This method may return a fiddled clock rate if certain hardware
     * clocks are the Time Source. Use {@link #userGetRate()} if you want the
     * real clock rate instead.
     *
     * @return the rate
     */
    public double getRate();

    /**
     * Get the true fast clock rate even if the master timebase rate has been
     * modified by a hardware clock. External changes in fast clock rate occur
     * because of the peculiar way some hardware clocks attempt to synchronize
     * with the JMRI fast clock.
     *
     * @return the rate
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
     * Set 12 or 24 hour display option.
     *
     * @param display true for a 12-hour display; false for a 24-hour display
     * @param update  true to set display for external fast clocks; false
     *                    otherwise
     */
    public void set12HourDisplay(boolean display, boolean update);

    public boolean use12HourDisplay();

    /**
     * Defines what to do with the fast clock when JMRI starts up.
     */
    enum ClockInitialRunState {
        // Changes the clock to stopped when JMRI starts.
        DO_STOP,
        // Changes the clock to running when JMRI starts.
        DO_START,
        // Does not change the clock when JMRI starts.
        DO_NOTHING
    }

    // methods for start up with clock stopped/started/nochange option
    public void setClockInitialRunState(ClockInitialRunState initialState);

    public ClockInitialRunState getClockInitialRunState();

    // methods for start up with start/stop button displayed
    public void setShowStopButton(boolean displayed);

    public boolean getShowStopButton();

    // methods to get set time at start up option, and start up time  
    public void setStartSetTime(boolean set, Date time);

    public boolean getStartSetTime();

    // What to set the rate at startup.
    public void setStartRate(double factor);

    public double getStartRate();

    // If true, the rate at startup will be set to the value of getStartRate().
    public void setSetRateAtStart(boolean set);

    public boolean getSetRateAtStart();

    @Nonnull
    public Date getStartTime();

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
     * Initialize hardware clock at start up after all options are set up.
     * <p>
     * Note: This method is always called at start up. It should be ignored if
     * there is no communication with a hardware clock
     */
    public void initializeHardwareClock();

    /**
     * @return true if call to initialize Hardware Clock has occurred
     */
    public boolean getIsInitialized();

    /**
     * Request a callback when the minutes place of the time changes. This is
     * the same as calling
     * {@link #addPropertyChangeListener(String, PropertyChangeListener)} with
     * the propertyName {@code minutes}.
     *
     * @param l the listener to receive the callback
     */
    public void addMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Remove a request for callback when the minutes place of the time changes.
     * This is the same as calling
     * {@link #removePropertyChangeListener(String, PropertyChangeListener)}
     * with the propertyName {@code minutes}.
     *
     * @param l the listener to receive the callback
     */
    public void removeMinuteChangeListener(@Nonnull PropertyChangeListener l);

    /**
     * Get the list of minute change listeners. This is the same as calling
     * {@link #getPropertyChangeListeners(String)} with the propertyName
     * {@code minutes}.
     *
     * @return the list of listeners
     */
    @Nonnull
    public PropertyChangeListener[] getMinuteChangeListeners();

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     */
    @Override
    public void dispose();

}
