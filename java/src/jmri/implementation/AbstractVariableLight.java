package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Date;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import static jmri.Light.INTERMEDIATE;
import static jmri.Light.TRANSITIONINGHIGHER;
import static jmri.Light.TRANSITIONINGLOWER;
import static jmri.Light.TRANSITIONINGTOFULLOFF;
import static jmri.Light.TRANSITIONINGTOFULLON;
import static jmri.DigitalIO.OFF;
import static jmri.DigitalIO.ON;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.VariableLight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class providing partial implementation of the logic of the Light
 * interface when the Intensity is variable.
 * <p>
 * Now it includes the transition code, but it only does the steps on the fast
 * minute clock. Later it may do its own timing but this was simple to piggy
 * back on the fast minute listener.
 * <p>
 * The structure is in part dictated by the limitations of the X10 protocol and
 * implementations. However, it is not limited to X10 devices only. Other
 * interfaces that have a way to provide a dimmable light should use it.
 * <p>
 * X10 has on/off commands, and separate commands for setting a variable
 * intensity via "dim" commands. Some X10 implementations use relative dimming,
 * some use absolute dimming. Some people set the dim level of their Lights and
 * then just use on/off to turn control the lamps; in that case we don't want to
 * send dim commands. Further, X10 communications is very slow, and sending a
 * complete set of dim operations can take a long time. So the algorithm is:
 * <ul>
 * <li>Until the intensity has been explicitly set different from 1.0 or 0.0, no
 * intensity commands are to be sent over the power line.
 * </ul>
 * <p>
 * Unlike the parent class, this stores CurrentIntensity and TargetIntensity in
 * separate variables.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Ken Cameron Copyright (C) 2008,2009
 * @author Bob Jacobsen Copyright (C) 2008,2009
 */
