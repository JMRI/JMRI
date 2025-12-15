package jmri.time.implementation;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;

import java.util.Date;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.time.*;

/**
 * Default implementation of Timebase.
 *
 * This code relates to all the code that don't relate to TimeProvider.
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2007
 * @author Dave Duchamp Copyright (C) 2007. additions/revisions for handling one hardware clock
 * @author Daniel Bergqvist Copyright (C) 2025
 */
public class DefaultTimebase extends AbstractTimebase {

    private final SystemConnectionMemo memo;

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

//    private Date startAtTime;
//    private Date setTimeValue;
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

//    private java.text.SimpleDateFormat timeStorageFormat = null;

    private javax.swing.Timer timer = null;









    public DefaultTimebase(InternalSystemConnectionMemo memo) {
        super("SIMPLECLOCK");
        this.memo = memo;
        // initialize time-containing memory
        try {
            clockMemory = InstanceManager.memoryManagerInstance().provideMemory(memo.getSystemPrefix()+"MCURRENTTIME");
            clockMemory.setValue("--");
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to create CURRENTTIME time memory variable");
        }

        init();

    }

    private void init(){

        // set to start counting from now
        setTimeIfPossible(new Date());
        pauseTime = null;
        // initialize start/stop sensor for time running
        try {
            clockSensor = InstanceManager.sensorManagerInstance().provideSensor(memo.getSystemPrefix()+"SCLOCKRUNNING");
            clockSensor.setKnownState(Sensor.ACTIVE);
            clockSensor.addPropertyChangeListener(this::clockSensorChanged);
        } catch (JmriException e) {
            log.warn("Exception setting CLOCKRUNNING sensor ACTIVE", e);
        }
        // initialize rate factor-containing memory
        if (InstanceManager.getNullableDefault(MemoryManager.class) != null) {
            // only try to create memory if memories are supported
            try {
                factorMemory = InstanceManager.memoryManagerInstance()
                    .provideMemory(memo.getSystemPrefix()+"MRATEFACTOR");
                factorMemory.setValue(userGetRate());
            } catch (IllegalArgumentException ex) {
                log.warn("Unable to create RATEFACTOR time memory variable");
            }
        }

    }

    @Override
    public void setState(int s) throws JmriException {
    }

    @Override
    public int getState() {
        return 0;
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameTime");
    }

