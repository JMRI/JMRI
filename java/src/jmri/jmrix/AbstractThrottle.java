package jmri.jmrix;

import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jmri.BasicRosterEntry;
import jmri.CommandStation;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.Throttle;
import jmri.ThrottleListener;
import jmri.beans.PropertyChangeSupport;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.GuardedBy;

/**
 * An abstract implementation of DccThrottle. Based on Glen Oberhauser's
 * original LnThrottleManager implementation.
 * <p>
 * Note that this implements DccThrottle, not Throttle directly, so it has some
 * DCC-specific content.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005
 */
abstract public class AbstractThrottle extends PropertyChangeSupport implements DccThrottle {

    @GuardedBy("this")
    protected float speedSetting;
    /**
     * Question: should we set a default speed step mode so it's never zero?
     */
    protected SpeedStepMode speedStepMode = SpeedStepMode.UNKNOWN;
    protected boolean isForward;
    
    /**
     * Array of Function values.
     * <p>
     * Contains current Boolean value for functions.
     * This array should not be accessed directly by Throttles,
     * use setFunction / getFunction / updateFunction.
     * Needs to be same length as FUNCTION_MOMENTARY_BOOLEAN_ARRAY.
     */
    private final boolean[] FUNCTION_BOOLEAN_ARRAY;

    /**
     * Array of Momentary Function values.
     * <p>
     * Contains current Boolean value for Momentary function settings.
     * Needs to be same length as FUNCTION_BOOLEAN_ARRAY.
     */
    private final boolean[] FUNCTION_MOMENTARY_BOOLEAN_ARRAY;
    
    /**
     * Is this object still usable? Set false after dispose, this variable is
     * used to check for incorrect usage.
     */
    protected boolean active;

    /**
     * Create a new AbstractThrottle with Functions 0-28..
     * <p>
     * All function and momentary functions set to Off.
     * @param memo System Connection.
     */
    public AbstractThrottle(SystemConnectionMemo memo) {
        active = true;
        adapterMemo = memo;
        FUNCTION_BOOLEAN_ARRAY = new boolean[29];
        FUNCTION_MOMENTARY_BOOLEAN_ARRAY = new boolean[29];
    }
    
    /**
     * Create a new AbstractThrottle with custom number of functions.
     * <p>
     * All function and momentary functions set to Off.
     * @param memo System Connection this throttle is on
     * @param totalFunctions total number of functions available, including 0
     */
    public AbstractThrottle(SystemConnectionMemo memo, int totalFunctions) {
        active = true;
        adapterMemo = memo;
        FUNCTION_BOOLEAN_ARRAY = new boolean[totalFunctions];
        FUNCTION_MOMENTARY_BOOLEAN_ARRAY = new boolean[totalFunctions];
    }

    /**
     * System Connection this throttle is on
     */
    protected SystemConnectionMemo adapterMemo;

    /**
     * speed - expressed as a value {@literal 0.0 -> 1.0.} Negative means
     * emergency stop. This is a bound parameter.
     *
     * @return speed
     */
    @Override
    public synchronized float getSpeedSetting() {
        return speedSetting;
    }

    /**
     * setSpeedSetting - Implementing functions should override this function,
     * but should either make a call to super.setSpeedSetting() to notify the
     * listeners at the end of their work, or should notify the listeners
     * themselves.
     */
    @Override
    public void setSpeedSetting(float speed) {
        setSpeedSetting(speed, false, false);
        record(speed);
    }

    /**
     * setSpeedSetting - Implementations should override this method only if
     * they normally suppress messages to the system if, as far as JMRI can
     * tell, the new message would make no difference to the system state (eg.
     * the speed is the same, or effectivly the same, as the existing speed).
     * Then, the boolean options can affect this behaviour.
     *
     * @param speed                 the new speed
     * @param allowDuplicates       don't suppress messages
     * @param allowDuplicatesOnStop don't suppress messages if the new speed is
     *                              'stop'
     */
    @Override
    public synchronized void setSpeedSetting(float speed, boolean allowDuplicates, boolean allowDuplicatesOnStop) {
        if (Math.abs(this.speedSetting - speed) > 0.0001) {
            firePropertyChange(SPEEDSETTING, this.speedSetting, this.speedSetting = speed);
        }
        record(speed);
    }

    /**
     * setSpeedSettingAgain - set the speed and don't ever suppress the sending
     * of messages to the system
     *
     * @param speed the new speed
     */
    @Override
    public void setSpeedSettingAgain(float speed) {
        setSpeedSetting(speed, true, true);
    }