public abstract class AbstractVariableLight
        extends AbstractLight implements VariableLight {

    private final static Logger log = LoggerFactory.getLogger(AbstractVariableLight.class);

    public AbstractVariableLight(String systemName, String userName) {
        super(systemName, userName);
        initClocks();
    }

    public AbstractVariableLight(String systemName) {
        super(systemName);
        initClocks();
    }

    /**
     * System independent instance variables (saved between runs).
     */
//    protected double mMaxIntensity = 1.0;     // Uncomment when mMaxIntensity is removed from AbstractLight due to deprecation
//    protected double mMinIntensity = 0.0;     // Uncomment when mMinIntensity is removed from AbstractLight due to deprecation

    /**
     * System independent operational instance variables (not saved between
     * runs).
     */
//    protected double mCurrentIntensity = 0.0; // Uncomment when mCurrentIntensity is removed from AbstractLight due to deprecation

    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case INTERMEDIATE: return Bundle.getMessage("LightStateIntermediate");
            case TRANSITIONINGTOFULLON: return Bundle.getMessage("LightStateTransitioningToFullOn");
            case TRANSITIONINGHIGHER: return Bundle.getMessage("LightStateTransitioningHigher");
            case TRANSITIONINGLOWER: return Bundle.getMessage("LightStateTransitioningLower");
            case TRANSITIONINGTOFULLOFF: return Bundle.getMessage("LightStateTransitioningToFullOff");
            default: return super.describeState(state);
        }
    }

    /**
     * Handle a request for a state change. ON and OFF go to the MaxIntensity
     * and MinIntensity, specifically, and all others are not permitted
     * <p>
     * ON and OFF avoid use of variable intensity if MaxIntensity = 1.0 or
     * MinIntensity = 0.0, and no transition is being used.
     */
    @Override
    public void setState(int newState) {
        if (log.isDebugEnabled()) {
            log.debug("setState {} was {}", newState, mState);
        }
        int oldState = mState;
        if (newState != ON && newState != OFF) {
            throw new IllegalArgumentException("cannot set state value " + newState);
        }

        // first, send the on command
        sendOnOffCommand(newState);

        if (newState == ON) {
            // see how to handle intensity
            if (getMaxIntensity() == 1.0 && getTransitionTime() <= 0) {
                // treat as not variable light
                if (log.isDebugEnabled()) {
                    log.debug("setState({}) considers not variable for ON", newState);
                }
                // update the intensity without invoking the hardware
                notifyTargetIntensityChange(1.0);
            } else {
                // requires an intensity change, check for transition
                if (getTransitionTime() <= 0) {
                    // no transition, just to directly to target using on/off
                    if (log.isDebugEnabled()) {
                        log.debug("setState({}) using variable intensity", newState);
                    }
                    // tell the hardware to change intensity
                    sendIntensity(getMaxIntensity());
                    // update the intensity value and listeners without invoking the hardware
                    notifyTargetIntensityChange(getMaxIntensity());
                } else {
                    // using transition
                    startTransition(getMaxIntensity());
                }
            }
        }
        if (newState == OFF) {
            // see how to handle intensity
            if (getMinIntensity() == 0.0 && getTransitionTime() <= 0) {
                // treat as not variable light
                if (log.isDebugEnabled()) {
                    log.debug("setState({}) considers not variable for OFF", newState);
                }
                // update the intensity without invoking the hardware
                notifyTargetIntensityChange(0.0);
            } else {
                // requires an intensity change
                if (getTransitionTime() <= 0) {
                    // no transition, just to directly to target using on/off
                    if (log.isDebugEnabled()) {
                        log.debug("setState({}) using variable intensity", newState);
                    }
                    // tell the hardware to change intensity
                    sendIntensity(getMinIntensity());
                    // update the intensity value and listeners without invoking the hardware
                    notifyTargetIntensityChange(getMinIntensity());
                } else {
                    // using transition
                    startTransition(getMinIntensity());
                }
            }
        }

        // notify of state change
        notifyStateChange(oldState, newState);
    }

    /**
     * Set the intended new intensity value for the Light. If transitions are in
     * use, they will be applied.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Values at or below the minIntensity property will result in the Light
     * going to the OFF state immediately. Values at or above the maxIntensity
     * property will result in the Light going to the ON state immediately.
     * <p>
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     */
    @Override
    public void setTargetIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("setTargetIntensity {}", intensity);
        }
        if (intensity < 0.0 || intensity > 1.0) {
            throw new IllegalArgumentException("Target intensity value " + intensity + " not in legal range");
        }

        // limit
        if (intensity > mMaxIntensity) {
            intensity = mMaxIntensity;
        }
        if (intensity < mMinIntensity) {
            intensity = mMinIntensity;
        }

        // see if there's a transition in use
        if (getTransitionTime() > 0.0) {
            startTransition(intensity);
        } else {
            // No transition in use, move immediately

            // Set intensity and intermediate state
            sendIntensity(intensity);
            // update value and tell listeners
            notifyTargetIntensityChange(intensity);

            // decide if this is a state change operation
            if (intensity >= mMaxIntensity) {
                setState(ON);
            } else if (intensity <= mMinIntensity) {
                setState(OFF);
            } else {
                notifyStateChange(mState, INTERMEDIATE);
            }
        }
    }

    /**
     * Set up to start a transition
     * @param intensity target intensity
     */
    protected void startTransition(double intensity) {
        // set target value
        mTransitionTargetIntensity = intensity;

        // set state
        int nextState;
        if (intensity >= getMaxIntensity()) {
            nextState = TRANSITIONINGTOFULLON;
        } else if (intensity <= getMinIntensity()) {
            nextState = TRANSITIONINGTOFULLOFF;
        } else if (intensity >= mCurrentIntensity) {
            nextState = TRANSITIONINGHIGHER;
        } else if (intensity <= mCurrentIntensity) {
            nextState = TRANSITIONINGLOWER;
        } else {
            nextState = TRANSITIONING;  // not expected
        }
        notifyStateChange(mState, nextState);
        // make sure clocks running to handle it   
        initClocks();
    }

    /**
     * Send a Dim/Bright commands to the hardware to reach a specific intensity.
     * @param intensity new intensity
     */
    abstract protected void sendIntensity(double intensity);

    /**
     * Send a On/Off Command to the hardware
     * @param newState new state
     */
    abstract protected void sendOnOffCommand(int newState);

    /**
     * Variables needed for saved values
     */
    protected double mTransitionDuration = 0.0;

    /**
     * Variables needed but not saved to files/panels
     */
    protected double mTransitionTargetIntensity = 0.0;
    protected Date mLastTransitionDate = null;
    protected long mNextTransitionTs = 0;
    protected Timebase internalClock = null;
    protected javax.swing.Timer alarmSyncUpdate = null;
    protected java.beans.PropertyChangeListener minuteChangeListener = null;

    /**
     * setup internal clock, start minute listener
     */
    private void initClocks() {
        if (minuteChangeListener != null) {
            return; // already done
        }
        // Create a Timebase listener for the Minute change events
        internalClock = InstanceManager.getNullableDefault(jmri.Timebase.class);
        if (internalClock == null) {
            log.error("No Timebase Instance");
            return;
        }
        minuteChangeListener = (java.beans.PropertyChangeEvent e) -> {
            //process change to new minute
            newInternalMinute();
        };
        internalClock.addMinuteChangeListener(minuteChangeListener);
    }

    /**
     * Layout time has changed to a new minute. Process effect that might be
     * having on intensity. Currently, this implementation assumes there's a
     * fixed number of steps between min and max brightness.
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    protected void newInternalMinute() {
        double origCurrent = mCurrentIntensity;
        int origState = mState;
        int steps = getNumberOfSteps();

        if ((mTransitionDuration > 0) && (steps > 0)) {
            double stepsPerMinute = steps / mTransitionDuration;
            double stepSize = 1 / (double) steps;
            double intensityDiffPerMinute = stepSize * stepsPerMinute;
            // if we are more than one step away, keep stepping
            if (Math.abs(mCurrentIntensity - mTransitionTargetIntensity) != 0) {
                if (log.isDebugEnabled()) {
                    log.debug("before Target: {} Current: {}", mTransitionTargetIntensity, mCurrentIntensity);
                }

                if (mTransitionTargetIntensity > mCurrentIntensity) {
                    mCurrentIntensity = mCurrentIntensity + intensityDiffPerMinute;
                    if (mCurrentIntensity >= mTransitionTargetIntensity) {
                        // Done!
                        mCurrentIntensity = mTransitionTargetIntensity;
                        if (mCurrentIntensity >= getMaxIntensity()) {
                            mState = ON;
                        } else {
                            mState = INTERMEDIATE;
                        }
                    }
                } else {
                    mCurrentIntensity = mCurrentIntensity - intensityDiffPerMinute;
                    if (mCurrentIntensity <= mTransitionTargetIntensity) {
                        // Done!
                        mCurrentIntensity = mTransitionTargetIntensity;
                        if (mCurrentIntensity <= getMinIntensity()) {
                            mState = OFF;
                        } else {
                            mState = INTERMEDIATE;
                        }
                    }
                }

                // command new intensity
                sendIntensity(mCurrentIntensity);

                if (log.isDebugEnabled()) {
                    log.debug("after Target: {} Current: {}", mTransitionTargetIntensity, mCurrentIntensity);
                }
            }
        }
        if (origCurrent != mCurrentIntensity) {
            firePropertyChange("CurrentIntensity", Double.valueOf(origCurrent), Double.valueOf(mCurrentIntensity));
            if (log.isDebugEnabled()) {
                log.debug("firePropertyChange intensity {} -> {}", origCurrent, mCurrentIntensity);
            }
        }
        if (origState != mState) {
            firePropertyChange("KnownState", Integer.valueOf(origState), Integer.valueOf(mState));
            if (log.isDebugEnabled()) {
                log.debug("firePropertyChange intensity {} -> {}", origCurrent, mCurrentIntensity);
            }
        }
    }

    /**
     * Provide the number of steps available between min and max intensity
     * @return number of steps
     */
    abstract protected int getNumberOfSteps();

    /**
     * Change the stored target intensity value and do notification, but don't
     * change anything in the hardware
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    @Override
    protected void notifyTargetIntensityChange(double intensity) {
        double oldValue = mCurrentIntensity;
        mCurrentIntensity = intensity;
        if (oldValue != intensity) {
            firePropertyChange("TargetIntensity", oldValue, intensity);
        }
    }

    /*.*
     * Check if this object can handle variable intensity.
     * <p>
     * @return true, as this abstract class implements variable intensity.
     *./
    @Override
    public boolean isIntensityVariable() {
        return true;
    }

    /**
     * Can the Light change its intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the transitionTime
     * property, which controls how long the Light will take to change from one
     * intensity level to another.
     * <p>
     * Unbound property
     * @return can transition
     */
    @Override
    public boolean isTransitionAvailable() {
        return true;
    }

    /**
     * Set the fast-clock duration for a transition from full ON to full OFF or
     * vice-versa.
     * <p>
     * Bound property
     * <p>
     * @throws IllegalArgumentException if minutes is not valid
     */
    @Override
    public void setTransitionTime(double minutes) {
        if (minutes < 0.0) {
            throw new IllegalArgumentException("Invalid transition time: " + minutes);
        }
        mTransitionDuration = minutes;
    }

    /**
     * Get the number of fastclock minutes taken by a transition from full ON to
     * full OFF or vice versa.
     * <p>
     * @return 0.0 if the output intensity transition is instantaneous
     */
    @Override
    public double getTransitionTime() {
        return mTransitionDuration;
    }

    /**
     * Convenience method for checking if the intensity of the light is
     * currently changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the
     * transition is over.
     * @return is transitioning
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    @Override
    public boolean isTransitioning() {
        if (mTransitionTargetIntensity != mCurrentIntensity) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the current intensity value. If the Light is currently transitioning,
     * this may be either an intermediate or final value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return current intensity
     */
    @Override
    public double getCurrentIntensity() {
        return mCurrentIntensity;
    }

    /**
     * Get the target intensity value for the current transition, if any. If the
     * Light is not currently transitioning, this is the current intensity
     * value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Bound property
     *
     * @return target intensity
     */
    @Override
    public double getTargetIntensity() {
        return mCurrentIntensity;
    }

    @Override
    public void setCommandedAnalogValue(double value) throws JmriException {
        int origState = mState;
        double origCurrent = mCurrentIntensity;
        
        if (mCurrentIntensity >= getMaxIntensity()) {
            mState = ON;
            mCurrentIntensity = getMaxIntensity();
        } else if (mCurrentIntensity <= getMinIntensity()) {
            mState = OFF;
            mCurrentIntensity = getMinIntensity();
        } else {
            mState = INTERMEDIATE;
            mCurrentIntensity = value;
        }
        
        mTransitionTargetIntensity = mCurrentIntensity;
        
        // first, send the on command
        sendOnOffCommand(mState);
        
        // command new intensity
        sendIntensity(mCurrentIntensity);
        if (log.isDebugEnabled()) {
            log.debug("set analog value: {}", value);
        }
        
        firePropertyChange("CurrentIntensity", origCurrent, mCurrentIntensity);
        if (log.isDebugEnabled()) {
            log.debug("firePropertyChange intensity {} -> {}", origCurrent, mCurrentIntensity);
        }
        
        if (origState != mState) {
            firePropertyChange("KnownState", origState, mState);
            if (log.isDebugEnabled()) {
                log.debug("firePropertyChange intensity {} -> {}", origCurrent, mCurrentIntensity);
            }
        }
    }

    /**
     * Get the current value of the minIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return min intensity value
     */
    @Override
    public double getMinIntensity() {
        return mMinIntensity;
    }

    /**
     * Set the value of the minIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity intensity value
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not less than the
     *                                  current value of the maxIntensity
     *                                  property
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    @Override
    public void setMinIntensity(double intensity) {
        if (intensity < 0.0 || intensity > 1.0) {
            throw new IllegalArgumentException("Illegal intensity value: " + intensity);
        }
        if (intensity >= mMaxIntensity) {
            throw new IllegalArgumentException("Requested intensity " + intensity + " should be less than maxIntensity " + mMaxIntensity);
        }

        double oldValue = mMinIntensity;
        mMinIntensity = intensity;

        if (oldValue != intensity) {
            firePropertyChange("MinIntensity", Double.valueOf(oldValue), Double.valueOf(intensity));
        }
    }

    /**
     * Get the current value of the maxIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return max intensity
     */
    @Override
    public double getMaxIntensity() {
        return mMaxIntensity;
    }

    /**
     * Set the value of the maxIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity max intensity
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not greater than the
     *                                  current value of the minIntensity
     *                                  property
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    @Override
    public void setMaxIntensity(double intensity) {
        if (intensity < 0.0 || intensity > 1.0) {
            throw new IllegalArgumentException("Illegal intensity value: " + intensity);
        }
        if (intensity <= mMinIntensity) {
            throw new IllegalArgumentException("Requested intensity " + intensity + " must be higher than minIntensity " + mMinIntensity);
        }

        double oldValue = mMaxIntensity;
        mMaxIntensity = intensity;

        if (oldValue != intensity) {
            firePropertyChange("MaxIntensity", oldValue, intensity);
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getState(double v) {
        return getCommandedAnalogValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setState(double newState) throws JmriException {
        setCommandedAnalogValue(newState);
    }

    @Override
    public double getResolution() {
        return 1.0 / getNumberOfSteps();
    }

    @Override
    public double getCommandedAnalogValue() {
        return getCurrentIntensity();
    }

    @Override
    public double getMin() {
        return getMinIntensity();
    }

    @Override
    public double getMax() {
        return getMaxIntensity();
    }

    @Override
    public AbsoluteOrRelative getAbsoluteOrRelative() {
        return AbsoluteOrRelative.ABSOLUTE;
    }

}
