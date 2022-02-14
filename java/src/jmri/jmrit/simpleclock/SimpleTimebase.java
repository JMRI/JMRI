package jmri.jmrit.simpleclock;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected final SystemConnectionMemo memo;

    public SimpleTimebase(InternalSystemConnectionMemo memo) {
        super("SIMPLECLOCK");
        this.memo = memo;
        // initialize time-containing memory
        try {
            clockMemory = jmri.InstanceManager.memoryManagerInstance().provideMemory(memo.getSystemPrefix()+"MCURRENTTIME");
            clockMemory.setValue("--");
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to create CURRENTTIME time memory variable");
        }

        init();

    }

    final void init(){

        // set to start counting from now
        setTime(new Date());
        pauseTime = null;
        // initialize start/stop sensor for time running
        try {
            clockSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor(memo.getSystemPrefix()+"SCLOCKRUNNING");
            clockSensor.setKnownState(Sensor.ACTIVE);
            clockSensor.addPropertyChangeListener(this::clockSensorChanged);
        } catch (jmri.JmriException e) {
            log.warn("Exception setting CLOCKRUNNING sensor ACTIVE", e);
        }
        // initialize rate factor-containing memory
        if (jmri.InstanceManager.getNullableDefault(jmri.MemoryManager.class) != null) {
            // only try to create memory if memories are supported
            try {
                factorMemory = jmri.InstanceManager.memoryManagerInstance().provideMemory(memo.getSystemPrefix()+"MRATEFACTOR");
                factorMemory.setValue(userGetRate());
            } catch (IllegalArgumentException ex) {
                log.warn("Unable to create RATEFACTOR time memory variable");
            }
        }

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
        // is clock stopped?
        if (pauseTime != null) {
            return new Date(pauseTime.getTime()); // to ensure not modified outside
        } // clock running
        long elapsedMSec = (new Date()).getTime() - startAtTime.getTime();
        long nowMSec = setTimeValue.getTime() + (long) (mFactor * elapsedMSec);
        return new Date(nowMSec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(Date d) {
        startAtTime = new Date(); // set now in wall clock time
        setTimeValue = new Date(d.getTime()); // to ensure not modified from outside
        if (synchronizeWithHardware) {
            // send new time to all hardware clocks, except the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            if (jmri.InstanceManager.getDefault(jmri.ClockControl.class) != hardwareTimeSource) {
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).setTime(d);
            }
        }
        if (pauseTime != null) {
            pauseTime = setTimeValue; // if stopped, continue stopped at new time
        }
        handleAlarm(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTime(Instant i) {
        setTime(Date.from(i));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userSetTime(Date d) {
        // this call only results from user changing fast clock time in Setup Fast Clock
        startAtTime = new Date(); // set now in wall clock time
        setTimeValue = new Date(d.getTime()); // to ensure not modified from outside
        if (synchronizeWithHardware) {
            // send new time to all hardware clocks, including the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            jmri.InstanceManager.getDefault(jmri.ClockControl.class).setTime(d);
        } else if (!internalMaster && (hardwareTimeSource != null)) {
            // if not synchronizing, send to the hardware time source if there is one
            hardwareTimeSource.setTime(d);
        }
        if (pauseTime != null) {
            pauseTime = setTimeValue; // if stopped, continue stopped at new time
        }
        handleAlarm(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRun(boolean run) {
        if (run && pauseTime != null) {
            // starting of stopped clock
            setTime(pauseTime);
            if (synchronizeWithHardware) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).startHardwareClock(getTime());
            } else if (!internalMaster && hardwareTimeSource != null) {
                hardwareTimeSource.startHardwareClock(getTime());
            }
            pauseTime = null;
            if (clockSensor != null) {
                try {
                    clockSensor.setKnownState(Sensor.ACTIVE);
                } catch (jmri.JmriException e) {
                    log.warn("Exception setting ISClockRunning sensor ACTIVE", e);
                }
            }
        } else if (!run && pauseTime == null) {
            // stopping of running clock:
            // Store time it was stopped, and stop it
            pauseTime = getTime();
            if (synchronizeWithHardware) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).stopHardwareClock();
            } else if (!internalMaster && hardwareTimeSource != null) {
                hardwareTimeSource.stopHardwareClock();
            }
            if (clockSensor != null) {
                try {
                    clockSensor.setKnownState(Sensor.INACTIVE);
                } catch (jmri.JmriException e) {
                    log.warn("Exception setting ISClockRunning sensor INACTIVE", e);
                }
            }
        }
        firePropertyChange("run", !run, run); // old, then new
        handleAlarm(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getRun() {
        return pauseTime == null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRate(double factor) throws TimebaseRateException {
        if (factor < MINIMUM_RATE || factor > MAXIMUM_RATE) {
            log.error("rate of {} is out of reasonable range", factor);
            throw new TimebaseRateException();
        }
        if (internalMaster && (!notInitialized)) {
            log.error("Probable Error - questionable attempt to change fast clock rate");
        }
        double oldFactor = mFactor;
        Date now = getTime();
        // actually make the change
        mFactor = factor;
        if (internalMaster || notInitialized) {
            hardwareFactor = factor;
        }
        if (internalMaster || (synchronizeWithHardware && notInitialized)) {
            // send new rate to all hardware clocks, except the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            if (jmri.InstanceManager.getDefault(jmri.ClockControl.class) != hardwareTimeSource) {
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).setRate(factor);
            }
        }
        // make sure time is right with new rate
        setTime(now);
        // notify listeners if internal master
        if (internalMaster) {
            firePropertyChange("rate", oldFactor, factor); // old, then new
        }
        handleAlarm(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void userSetRate(double factor) throws TimebaseRateException {
        // this call is used when user changes fast clock rate either in Setup Fast Clock or via a ClockControl
        // implementation
        if (factor < MINIMUM_RATE || factor > MAXIMUM_RATE) {
            log.error("rate of {} is out of reasonable range", factor);
            throw new TimebaseRateException();
        }
        double oldFactor = hardwareFactor;
        Date now = getTime();
        // actually make the change
        mFactor = factor;
        hardwareFactor = factor;
        if (synchronizeWithHardware) {
            // send new rate to all hardware clocks, including the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            jmri.InstanceManager.getDefault(jmri.ClockControl.class).setRate(factor);
        } else if (!internalMaster && (hardwareTimeSource != null)) {
            // if not synchronizing, send to the hardware time source if there is one
            hardwareTimeSource.setRate(factor);
        }
        // make sure time is right with new rate
        setTime(now);
        // update memory
        updateMemory(factor);
        // notify listeners
        firePropertyChange("rate", oldFactor, factor); // old, then new
        handleAlarm(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getRate() {
        return mFactor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double userGetRate() {
        return ( internalMaster ? mFactor : hardwareFactor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInternalMaster(boolean master, boolean update) {
        if (master != internalMaster) {
            internalMaster = master;
            if (internalMaster) {
                mFactor = hardwareFactor; // get rid of any fiddled rate present
            }
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(userGetRate(),
                        getTime(), false);
            }

            if (internalMaster) {
                masterName = "";
                hardwareTimeSource = null;
            } else {
                // Note if there are multiple hardware clocks, this should be changed to correctly
                // identify which hardware clock has been chosen-currently assumes only one
                hardwareTimeSource = jmri.InstanceManager.getDefault(jmri.ClockControl.class);
                masterName = hardwareTimeSource.getHardwareClockName();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getInternalMaster() {
        return internalMaster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMasterName(String name) {
        if (!internalMaster) {
            masterName = name;
            // if multiple clocks, this must be replaced by a loop over all hardware clocks to identify
            // the one that is the hardware time source
            hardwareTimeSource = jmri.InstanceManager.getDefault(jmri.ClockControl.class);
        } else {
            masterName = "";
            hardwareTimeSource = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMasterName() {
        return masterName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSynchronize(boolean synchronize, boolean update) {
        if (synchronizeWithHardware != synchronize) {
            synchronizeWithHardware = synchronize;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(
                   userGetRate(), getTime(), false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSynchronize() {
        return synchronizeWithHardware;
    }

    /**
     * If update true, calls initializeHardwareClock.
     * {@inheritDoc}
     */
    @Override
    public void setCorrectHardware(boolean correct, boolean update) {
        if (correctHardware != correct) {
            correctHardware = correct;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(
                userGetRate(), getTime(), false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getCorrectHardware() {
        return correctHardware;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set12HourDisplay(boolean display, boolean update) {
        if (display != display12HourClock) {
            display12HourClock = display;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(
                userGetRate(), getTime(), false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean use12HourDisplay() {
        return display12HourClock;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setClockInitialRunState(ClockInitialRunState state) {
        initialState = state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClockInitialRunState getClockInitialRunState() {
        return initialState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setShowStopButton(boolean displayed) {
        showStopButton = displayed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getShowStopButton() {
        return showStopButton;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartSetTime(boolean set, Date time) {
        startSetTime = set;
        startTime = new Date(time.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getStartSetTime() {
        return startSetTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartRate(double factor) {
        startupFactor = factor;
        haveStartupFactor = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getStartRate() {
        if (haveStartupFactor) {
            return startupFactor;
        } else {
            return userGetRate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSetRateAtStart(boolean set) {
        startSetRate = set;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getSetRateAtStart() {
        return startSetRate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartTime() {
        return new Date(startTime.getTime());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartClockOption(int option) {
        startClockOption = option;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getStartClockOption() {
        return startClockOption;
    }

    /**
     * The following method should only be invoked at start up.
     * {@inheritDoc}
     */
    @Override
    public void initializeClock() {
        switch (startClockOption) {
            case NIXIE_CLOCK:
                jmri.jmrit.nixieclock.NixieClockFrame f = new jmri.jmrit.nixieclock.NixieClockFrame();
                f.setVisible(true);
                break;
            case ANALOG_CLOCK:
                jmri.jmrit.analogclock.AnalogClockFrame g = new jmri.jmrit.analogclock.AnalogClockFrame();
                g.setVisible(true);
                break;
            case LCD_CLOCK:
                jmri.jmrit.lcdclock.LcdClockFrame h = new jmri.jmrit.lcdclock.LcdClockFrame();
                h.setVisible(true);
                break;
            case PRAGOTRON_CLOCK:
                jmri.jmrit.pragotronclock.PragotronClockFrame p = new jmri.jmrit.pragotronclock.PragotronClockFrame();
                p.setVisible(true);
                break;
            default:
                log.debug("initializeClock() called with invalid startClockOption: {}", startClockOption);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeHardwareClock() {
        boolean startStopped = (initialState == ClockInitialRunState.DO_STOP);
        if (synchronizeWithHardware || correctHardware) {
            if (startStopped) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(0,
                        getTime(), (!internalMaster && !startSetTime));
            } else {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(mFactor,
                        getTime(), (!internalMaster && !startSetTime));
            }
        } else if (!internalMaster) {
            if (startStopped) {
                hardwareTimeSource.initializeHardwareClock(0, getTime(), (!startSetTime));
            } else {
                hardwareTimeSource.initializeHardwareClock(hardwareFactor, getTime(), (!startSetTime));
            }
        }
        notInitialized = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getIsInitialized() {
        return (!notInitialized);
    }

    /**
     * Handle a change in the clock running sensor
     */
    private void clockSensorChanged(java.beans.PropertyChangeEvent e) {
        if (clockSensor.getKnownState() == Sensor.ACTIVE) {
            // simply return if clock is already running
            if (pauseTime == null) {
                return;
            }
            setRun(true);
        } else {
            // simply return if clock is already stopped
            if (pauseTime != null) {
                return;
            }
            setRun(false);
        }
    }

    /**
     * Stops Timer.
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if (timer != null) {
            // end this timer
            timer.setRepeats(false); // just in case
            timer.stop();

            java.awt.event.ActionListener listeners[] = timer.getListeners(java.awt.event.ActionListener.class);
            for (java.awt.event.ActionListener listener : listeners)
                timer.removeActionListener(listener);

            timer = null;
        }
    }

    /**
     * InstanceManager.getDefault(jmri.Timebase.class) variables and options
     */
    private double mFactor = 1.0; // this is the rate factor for the JMRI fast clock
    private double hardwareFactor = 1.0; // this is the rate factor for the hardware clock
    //  The above is necessary to support hardware clock Time Sources that fiddle with mFactor to
    //      synchronize, instead of sending over a new time to synchronize.
    private double startupFactor = 1.0; // this is the rate requested at startup
    private boolean startSetRate = true; // if true, the hardware rate will be set to
    private boolean haveStartupFactor = false; // true if startup factor was ever set.
    // startupFactor at startup.

    private Date startAtTime;
    private Date setTimeValue;
    private Date pauseTime; // null value indicates clock is running
    private Sensor clockSensor = null; // active when clock is running, inactive when stopped
    private Memory clockMemory = null; // contains current time on each tick
    private Memory factorMemory = null; // contains the rate factor for the fast clock

    private boolean internalMaster = true; // false indicates a hardware clock is the master
    private String masterName = ""; // name of hardware time source, if not internal master
    private ClockControl hardwareTimeSource = null; // ClockControl instance of hardware time source
    private boolean synchronizeWithHardware = false; // true indicates need to synchronize
    private boolean correctHardware = false; // true indicates hardware correction requested
    private boolean display12HourClock = false; // true if 12-hour clock display is requested
    private ClockInitialRunState initialState = ClockInitialRunState.DO_START; // what to do with the clock running state at startup
    private boolean startSetTime = false; // true indicates set fast clock to specified time at
    //start up requested
    private Date startTime = new Date(); // specified time for setting fast clock at start up
    private int startClockOption = NONE; // request start of a clock at start up
    private boolean notInitialized = true; // true before initialization received from start up
    private boolean showStopButton = false; // true indicates start up with start/stop button displayed

    private java.text.SimpleDateFormat timeStorageFormat = null;

    private javax.swing.Timer timer = null;

    /**
     * Start the minute alarm ticking, if it isnt already.
     */
    void startAlarm() {
        if (timer == null) {
            handleAlarm(null);
        }
    }

    private int oldHours = -1;
    private int oldMinutes = -1;
    private Date oldDate = null;

    /**
     * Handle an "alarm", which is used to count off minutes.
     * <p>
     * Listeners will be notified if the hours or minutes changed
     * since the last time.
     * @param e Event which triggered this
     */
    void handleAlarm(ActionEvent e) {
        // on first pass, set up the timer to call this routine
        if (timer == null) {
            timer = new javax.swing.Timer(60 * 1000, this::handleAlarm);
        }

        Calendar calendar = Calendar.getInstance();
        timer.stop();
        Date date = getTime();
        calendar.setTime(date);
        int waitSeconds = 60 - calendar.get(Calendar.SECOND);
        int delay = (int) (waitSeconds * 1000 / mFactor) + 100; // make sure you miss the time transition
        timer.setInitialDelay(delay);
        timer.setRepeats(true); // in case we run by
        timer.start();

        // and notify the others
        calendar.setTime(date);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        if (hours != oldHours || minutes != oldMinutes) {
            // update memory
            updateMemory(date);
            // notify listeners
            firePropertyChange("minutes", Double.valueOf(oldMinutes), Double.valueOf(minutes));
            firePropertyChange("time", oldDate != null ? new Date(oldDate.getTime()) : null, new Date(date.getTime())); // to ensure not modified outside
        }
        oldDate = date;
        oldHours = hours;
        oldMinutes = minutes;
    }

    void updateMemory(Date date) {
        if (timeStorageFormat == null) {
            try {
                timeStorageFormat = new java.text.SimpleDateFormat(
               java.util.ResourceBundle.getBundle("jmri.jmrit.simpleclock.SimpleClockBundle")
                    .getString("TimeStorageFormat"));
            } catch (java.lang.IllegalArgumentException e) {
                log.info("Dropping back to default time format due to exception", e);
                timeStorageFormat = new java.text.SimpleDateFormat("h:mm a");
            }
        }
        clockMemory.setValue(timeStorageFormat.format(date));
    }

    void updateMemory(double factor) {
        factorMemory.setValue(factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMinuteChangeListener(PropertyChangeListener l) {
        addPropertyChangeListener("minutes", l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        removePropertyChangeListener("minutes", l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return getPropertyChangeListeners("minutes");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        startAlarm();
    }


    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        super.addPropertyChangeListener(propertyName, listener);
        if (propertyName != null && (propertyName.equals("minutes") || propertyName.equals("time"))) {
            startAlarm();
        }
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

    private final static Logger log = LoggerFactory.getLogger(SimpleTimebase.class);

}