    /**
     * direction This is an bound parameter.
     *
     * @return true if locomotive is running forward
     */
    @Override
    public boolean getIsForward() {
        return isForward;
    }

    /**
     * Implementing functions should override this function, but should either
     * make a call to super.setIsForward() to notify the listeners, or should
     * notify the listeners themselves.
     *
     * @param forward true if forward; false otherwise
     */
    @Override
    public void setIsForward(boolean forward) {
        firePropertyChange(ISFORWARD, isForward, isForward = forward);
    }

    /*
     * functions - note that we use the naming for DCC, though that's not the
     * implication; see also DccThrottle interface
     */

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public boolean[] getFunctions() {
        return Arrays.copyOf(FUNCTION_BOOLEAN_ARRAY,FUNCTION_BOOLEAN_ARRAY.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public boolean[] getFunctionsMomentary() {
        return Arrays.copyOf(FUNCTION_MOMENTARY_BOOLEAN_ARRAY,
            FUNCTION_MOMENTARY_BOOLEAN_ARRAY.length);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFunction(int fN) {
        if (fN<0 || fN > FUNCTION_BOOLEAN_ARRAY.length-1){
            log.warn("Unhandled get function: {}",fN);
            return false;
        }
        return FUNCTION_BOOLEAN_ARRAY[fN];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getFunctionMomentary(int fN) {
        if (fN<0 || fN > FUNCTION_MOMENTARY_BOOLEAN_ARRAY.length-1){
            log.warn("Unhandled get momentary function: {}",fN);
            return false;
        }
        return FUNCTION_MOMENTARY_BOOLEAN_ARRAY[fN];
    
    }
    
    /**
     * Notify listeners that a Throttle has disconnected and is no longer
     * available for use.
     * <p>
     * For when throttles have been stolen or encounter hardware error, and a
     * normal release / dispose is not possible.
     */
    protected void notifyThrottleDisconnect() {
        firePropertyChange("ThrottleConnected", true, false); // NOI18N
    }

    // set initial values purely for changelistener following
    // the 1st true or false will always get sent
    private Boolean _dispatchEnabled = null;
    private Boolean _releaseEnabled = null;

    /**
     * Notify listeners that a Throttle has Dispatch enabled or disabled.
     * <p>
     * For systems where dispatch availability is variable.
     * <p>
     * Does not notify if existing value is unchanged.
     *
     * @param newVal true if Dispatch enabled, else false
     *
     */
    @Override
    public void notifyThrottleDispatchEnabled(boolean newVal) {
        firePropertyChange("DispatchEnabled", _dispatchEnabled, _dispatchEnabled = newVal); // NOI18N
    }

    /**
     * Notify listeners that a Throttle has Release enabled or disabled.
     * <p>
     * For systems where release availability is variable.
     * <p>
     * Does not notify if existing value is unchanged.
     *
     * @param newVal true if Release enabled, else false
     *
     */
    @Override
    public void notifyThrottleReleaseEnabled(boolean newVal) {
        firePropertyChange("ReleaseEnabled", _releaseEnabled, _releaseEnabled = newVal); // NOI18N
    }

    /**
     * Temporary behaviour only allowing unique PCLs.
     * To support Throttle PCL's ( eg. WiThrottle Server ) that rely on the 
     * previous behaviour of only allowing 1 unique PCL instance.
     * To be removed when WiThrottle Server has been updated.
     * {@inheritDoc}
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if ( Arrays.asList(getPropertyChangeListeners()).contains(l) ){
            log.warn("Preventing {} adding duplicate PCL", l);
            return;
        }
        super.addPropertyChangeListener(l);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        log.debug("Removing property change {}", l);
        super.removePropertyChangeListener(l);
        log.debug("remove listeners size is {}", getPropertyChangeListeners().length);
        if (getPropertyChangeListeners().length == 0) {
            log.debug("No listeners so calling ThrottleManager.dispose with an empty ThrottleListener");
            InstanceManager.throttleManagerInstance().disposeThrottle(this, new ThrottleListener() {
                @Override
                public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                }

                @Override
                public void notifyThrottleFound(DccThrottle t) {
                }

                @Override
                public void notifyDecisionRequired(LocoAddress address, DecisionType question) {
                }
            });
        }
    }

    /**
     * Trigger the notification of all PropertyChangeListeners. Will only notify
     * if oldValue and newValue are not equal and non-null.
     *
     * @param property the name of the property to send notifications for
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     * @deprecated since 4.19.5; use
     * {@link #firePropertyChange(java.lang.String, java.lang.Object, java.lang.Object)}
     * instead
     */
    @Deprecated
    protected void notifyPropertyChangeListener(String property, Object oldValue, Object newValue) {
        firePropertyChange(property, oldValue, newValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Deprecated
    public List<PropertyChangeListener> getListeners() {
        return Arrays.asList(getPropertyChangeListeners());
    }

    /**
     * Call from a ThrottleListener to dispose of the throttle instance
     *
     * @param l the listener requesting the dispose
     *
     */
    @Override
    public void dispose(ThrottleListener l) {
        if (!active) {
            log.error("Dispose called when not active");
        }
        InstanceManager.throttleManagerInstance().disposeThrottle(this, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatch(ThrottleListener l) {
        if (!active) {
            log.warn("dispatch called when not active");
        }
        InstanceManager.throttleManagerInstance().dispatchThrottle(this, l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release(ThrottleListener l) {
        if (!active) {
            log.warn("release called when not active");
        }
        InstanceManager.throttleManagerInstance().releaseThrottle(this, l);
    }

    /**
     * Dispose when finished with this Throttle. May be used in tests for cleanup.
     * Throttles normally call {@link #finishRecord()} here.
     */
    protected abstract void throttleDispose();

    /**
     * Handle quantized speed. Note this can change! Value returned is
     * always positive.
     *
     * @return 1 divided by the number of speed steps this DCC throttle supports
     */
    @Override
    public float getSpeedIncrement() {
        return speedStepMode.increment;
    }

    /*
     * functions - note that we use the naming for DCC, though that's not the
     * implication; see also DccThrottle interface
     */
    
    /**
     * Send whole (DCC) Function Group for a particular function number.
     * @param functionNum Function Number
     * @param momentary False to send normal function status, true to send momentary.
     */
    protected void sendFunctionGroup(int functionNum, boolean momentary){
        switch (FUNCTION_GROUPS[functionNum]) {
            case 1:
                if (momentary) sendMomentaryFunctionGroup1(); else sendFunctionGroup1();
                break;
            case 2:
                if (momentary) sendMomentaryFunctionGroup2(); else sendFunctionGroup2();
                break;
            case 3:
                if (momentary) sendMomentaryFunctionGroup3(); else sendFunctionGroup3();
                break;
            case 4:
                if (momentary) sendMomentaryFunctionGroup4(); else sendFunctionGroup4();
                break;
            case 5:
                if (momentary) sendMomentaryFunctionGroup5(); else sendFunctionGroup5();
                break;
            default:
                break;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunction(int functionNum, boolean newState) {
        if (functionNum < 0 || functionNum > FUNCTION_BOOLEAN_ARRAY.length-1) {
            log.warn("Unhandled set function number: {} {}", functionNum, this.getClass().getName());
            return;
        }
        boolean old = FUNCTION_BOOLEAN_ARRAY[functionNum];
        FUNCTION_BOOLEAN_ARRAY[functionNum] = newState;
        sendFunctionGroup(functionNum,false);
        firePropertyChange(Throttle.getFunctionString(functionNum), old, newState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFunctions(Map<Integer, Boolean> newStates) {
        // First update all the functions in the FUNCTION_BOOLEAN_ARRAY
        for (Map.Entry<Integer, Boolean> entry : newStates.entrySet()) {
            int functionNum = entry.getKey();
            boolean newState = entry.getValue();
            if (functionNum < 0 || functionNum > FUNCTION_BOOLEAN_ARRAY.length-1) {
                log.warn("Unhandled set function number: {} {}", functionNum, this.getClass().getName());
                continue;
            }
            FUNCTION_BOOLEAN_ARRAY[functionNum] = newState;
        }
        
        // Then tell the layout and JMRI about it
        for (Map.Entry<Integer, Boolean> entry : newStates.entrySet()) {
            int functionNum = entry.getKey();
            boolean newState = entry.getValue();
            if (functionNum < 0 || functionNum > FUNCTION_BOOLEAN_ARRAY.length-1) {
                // Don't log a warning here. We have already done that above.
                continue;
            }
            boolean old = FUNCTION_BOOLEAN_ARRAY[functionNum];
            sendFunctionGroup(functionNum,false);
            firePropertyChange(Throttle.getFunctionString(functionNum), old, newState);
        }
    }

    /**
     * Update the state of a single function. Updates function value and
     * ChangeListener. Does not send outward message TO hardware.
     *
     * @param fn    Function Number 0-28
     * @param state On - True, Off - False
     */
    public void updateFunction(int fn, boolean state) {
        if (fn < 0 || fn > FUNCTION_BOOLEAN_ARRAY.length-1) {
            log.warn("Unhandled update function number: {} {}", fn, this.getClass().getName());
            return;
        }
        boolean old = FUNCTION_BOOLEAN_ARRAY[fn];
        FUNCTION_BOOLEAN_ARRAY[fn] = state;
        firePropertyChange(Throttle.getFunctionString(fn), old, state);
    }
    
    /**
     * Update the Momentary state of a single function. 
     * Updates function value and ChangeListener. 
     * Does not send outward message TO hardware.
     *
     * @param fn    Momentary Function Number 0-28
     * @param state On - True, Off - False
     */
    public void updateFunctionMomentary(int fn, boolean state) {
        if (fn < 0 || fn > FUNCTION_MOMENTARY_BOOLEAN_ARRAY.length-1) {
            log.warn("Unhandled update momentary function number: {}", fn);
            return;
        }
        boolean old = FUNCTION_MOMENTARY_BOOLEAN_ARRAY[fn];
        FUNCTION_MOMENTARY_BOOLEAN_ARRAY[fn] = state;
        firePropertyChange(Throttle.getFunctionMomentaryString(fn), old, state);
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup1() {
        log.error("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup2() {
        log.error("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup3() {
        log.error("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Send the message to set the state of functions F13, F14, F15, F16, F17,
     * F18, F19, F20.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup4() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function13Through20Packet(
                a.getNumber(), a.isLongAddress(),
                getF13(), getF14(), getF15(), getF16(),
                getF17(), getF18(), getF19(), getF20());

        //if the result returns as null, we should quit.
        if (result == null) {
            return;
        }
        CommandStation c;
        if ((adapterMemo != null) && (adapterMemo.get(jmri.CommandStation.class) != null)) {
            c = adapterMemo.get(jmri.CommandStation.class);
        } else {
            c = InstanceManager.getNullableDefault(CommandStation.class);
        }

        // send it 3 times
        if (c != null) {
            c.sendPacket(result, 3);
        } else {
            log.error("Can't send F13-F20 since no command station defined");
        }
    }

    /**
     * Send the message to set the state of functions F21, F22, F23, F24, F25,
     * F26, F27, F28.
     * <p>
     * This is used in the setFn implementations provided in this class, but a
     * real implementation needs to be provided.
     */
    protected void sendFunctionGroup5() {
        DccLocoAddress a = (DccLocoAddress) getLocoAddress();
        byte[] result = jmri.NmraPacket.function21Through28Packet(
                a.getNumber(), a.isLongAddress(),
                getF21(), getF22(), getF23(), getF24(),
                getF25(), getF26(), getF27(), getF28());
        //if the result returns as null, we should quit.
        if (result == null) {
            return;
        }
        CommandStation c;
        if ((adapterMemo != null) && (adapterMemo.get(jmri.CommandStation.class) != null)) {
            c = adapterMemo.get(jmri.CommandStation.class);
        } else {
            c = InstanceManager.getNullableDefault(CommandStation.class);
        }

        // send it 3 times
        if (c != null) {
            c.sendPacket(result, 3);
        } else {
            log.error("Can't send F21-F28 since no command station defined");
        }
    }

    /**
     * Sets Momentary Function and sends to layout.
     * {@inheritDoc}
     */
    @Override
    public void setFunctionMomentary(int momFuncNum, boolean state){
        if (momFuncNum < 0 || momFuncNum > FUNCTION_MOMENTARY_BOOLEAN_ARRAY.length-1) {
            log.warn("Unhandled set momentary function number: {}", momFuncNum);
            return;
        }
        boolean old = FUNCTION_MOMENTARY_BOOLEAN_ARRAY[momFuncNum];
        FUNCTION_MOMENTARY_BOOLEAN_ARRAY[momFuncNum] = state;
        sendFunctionGroup(momFuncNum,true);
        firePropertyChange(Throttle.getFunctionMomentaryString(momFuncNum), old, state);
    }

    /**
     * Send the message to set the momentary state of functions F0, F1, F2, F3,
     * F4.
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup1() {
    }

    /**
     * Send the message to set the momentary state of functions F5, F6, F7, F8.
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup2() {
    }

    /**
     * Send the message to set the Momentary state of functions F9, F10, F11,
     * F12
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup3() {
    }

    /**
     * Send the message to set the Momentary state of functions F13, F14, F15,
     * F16, F17, F18, F19, F20
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup4() {
    }

    /**
     * Send the message to set the Momentary state of functions F21, F22, F23,
     * F24, F25, F26, F27, F28
     * <p>
     * This is used in the setFnMomentary implementations provided in this
     * class, but a real implementation needs to be provided if the hardware
     * supports setting functions momentary.
     */
    protected void sendMomentaryFunctionGroup5() {
    }

    /**
     * Set the speed step value. Default should be 128 speed step mode in most
     * cases.
     * <p>
     * Specific implementations should override this function.
     *
     * @param mode the current speed step mode
     */
    @Override
    public void setSpeedStepMode(SpeedStepMode mode) {
        log.debug("Speed Step Mode Change from:{} to:{}", speedStepMode, mode);
        firePropertyChange(SPEEDSTEPS, speedStepMode, speedStepMode = mode);
    }

    @Override
    public SpeedStepMode getSpeedStepMode() {
        return speedStepMode;
    }

    long durationRunning = 0;
    protected long start;

    /**
     * Processes updated speed from subclasses. Tracks total operating time for
     * the roster entry by starting the clock if speed is non-zero or stopping
     * the clock otherwise.
     *
     * @param speed the current speed
     */
    protected synchronized void record(float speed) {
        if (re == null) {
            return;
        }
        if (speed == 0) {
            stopClock();
        } else {
            startClock();
        }
    }

    protected synchronized void startClock() {
        if (start == 0) {
            start = System.currentTimeMillis();
        }
    }

    void stopClock() {
        if (start == 0) {
            return;
        }
        long stop = System.currentTimeMillis();
        //Set running duration in seconds
        durationRunning = durationRunning + ((stop - start) / 1000);
        start = 0;
    }

    protected synchronized void finishRecord() {
        if (re == null) {
            return;
        }
        stopClock();
        String currentDurationString = re.getAttribute("OperatingDuration");
        long currentDuration = 0;
        if (currentDurationString == null) {
            currentDurationString = "0";
            log.info("operating duration for {} starts as zero", getLocoAddress());
        }
        try {
            currentDuration = Long.parseLong(currentDurationString);
        } catch (NumberFormatException e) {
            log.warn("current stored duration is not a valid number \"{} \"", currentDurationString);
        }
        currentDuration = currentDuration + durationRunning;
        re.putAttribute("OperatingDuration", "" + currentDuration);
        re.putAttribute("LastOperated", new StdDateFormat().format(new Date()));
        //Only store if the roster entry isn't open.
        if (!re.isOpen()) {
            re.store();
        } else {
            log.warn("Roster Entry {} running time not saved as entry is already open for editing", re.getId());
        }
        re = null;
    }

    @GuardedBy("this")
    BasicRosterEntry re = null;

    @Override
    public synchronized void setRosterEntry(BasicRosterEntry re) {
        this.re = re;
    }

    @Override
    public synchronized BasicRosterEntry getRosterEntry() {
        return re;
    }

    /**
     * Get an integer speed for the given raw speed value. This is a convenience
     * method that calls {@link #intSpeed(float, int)} with a maxStep of 127.
     *
     * @param speed the speed as a percentage of maximum possible speed;
     *              negative values indicate a need for an emergency stop
     * @return an integer in the range 0-127
     */
    protected int intSpeed(float speed) {
        return intSpeed(speed, 127);
    }

    /**
     * Get an integer speed for the given raw speed value.
     *
     * @param speed the speed as a percentage of maximum possible speed;
     *              negative values indicate a need for an emergency stop
     * @param steps number of possible speeds; values less than 2 will cause
     *              errors
     * @return an integer in the range 0-steps
     */
    protected static int intSpeed(float speed, int steps) {
        // test that speed is < 0 for emergency stop since calculation of
        // value returns 0 for some values of -1 < rawSpeed < 0
        if (speed < 0) {
            return 1; // emergency stop
        }

        // Stretch speed input to full output range
        // Since Emergency Stop (estop) is speed 1, subtract 1 from steps
        speed *= (steps - 1);
        // convert to integer by rounding
        int value = Math.round(speed);

        // Only return stop if value is actually 0, jump to first speed
        // step for small positive inputs.
        // speeds (at this point) larger than 0.5f are already handled 
        // by the rounding above.
        if (speed > 0.0f && speed <= 0.5f) {
            value = 1;
        }

        if (value < 0) {
            // if we get here, something is wrong and needs to be reported.
            Exception ex = new Exception("Error calculating speed. Please send logs to the JMRI developers.");
            log.error(ex.getMessage(), ex);
            return 1;  // return estop anyway
        } else if (value >= steps) {
            return steps; // maximum possible speed
        } else if (value > 0) {
            return value + 1; // add 1 to the value to avoid the estop
        } else {
            return 0; // non-emergency stop
        }
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractThrottle.class);

}
