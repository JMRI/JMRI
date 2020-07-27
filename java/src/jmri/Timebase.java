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

    /**
     * Get the current time.
     * @return current time.
     */
    @Nonnull
    public Date getTime();

    /**
     * Set if Timebase is running.
     * @param y true if running else false.
     */
    public void setRun(boolean y);

    /**
     * Get if Timebase is running.
     * @return true if running, else false.
     */
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
    /**
     * Set internalMaster and update fields.
     *
     * @param master true if fast clock time is derived from internal computer clock, 
     *                  false if derived from hardware clock.
     * @param update true to send update, else false.
     */
    public void setInternalMaster(boolean master, boolean update);

    /**
     * Get internalMaster field.
     *
     * @return true if fast clock time is derived from internal computer clock, 
     *  false if derived from hardware clock
     */
    public boolean getInternalMaster();

    /**
     * Set the Master Clock Name.
     * @param name master clock name.
     */
    public void setMasterName(@Nonnull String name);

    /**
     * Get the Master Clock Name.
     * @return master clock name.
     */
    public String getMasterName();

    /**
     * Set if clock should synchronise.
     * @param synchronize  set true to synchronise hardware clocks with Time base.
     * @param update set true to update clock when function called.
     */
    public void setSynchronize(boolean synchronize, boolean update);

    /**
     * Get if clock should synchronise with Time base.
     * @return true if should synchronise hardware clocks.
     */
    public boolean getSynchronize();

    /**
     * Set if should correct or update hardware.
     * @param correct set true to correct hardware clocks.
     * @param update set true to update clock when function called.
     */
    public void setCorrectHardware(boolean correct, boolean update);

    /**
     * Get if should correct Hardware clocks.
     * @return true to correct, else false.
     */
    public boolean getCorrectHardware();

    /**
     * Set 12 or 24 hour display option.
     *
     * @param display true for a 12-hour display; false for a 24-hour display
     * @param update true to update clock when function called.
     */
    public void set12HourDisplay(boolean display, boolean update);

    /**
     * Get 12 or 24 hour display option.
     *
     * @return true for a 12-hour display; false for a 24-hour display
     */
    public boolean use12HourDisplay();

    /**
     * Defines what to do with the fast clock when JMRI starts up.
     */
    enum ClockInitialRunState {
        /**
         * Changes the clock to stopped when JMRI starts.
         */
        DO_STOP,
        /**
         * Changes the clock to running when JMRI starts.
         */
        DO_START,
        /**
         * Does not change the clock when JMRI starts.
         */
        DO_NOTHING
    }

    /**
     * Set the Clock Initial Run State ENUM.
     * @param initialState Initial state.
     */

    public void setClockInitialRunState(ClockInitialRunState initialState);

    /**
     * Get the Clock Initial Run State ENUM.
     * @return Initial state.
     */
    public ClockInitialRunState getClockInitialRunState();

    /**
     * Set if to show a Stop / Resume button next to the clock.
     * @param displayed true if to display, else false.
     */
    public void setShowStopButton(boolean displayed);

    /**
     * Get if to show a Stop / Resume button next to the clock.
     * @return true if to display, else false.
     */
    public boolean getShowStopButton();

    /**
     * Set time at start up option, and start up time.
     * @param set true for set time at startup, else false.
     * @param time startup time.
     */
    public void setStartSetTime(boolean set, Date time);

    /**
     * Get if to use a set start time.
     * @return true if using set start time.
     */
    public boolean getStartSetTime();

    /**
     * Set the start clock speed rate.
     * @param factor start clock speed factor.
     */
    public void setStartRate(double factor);

    /**
     * Get the startup clock speed rate.
     * @return startup clock speed rate factor.
     */
    public double getStartRate();

    /**
     * Set Set Rate at Start option.
     * @param set If true, the rate at startup will be set to the value of getStartRate().
     */
    public void setSetRateAtStart(boolean set);

    /**
     * Get if to Set Rate at Start option checked.
     * @return If true, the rate at startup should be set to the value of getStartRate()
     */
    public boolean getSetRateAtStart();

    /**
     * Get the Clock Start Time.
     * @return Clock Start Time.
     */
    @Nonnull
    public Date getStartTime();

    /**
     * Set the Start Clock type Option.
     * @param option Clock type, e.g. NIXIE_CLOCK or PRAGOTRON_CLOCK
     */
    public void setStartClockOption(int option);

    
    /**
     * Get the Start Clock Type.
     * @return Clock type, e.g. NIXIE_CLOCK or PRAGOTRON_CLOCK
     */
    public int getStartClockOption();

    /**
     * Initialise the clock.
     * Should only be invoked at start up.
     */
    public void initializeClock();

    /**
     * Clock start option.
     * No startup clock type.
     */
    public static final int NONE = 0x00;
    /**
     * Clock start option.
     * Startup Nixie clock type.
     */
    public static final int NIXIE_CLOCK = 0x01;
    /**
     * Clock start option.
     * Startup Analogue clock type.
     */
    public static final int ANALOG_CLOCK = 0x02;
    /**
     * Clock start option.
     * Startup LCD clock type.
     */
    public static final int LCD_CLOCK = 0x04;
    /**
     * Clock start option.
     * Startup Pragotron clock type.
     */
    public static final int PRAGOTRON_CLOCK = 0x08;

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
