package jmri.implementation;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import jmri.JmriException;
import jmri.Light;
import jmri.LightControl;

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
    protected List<LightControl> lightControlList = new ArrayList<>();
    protected double mMaxIntensity = 1.0;
    protected double mMinIntensity = 0.0;

    /**
     * System independent operational instance variables (not saved between
     * runs).
     */
    protected double mCurrentIntensity = 0.0;
    protected boolean mActive = false; // used to indicate if LightControls are active
    protected boolean mEnabled = true;
    protected int mState = OFF;

    @Override
    @Nonnull
    public String describeState(int state) {
        switch (state) {
            case ON: return Bundle.getMessage("StateOn");
            case OFF: return Bundle.getMessage("StateOff");
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
            firePropertyChange("Enabled", old, v);
        }
    }

    /**
     * Handle a request for a state change. For these lights, ON and OFF just
     * transition immediately between MinIntensity and MaxIntensity.
     * Ignores any outputDelay setting for connection.
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
            firePropertyChange("KnownState", oldState, newState);
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
    public void addLightControl(LightControl c) {
        if (lightControlList.contains(c)) {
            log.debug("not adding duplicate LightControl {}", c);
            return;
        }
        lightControlList.add(c);
    }

    @Override
    public List<LightControl> getLightControlList() {
        List<LightControl> listCopy = new ArrayList<>();
        lightControlList.stream().forEach((lightControlList1) -> {
            listCopy.add(lightControlList1);
        });
        return listCopy;
    }

    @Override
    public List<jmri.NamedBeanUsageReport> getUsageReport(jmri.NamedBean bean) {
        List<jmri.NamedBeanUsageReport> report = new ArrayList<>();
        jmri.SensorManager sm = jmri.InstanceManager.getDefault(jmri.SensorManager.class);
        jmri.TurnoutManager tm = jmri.InstanceManager.getDefault(jmri.TurnoutManager.class);
        if (bean != null) {
            getLightControlList().forEach((control) -> {
                String descText = control.getDescriptionText("");
                if (bean.equals(sm.getSensor(control.getControlSensorName()))) {
                    report.add(new jmri.NamedBeanUsageReport("LightControlSensor1", descText));  // NOI18N
                }
                if (bean.equals(sm.getSensor(control.getControlSensor2Name()))) {
                    report.add(new jmri.NamedBeanUsageReport("LightControlSensor2", descText));  // NOI18N
                }
                if (bean.equals(sm.getSensor(control.getControlTimedOnSensorName()))) {
                    report.add(new jmri.NamedBeanUsageReport("LightControlSensorTimed", descText));  // NOI18N
                }
                if (bean.equals(tm.getTurnout(control.getControlTurnoutName()))) {
                    report.add(new jmri.NamedBeanUsageReport("LightControlTurnout", descText));  // NOI18N
                }
            });
        }
        return report;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractLight.class);

}