    @Override
    public void setInternalMaster(boolean master, boolean update) {
        if (master != internalMaster) {
            internalMaster = master;
            if (internalMaster) {
                mFactor = hardwareFactor; // get rid of any fiddled rate present
            }
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                InstanceManager.getDefault(ClockControl.class).initializeHardwareClock(userGetRate(),
                        getTime(), false);
            }

            if (internalMaster) {
                masterName = "";
                hardwareTimeSource = null;
            } else {
                // Note if there are multiple hardware clocks, this should be changed to correctly
                // identify which hardware clock has been chosen-currently assumes only one
                hardwareTimeSource = InstanceManager.getDefault(ClockControl.class);
                masterName = hardwareTimeSource.getHardwareClockName();
            }
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getInternalMaster() {
        return internalMaster;
    }

    @Override
    public void setMasterName(String name) {
        if (!internalMaster) {
            masterName = name;
            // if multiple clocks, this must be replaced by a loop over all hardware clocks to identify
            // the one that is the hardware time source
            hardwareTimeSource = InstanceManager.getDefault(ClockControl.class);
        } else {
            masterName = "";
            hardwareTimeSource = null;
        }
    }

    @Override
    public String getMasterName() {
        return masterName;
    }

    @Override
    public void setSynchronize(boolean synchronize, boolean update) {
        if (synchronizeWithHardware != synchronize) {
            synchronizeWithHardware = synchronize;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                InstanceManager.getDefault(ClockControl.class).initializeHardwareClock(
                   userGetRate(), getTime(), false);
            }
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getSynchronize() {
        return synchronizeWithHardware;
    }

    @Override
    public void setCorrectHardware(boolean correct, boolean update) {
        if (correctHardware != correct) {
            correctHardware = correct;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                InstanceManager.getDefault(ClockControl.class).initializeHardwareClock(
                userGetRate(), getTime(), false);
            }
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getCorrectHardware() {
        return correctHardware;
    }

    @Override
    public void set12HourDisplay(boolean display, boolean update) {
        if (display != display12HourClock) {
            display12HourClock = display;
            if (update) {
                // Note if there are multiple hardware clocks, this should be a loop over all hardware clocks
                InstanceManager.getDefault(ClockControl.class).initializeHardwareClock(
                userGetRate(), getTime(), false);
            }
        }
    }

    @Override
    public boolean use12HourDisplay() {
        return display12HourClock;
    }

    @Override
    public void setClockInitialRunState(ClockInitialRunState state) {
        if (initialState != state) {
            initialState = state;
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public ClockInitialRunState getClockInitialRunState() {
        return initialState;
    }

    @Override
    public void setShowStopButton(boolean displayed) {
        if (showStopButton != displayed) {
            showStopButton = displayed;
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getShowStopButton() {
        return showStopButton;
    }

    @Override
    public void setStartSetTime(boolean set, Date time) {
        if (startSetTime!=set || startTime!=new Date(time.getTime())) {
            startSetTime = set;
            startTime = new Date(time.getTime());
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getStartSetTime() {
        return startSetTime;
    }

    @Override
    public void setStartRate(double factor) {
        if (Math.abs(startupFactor - factor) > 0.0001) { //avoid possible float precision errors
            startupFactor = factor;
            haveStartupFactor = true;
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public double getStartRate() {
        if (haveStartupFactor) {
            return startupFactor;
        } else {
            return userGetRate();
        }
    }

    @Override
    public void setSetRateAtStart(boolean set) {
        if (startSetRate != set) {
            startSetRate = set;
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public boolean getSetRateAtStart() {
        return startSetRate;
    }

    @Override
    public Date getStartTime() {
        return new Date(startTime.getTime());
    }

    @Override
    public void setStartClockOption(int option) {
        if (startClockOption != option) {
            startClockOption = option;
            firePropertyChange("config", 0, 1); // inform listeners that the clock config has changed
        }
    }

    @Override
    public int getStartClockOption() {
        return startClockOption;
    }

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

    @Override
    public void initializeHardwareClock() {
        boolean startStopped = (initialState == ClockInitialRunState.DO_STOP);
        if (synchronizeWithHardware || correctHardware) {
            if (startStopped) {
                InstanceManager.getList(ClockControl.class).forEach( cc ->
                    cc.initializeHardwareClock( 0, getTime(), (!internalMaster && !startSetTime)) );
            } else {
                InstanceManager.getList(ClockControl.class).forEach( cc ->
                    cc.initializeHardwareClock( mFactor, getTime(), (!internalMaster && !startSetTime)) );
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

    @Override
    public boolean getIsInitialized() {
        return (!notInitialized);
    }

    @Override
    public void addMinuteChangeListener(PropertyChangeListener l) {
        InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().addPropertyChangeListener(TimeProvider.PROPERTY_CHANGE_MINUTES, l);
    }

    @Override
    public void removeMinuteChangeListener(PropertyChangeListener l) {
        InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().removePropertyChangeListener(TimeProvider.PROPERTY_CHANGE_MINUTES, l);
    }

    @Override
    public PropertyChangeListener[] getMinuteChangeListeners() {
        return InstanceManager.getDefault(TimeProviderManager.class)
                .getMainTimeProviderHandler().getPropertyChangeListeners(TimeProvider.PROPERTY_CHANGE_MINUTES);
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

            ActionListener[] listeners = timer.getListeners(ActionListener.class);
            for (ActionListener listener : listeners) {
                timer.removeActionListener(listener);
            }
            timer = null;
        }
        if ( clockSensor != null ) {
            clockSensor.removePropertyChangeListener(this::clockSensorChanged);
        }
        super.dispose(); // remove standard property change listeners
    }


    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultTimebase.class);

}
