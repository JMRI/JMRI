package jmri.jmrit.simpleclock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import jmri.ClockControl;
import jmri.Memory;
import jmri.Sensor;
import jmri.Timebase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide basic Timebase implementation from system clock.
 * <P>
 * This implementation provides for the internal clock and for one hardware
 * clock. A number of hooks and comments are provided below for implementing
 * multiple hardware clocks should that ever be done.
 * <P>
 * The setTimeValue member is the fast time when the clock started. The
 * startAtTime member is the wall-clock time when the clock was started.
 * Together, those can be used to calculate the current fast time.
 * <P>
 * The pauseTime member is used to indicate that the Timebase was paused. If
 * non-null, it indicates the current fast time when the clock was paused.
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2007 Dave Duchamp - 2007
 * additions/revisions for handling one hardware clock
 */
public class SimpleTimebase extends jmri.implementation.AbstractNamedBean implements Timebase {

    public SimpleTimebase() {
        super("SIMPLECLOCK");
        // initialize time-containing memory
        try {
            clockMemory = jmri.InstanceManager.memoryManagerInstance().provideMemory("IMCURRENTTIME");
            clockMemory.setValue("--");
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to create IMCURRENTTIME time memory variable");
        }
        // set to start counting from now
        setTime(new Date());
        pauseTime = null;
        // initialize start/stop sensor for time running
        try {
            clockSensor = jmri.InstanceManager.sensorManagerInstance().provideSensor("ISCLOCKRUNNING");
            clockSensor.setKnownState(Sensor.ACTIVE);
            clockSensor.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            clockSensorChanged();
                        }
                    });
        } catch (jmri.JmriException e) {
            log.warn("Exception setting ISCLOCKRUNNING sensor ACTIVE: " + e);
        }
        // initialize rate factor-containing memory
        if (jmri.InstanceManager.getOptionalDefault(jmri.MemoryManager.class) != null) {
            // only try to create memory if memories are supported
            try {
                factorMemory = jmri.InstanceManager.memoryManagerInstance().provideMemory("IMRATEFACTOR");
                factorMemory.setValue(userGetRate());
            } catch (IllegalArgumentException ex) {
                log.warn("Unable to create IMRATEFACTOR time memory variable");
            }
        }
    }

    public String getBeanType() {
        return Bundle.getMessage("BeanNameTime");
    }

    // methods for getting and setting the current Fast Clock time
    public Date getTime() {
        // is clock stopped?
        if (pauseTime != null) {
            return new Date(pauseTime.getTime()); // to ensure not modified outside
        }    	// clock running
        long elapsedMSec = (new Date()).getTime() - startAtTime.getTime();
        long nowMSec = setTimeValue.getTime() + (long) (mFactor * elapsedMSec);
        return new Date(nowMSec);
    }

    public void setTime(Date d) {
        startAtTime = new Date();	// set now in wall clock time
        setTimeValue = new Date(d.getTime());   // to ensure not modified from outside
        if (synchronizeWithHardware) {
            // send new time to all hardware clocks, except the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            if (jmri.InstanceManager.getDefault(jmri.ClockControl.class) != hardwareTimeSource) {
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).setTime(d);
            }
        }
        if (pauseTime != null) {
            pauseTime = setTimeValue;  // if stopped, continue stopped at new time
        }
        handleAlarm();
    }

    /**
     * Set the current time
     * @param i java.time.Instant
     */
    public void setTime(Instant i){
       setTime(Date.from(i));
    }


    public void userSetTime(Date d) {
        // this call only results from user changing fast clock time in Setup Fast Clock
        startAtTime = new Date();	// set now in wall clock time
        setTimeValue = new Date(d.getTime());   // to ensure not modified from outside
        if (synchronizeWithHardware) {
            // send new time to all hardware clocks, including the hardware time source if there is one
            // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
            jmri.InstanceManager.getDefault(jmri.ClockControl.class).setTime(d);
        } else if (!internalMaster && (hardwareTimeSource != null)) {
            // if not synchronizing, send to the hardware time source if there is one
            hardwareTimeSource.setTime(d);
        }
        if (pauseTime != null) {
            pauseTime = setTimeValue;  // if stopped, continue stopped at new time
        }
        handleAlarm();
    }

    // methods for starting and stopping the Fast Clock and returning status
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
                    log.warn("Exception setting ISClockRunning sensor ACTIVE: " + e);
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
                    log.warn("Exception setting ISClockRunning sensor INACTIVE: " + e);
                }
            }
        }
        firePropertyChange("run", Boolean.valueOf(run), Boolean.valueOf(!run));
        handleAlarm();
    }

    public boolean getRun() {
        return pauseTime == null;
    }

    // methods for setting and getting rate
    public void setRate(double factor) {
        if (factor < 0.1 || factor > 100) {
            log.error("rate of " + factor + " is out of reasonable range, set to 1");
            factor = 1;
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
            firePropertyChange("rate", Double.valueOf(factor), Double.valueOf(oldFactor));
        }
        handleAlarm();
    }

    public void userSetRate(double factor) {
        // this call is used when user changes fast clock rate either in Setup Fast Clock or via a ClockControl  
        // implementation
        if (factor < 0.1 || factor > 100) {
            log.error("rate of " + factor + " is out of reasonable range, set to 1");
            factor = 1;
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
        firePropertyChange("rate", Double.valueOf(factor), Double.valueOf(oldFactor));
        handleAlarm();
    }

    public double getRate() {
        return mFactor;
    }

    public double userGetRate() {
        if (internalMaster) {
            return mFactor;
        } else {
            return hardwareFactor;
        }
    }

    public void setInternalMaster(boolean master, boolean update) {
        if (master != internalMaster) {
            internalMaster = master;
            if (internalMaster) {
                mFactor = hardwareFactor;  // get rid of any fiddled rate present
                if (update) {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(mFactor,
                            getTime(), false);
                }
            } else if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(hardwareFactor,
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

    public boolean getInternalMaster() {
        return internalMaster;
    }

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

    public String getMasterName() {
        return masterName;
    }

    public void setSynchronize(boolean synchronize, boolean update) {
        if (synchronizeWithHardware != synchronize) {
            synchronizeWithHardware = synchronize;
            if (update) {
                if (internalMaster) {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(mFactor,
                            getTime(), false);
                } else {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(hardwareFactor,
                            getTime(), false);
                }
            }
        }
    }

    public boolean getSynchronize() {
        return synchronizeWithHardware;
    }

    public void setCorrectHardware(boolean correct, boolean update) {
        if (correctHardware != correct) {
            correctHardware = correct;
            if (update) {
                if (internalMaster) {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(mFactor,
                            getTime(), false);
                } else {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(hardwareFactor,
                            getTime(), false);
                }
            }
        }
    }

    public boolean getCorrectHardware() {
        return correctHardware;
    }

    public void set12HourDisplay(boolean display, boolean update) {
        if (display != display12HourClock) {
            display12HourClock = display;
            if (update) {
                if (internalMaster) {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(mFactor,
                            getTime(), false);
                } else {
                    // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                    jmri.InstanceManager.getDefault(jmri.ClockControl.class).initializeHardwareClock(hardwareFactor,
                            getTime(), false);
                }
            }
        }
    }

    public boolean use12HourDisplay() {
        return display12HourClock;
    }

    public void setStartStopped(boolean stopped) {
        startStopped = stopped;
    }

    public boolean getStartStopped() {
        return startStopped;
    }

    public void setStartSetTime(boolean set, Date time) {
        startSetTime = set;
        startTime = new Date(time.getTime());
    }

    public boolean getStartSetTime() {
        return startSetTime;
    }

    public Date getStartTime() {
        return new Date(startTime.getTime());
    }

    public void setStartClockOption(int option) {
        startClockOption = option;
    }

    public int getStartClockOption() {
        return startClockOption;
    }

    // Note the following method should only be invoked at start up
    public void initializeClock() {
        switch (startClockOption) {
            case NIXIE_CLOCK:
                jmri.jmrit.nixieclock.NixieClockFrame f
                        = new jmri.jmrit.nixieclock.NixieClockFrame();
                f.setVisible(true);
                break;
            case ANALOG_CLOCK:
                jmri.jmrit.analogclock.AnalogClockFrame g
                        = new jmri.jmrit.analogclock.AnalogClockFrame();
                g.setVisible(true);
                break;
            case LCD_CLOCK:
                jmri.jmrit.lcdclock.LcdClockFrame h
                        = new jmri.jmrit.lcdclock.LcdClockFrame();
                h.setVisible(true);
                break;
            default:
                log.debug("initializeClock() called with invalid startClockOption: " + startClockOption);
        }
    }

    /**
     * Method to initialize hardware clock at start up Note: This method is
     * always called at start up after all options have been set. It should be
     * ignored if there is no communication with a hardware clock.
     */
    public void initializeHardwareClock() {
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

    public boolean getIsInitialized() {
        return (!notInitialized);
    }

    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    /**
     * Handle a change in the clock running sensor
     */
    private void clockSensorChanged() {
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
     * Request a call-back when the bound Rate or Run property changes.
     * <P>
     * Not yet implemented.
     */
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a request for a call-back when a bound property changes.
     * <P>
     * Not yet implemented.
     */
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    /**
     * Remove references to and from this object, so that it can eventually be
     * garbage-collected.
     *
     */
    public void dispose() {
    }

    /**
     * InstanceManager.getDefault(jmri.Timebase.class) variables and options
     */
    private double mFactor = 1.0;  // this is the rate factor for the JMRI fast clock
    private double hardwareFactor = 1.0;  // this is the rate factor for the hardware clock
    //  The above is necessary to support hardware clock Time Sources that fiddle with mFactor to
    //      synchronize, instead of sending over a new time to synchronize.
    private Date startAtTime;
    private Date setTimeValue;
    private Date pauseTime;   // null value indicates clock is running
    private Sensor clockSensor = null;   // active when clock is running, inactive when stopped
    private Memory clockMemory = null;   // contains current time on each tick
    private Memory factorMemory = null;  // contains the rate factor for the fast clock

    private boolean internalMaster = true;     // false indicates a hardware clock is the master
    private String masterName = "";		// name of hardware time source, if not internal master
    private ClockControl hardwareTimeSource = null;  // ClockControl instance of hardware time source
    private boolean synchronizeWithHardware = false;  // true indicates need to synchronize
    private boolean correctHardware = false;    // true indicates hardware correction requested
    private boolean display12HourClock = false; // true if 12-hour clock display is requested
    private boolean startStopped = false;    // true indicates start up with clock stopped requested
    private boolean startSetTime = false;    // true indicates set fast clock to specified time at
    //start up requested
    private Date startTime = new Date();	// specified time for setting fast clock at start up
    private int startClockOption = NONE;	// request start of a clock at start up
    private boolean notInitialized = true;  // true before initialization received from start up

    java.text.SimpleDateFormat timeStorageFormat = null;

    javax.swing.Timer timer = null;
    PropertyChangeSupport pcMinutes = new PropertyChangeSupport(this);

    /**
     * Start the minute alarm ticking, if it isnt already.
     */
    void startAlarm() {
        if (timer == null) {
            handleAlarm();
        }
    }

    int oldMinutes = 0;

    /**
     * Handle an "alarm", which is used to count off minutes.
     * <P>
     * Listeners won't be notified if the minute value hasn't changed since the
     * last time.
     */
    @SuppressWarnings("deprecation")
    void handleAlarm() {
        // on first pass, set up the timer to call this routine
        if (timer == null) {
            timer = new javax.swing.Timer(60 * 1000, new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    handleAlarm();
                }
            });
        }

        timer.stop();
        Date date = getTime();
        int waitSeconds = 60 - date.getSeconds();
        int delay = (int) (waitSeconds * 1000 / mFactor) + 100;  // make sure you miss the time transition
        timer.setInitialDelay(delay);
        timer.setRepeats(true);     // in case we run by
        timer.start();

        // and notify the others
        int minutes = date.getMinutes();
        if (minutes != oldMinutes) {
            // update memory
            updateMemory(date);
            // notify listeners
            pcMinutes.firePropertyChange("minutes", Double.valueOf(oldMinutes), Double.valueOf(minutes));
        }
        oldMinutes = minutes;

    }

    void updateMemory(Date date) {
        if (timeStorageFormat == null) {
            try {
                timeStorageFormat = new java.text.SimpleDateFormat(java.util.ResourceBundle.getBundle("jmri.jmrit.simpleclock.SimpleClockBundle")
                        .getString("TimeStorageFormat"));
            } catch (java.lang.IllegalArgumentException e) {
                log.info("Dropping back to default time format due to exception " + e);
                timeStorageFormat = new java.text.SimpleDateFormat("h:mm a");
            }
        }
        clockMemory.setValue(timeStorageFormat.format(date));
    }

    void updateMemory(double factor) {
        factorMemory.setValue(Double.valueOf(factor));
    }

    /**
     * Request a call-back when the minutes place of the time changes.
     */
    public void addMinuteChangeListener(PropertyChangeListener l) {
        if (!Arrays.asList(this.getMinuteChangeListeners()).contains(l)) {
            pcMinutes.addPropertyChangeListener(l);
            startAlarm();
        }
    }

    /**
     * Remove a request for call-back when the minutes place of the time
     * changes.
     */
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        pcMinutes.removePropertyChangeListener(l);
    }

    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return pcMinutes.getPropertyChangeListeners();
    }

    public void setState(int s) throws jmri.JmriException {
    }

    public int getState() {
        return 0;
    }

    private final static Logger log = LoggerFactory.getLogger(SimpleTimebase.class.getName());

}
