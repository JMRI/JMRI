package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Light;

/**
 * Abstract class providing partial implementation of the Light interface.
 * <p>
 * Light objects require a number of instance variables. Since Light objects are
 * created using the standard JMRI systemName/userName concept, accessor
 * routines are provided for setting and editing these instance variables.
 * <p>
 * Each Light may have one or more control mechanisms, of the types defined in
 * the Light interface. A Light may also not have any control mechanisms
 * defined.
 * <p>
 * Information for each control mechanism is held in LightControl objects, which
 * also implement the logic for control. A list of LightControls, if any, is
 * kept here, and activation and deactivation of LightControls is through this
 * module.
 * <p>
 * Instance variables are divided into system-independent and system dependent
 * categories. System independent instance variables are defined here, and their
 * accessor routines are implemented here.
 * <p>
 * This implementation provides a notional implementation of intensity and
 * transitions. The user can set intensity so long as it's at least the max
 * value (default 1.0) or no more than the minimum value (default 0.0). In that
 * case, the setTargetIntensity operations become a setState to ON or OFF.
 * Setting a target intensity between the min and max is an error, because this
 * type of Light does not support a true analog intensity. Transitions never
 * happen, and setting a TransitionTime greater than 0.0 gives an exception.
 * <p>
 * Since this form of Light does not do variable intensity nor transitions, it
 * stores both CurrentIntensity and TargetIntensity in a single location,
 * forcing them to be the same
 *
 * @author Dave Duchamp Copyright (C) 2004, 2010
 * @author Ken Cameron Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public abstract class AbstractLight extends AbstractNamedBean
        implements Light {

    public AbstractLight(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractLight(String systemName) {
        super(systemName);
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameLight");
    }

    /**
     * System independent instance variables (saved between runs).
     */
    protected ArrayList<LightControl> lightControlList = new ArrayList<>();
    protected double mMaxIntensity = 1.0;
    protected double mMinIntensity = 0.0;

    /**
     * System independent operational instance variables (not saved between
     * runs).
     */
    protected boolean mActive = false; // used to indicate if LightControls are active
    protected boolean mEnabled = true;
    protected double mCurrentIntensity = 0.0;
    protected int mState = OFF;

    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case ON: return Bundle.getMessage("StateOn");
            case OFF: return Bundle.getMessage("StateOff");
            case INTERMEDIATE: return Bundle.getMessage("LightStateIntermediate");
            case TRANSITIONINGTOFULLON: return Bundle.getMessage("LightStateTransitioningToFullOn");
            case TRANSITIONINGHIGHER: return Bundle.getMessage("LightStateTransitioningHigher");
            case TRANSITIONINGLOWER: return Bundle.getMessage("LightStateTransitioningLower");
            case TRANSITIONINGTOFULLOFF: return Bundle.getMessage("LightStateTransitioningToFullOff");
            default: return super.describeState(state);
        }
    }

    /**
     * Get enabled status.
     * 
     * @return enabled status
     */
    @Override
    public boolean getEnabled() {
        return mEnabled;
    }

    /**
     * Set enabled status.
     * 
     * @param v status to set
     */
    @Override
    public void setEnabled(boolean v) {
        boolean old = mEnabled;
        mEnabled = v;
        if (old != v) {
            firePropertyChange("Enabled", Boolean.valueOf(old), Boolean.valueOf(v));
        }
    }

    /**
     * Check if this object can handle variable intensity.
     * <p>
     * @return false, as this abstract class does not implement variable
     *         intensity. See e.g. {@link AbstractVariableLight} for an abstract
     *         implementation of variable intensity.
     */
    @Override
    public boolean isIntensityVariable() {
        return false;
    }

    /**
     * Set the intended new intensity value for the Light.
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
     * All others result in an exception, instead of the INTERMEDIATE state,
     * because this class does not implement analog intensity.
     *
     * @param intensity target intensity value
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is more than MinIntensity
     *                                  and less than MaxIntensity
     */
    @Override
    public void setTargetIntensity(double intensity) {
        log.debug("setTargetIntensity {}", intensity);
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

        // move directly to target, if possible
        if (intensity >= mMaxIntensity) {
            updateIntensityHigh(intensity);
        } else if (intensity <= mMinIntensity) {
            updateIntensityLow(intensity);
        } else {
            updateIntensityIntermediate(intensity);
        }
    }

    /**
     * Method for further implementation of setTargetIntensity at or below the
     * minimum.
     * <p>
     * Does not change state.
     * 
     * @param intensity low intensity value
     */
    protected void updateIntensityLow(double intensity) {
        notifyTargetIntensityChange(intensity);
        setState(OFF);
    }

    /**
     * Method for further implementation of setTargetIntensity between min and
     * max
     * <p>
     * Does not change state.
     * 
     * @param intensity intermediate intensity value
     */
    protected void updateIntensityIntermediate(double intensity) {
        // not in value range!
        throw new IllegalArgumentException("intensity value " + intensity + " between min " + mMinIntensity + " and max " + mMaxIntensity);
    }

    /**
     * Method for further implementation of setTargetIntensity at or above the
     * maximum
     * <p>
     * Does not change state.
     * 
     * @param intensity high intensity value
     */
    protected void updateIntensityHigh(double intensity) {
        notifyTargetIntensityChange(intensity);
        setState(ON);
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
     * Can the Light change its intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the transitionTime
     * property, which controls how long the Light will take to change from one
     * intensity level to another.
     * <p>
     * Unbound property
     *
     * @return transition availability
     */
    @Override
    public boolean isTransitionAvailable() {
        return false;
    }

    /**
     * Set the fast-clock duration for a transition from full ON to full OFF or
     * vice-versa.
     * <p>
     * This class does not implement transitions, so this property cannot be set
     * from zero.
     * <p>
     * Bound property
     *
     * @param minutes transition duration
     * @throws IllegalArgumentException if minutes is not 0.0
     */
    @Override
    public void setTransitionTime(double minutes) {
        if (minutes != 0.0) {
            throw new IllegalArgumentException("Illegal transition time: " + minutes);
        }
    }

    /**
     * Get the number of fastclock minutes taken by a transition from full ON to
     * full OFF or vice versa.
     *
     * @return 0.0 if the output intensity transition is instantaneous
     */
    @Override
    public double getTransitionTime() {
        return 0.0;
    }

    /**
     * Convenience method for checking if the intensity of the light is
     * currently changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the
     * transition is over.
     *
     * @return is transitioning, returns false unless overridden
     */
    @Override
    public boolean isTransitioning() {
        return false;
    }

    /**
     * Handle a request for a state change. For these lights, ON and OFF just
     * transition immediately between MinIntensity and MaxIntensity.
     *
     * @param newState new state
     */
    @Override
    public void setState(int newState) {
        log.debug("setState {} was {}", newState, mState);
        //int oldState = mState;
        if (newState != ON && newState != OFF) {
            throw new IllegalArgumentException("cannot set state value " + newState);
        }
        double intensity = getTargetIntensity();
        if (newState == ON && intensity < getMaxIntensity()) {
            setTargetIntensity(getMaxIntensity());
            // stop if state change was done as part of setTargetIntensity
            if (getState() == ON) {
                return;
            }
        }
        if (newState == OFF && intensity > getMinIntensity()) {
            setTargetIntensity(getMinIntensity());
            // stop if state change was done as part of setTargetIntensity
            if (getState() == OFF) {
                return;
            }
        }
        // do the state change in the hardware
        doNewState(mState, newState); // old state, new state
        // change value and tell listeners
        notifyStateChange(mState, newState);
    }

    /**
     * Change the stored target intensity value and do notification, but don't
     * change anything in the hardware.
     *
     * @param intensity intensity value
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY", justification = "OK to compare floating point")
    protected void notifyTargetIntensityChange(double intensity) {
        double oldValue = mCurrentIntensity;
        mCurrentIntensity = intensity;
        if (oldValue != intensity) {
            firePropertyChange("TargetIntensity", oldValue, intensity);
        }
    }

    /**
     * Change the stored state value and do notification, but don't change
     * anything in the hardware.
     *
     * @param oldState old value
     * @param newState new value
     */
    protected void notifyStateChange(int oldState, int newState) {
        mState = newState;
        if (oldState != newState) {
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
        }
    }

    /**
     * Implement the specific change of state needed by hardware.
     *
     * @param oldState old state
     * @param newState new state
     */
    protected void doNewState(int oldState, int newState) {
    }

    @Override
    public int getState() {
        return mState;
    }

    /**
     * Activate a light activating all its LightControl objects.
     */
    @Override
    public void activateLight() {
        lightControlList.stream().forEach((lc) -> {
            lc.activateLightControl();
        });
        mActive = true; // set flag for control listeners
    }

    /**
     * Deactivate a light by deactivating each of its LightControl objects.
     */
    @Override
    public void deactivateLight() {
        // skip if Light is not active
        if (mActive) { // check if flag set for control listeners
            lightControlList.stream().forEach((lc) -> {
                lc.deactivateLightControl();
            });
            mActive = false; // unset flag for control listeners
        }
    }

    /*
     * LightControl management methods
     */

    @Override
    public void clearLightControls() {
        // deactivate all Light Controls if any are active
        deactivateLight();
        // clear all LightControls, if there are any
        for (int i = lightControlList.size() - 1; i >= 0; i--) {
            lightControlList.remove(i);
        }
    }

    /** {@inheritDoc}
     */
    @Override
    public void addLightControl(jmri.implementation.LightControl c) {
        if (lightControlList.contains(c)) {
            log.debug("not adding duplicate LightControl {}", c);
            return;
        }
        lightControlList.add(c);
    }

    @Override
    public ArrayList<LightControl> getLightControlList() {
        ArrayList<LightControl> listCopy = new ArrayList<>();
        lightControlList.stream().forEach((lightControlList1) -> {
            listCopy.add(lightControlList1);
        });
        return listCopy;
    }

    @Override
    public void setCommandedAnalogValue(double value) throws JmriException {
        double middle = (getMax() - getMin()) / 2 + getMin();
        
        if (value > middle) {
            setCommandedState(ON);
        } else {
            setCommandedState(OFF);
        }
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
    public double getResolution() {
        // AbstractLight is by default only ON or OFF
        return (getMaxIntensity() - getMinIntensity());
    }

    @Override
    public AbsoluteOrRelative getAbsoluteOrRelative() {
        return AbsoluteOrRelative.ABSOLUTE;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractLight.class);

}
