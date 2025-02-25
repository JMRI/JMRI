package jmri.jmrit.simpleclock;

import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Date;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Provide basic Timebase implementation from system clock.
 * <p>
 * This implementation provides for the internal clock and for one hardware
 * clock. A number of hooks and comments are provided below for implementing
 * multiple hardware clocks should that ever be done.
 * <p>
 * The setTimeValue member is the fast time when the clock started. The
 * startAtTime member is the wall-clock time when the clock was started.
 * Together, those can be used to calculate the current fast time.
 * <p>
 * The pauseTime member is used to indicate that the Timebase was paused. If
 * non-null, it indicates the current fast time when the clock was paused.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007 Dave Duchamp - 2007
 *         additions/revisions for handling one hardware clock
 */
public class SimpleTimebase extends jmri.implementation.AbstractNamedBean implements Timebase {

    public static final double MINIMUM_RATE = 0.1;
    public static final double MAXIMUM_RATE = 100;

    private final FastClock fastClock;

    protected final SystemConnectionMemo memo;

    public SimpleTimebase(InternalSystemConnectionMemo memo) {
        super("SIMPLECLOCK");
        this.memo = memo;
        fastClock = InstanceManager.getDefault(FastClock.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTime");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getTime() {
        return fastClock.getTime().getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(Date d) {
        fastClock.setTime(d.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(Instant i) {
        fastClock.setTime(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userSetTime(Date d) {
        fastClock.userSetTime(d.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRun(boolean run) {
        fastClock.setRun(run);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRun() {
        return fastClock.getRun();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRate(double factor) throws TimebaseRateException {
        fastClock.setRate(factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userSetRate(double factor) throws TimebaseRateException {
        fastClock.userSetRate(factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRate() {
        return fastClock.getRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double userGetRate() {
        return fastClock.userGetRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInternalMaster(boolean master, boolean update) {
        fastClock.setInternalMaster(master, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getInternalMaster() {
        return fastClock.getInternalMaster();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMasterName(String name) {
        fastClock.setMasterName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMasterName() {
        return fastClock.getMasterName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSynchronize(boolean synchronize, boolean update) {
        fastClock.setSynchronize(synchronize, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSynchronize() {
        return fastClock.getSynchronize();
    }

    /**
     * If update true, calls initializeHardwareClock.
     * {@inheritDoc}
     */
    @Override
    public void setCorrectHardware(boolean correct, boolean update) {
        fastClock.setCorrectHardware(correct, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCorrectHardware() {
        return fastClock.getCorrectHardware();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set12HourDisplay(boolean display, boolean update) {
        fastClock.set12HourDisplay(display, update);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean use12HourDisplay() {
        return fastClock.use12HourDisplay();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClockInitialRunState(ClockInitialRunState state) {
        fastClock.setClockInitialRunState(FastClock.ClockInitialRunState.valueOf(state.name()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockInitialRunState getClockInitialRunState() {
        return ClockInitialRunState.valueOf(fastClock.getClockInitialRunState().name());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowStopButton(boolean displayed) {
        fastClock.setShowStopButton(displayed);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getShowStopButton() {
        return fastClock.getShowStopButton();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartSetTime(boolean set, Date time) {
        fastClock.setStartSetTime(set, time.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getStartSetTime() {
        return fastClock.getStartSetTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartRate(double factor) {
        fastClock.setStartRate(factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getStartRate() {
        return fastClock.getStartRate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSetRateAtStart(boolean set) {
        fastClock.setSetRateAtStart(set);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSetRateAtStart() {
        return fastClock.getSetRateAtStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartTime() {
        return fastClock.getStartTime().getTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartClockOption(int option) {
        fastClock.setStartClockOption(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStartClockOption() {
        return fastClock.getStartClockOption();
    }

    /**
     * The following method should only be invoked at start up.
     * {@inheritDoc}
     */
    @Override
    public void initializeClock() {
        fastClock.initializeClock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeHardwareClock() {
        fastClock.initializeHardwareClock();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsInitialized() {
        return fastClock.getIsInitialized();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMinuteChangeListener(PropertyChangeListener l) {
        fastClock.addMinuteChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        fastClock.removeMinuteChangeListener(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return fastClock.getMinuteChangeListeners();
    }

    /**
     * Implementation does nothing.
     * {@inheritDoc}
     */
    @Override
    public void setState(int s) throws jmri.JmriException {
    }

    /**
     * Implementation returns 0 .
     * {@inheritDoc}
     */
    @Override
    public int getState() {
        return 0;
    }

//    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleTimebase.class);

}
