package jmri.jmrix.powerline.dmx512;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.powerline.SerialTrafficController;

/**
 * Implementation of the Light class for DMX based subclasses.
 * <p>
 * DMX maps the value of 0.0 to 1.0 to values of 0 to 255.
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author Ken Cameron Copyright (C) 2009, 2010 Converted to multiple connection
 * @author kcameron Copyright (C) 2023
 */
public class SpecificDmxLight extends jmri.jmrix.powerline.SerialLight {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName system name
     * @param tc traffic controller
     */
    public SpecificDmxLight(String systemName, SerialTrafficController tc) {
        super(systemName, tc);
        this.tc = tc;
        maxDimStep = tc.getNumberOfIntensitySteps();
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName system name
     * @param tc traffic controller
     * @param userName user name
     */
    public SpecificDmxLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
        this.tc = tc;
        maxDimStep = tc.getNumberOfIntensitySteps();
    }

    SerialTrafficController tc = null;
    protected int maxDimStep = 0;

    /**
     * Set the intensity for the DMX hardware to reach a specific
     * intensity. Acts immediately, and changes no general state.
     */
    @Override
    protected void sendIntensity(double intensity) {
        // correct for out of range value
        if ((intensity < mMinIntensity) || (intensity > mMaxIntensity)) {
            log.debug("correcting out of range intensity: {}", intensity);
            if (intensity < mMinIntensity) {
                intensity = mMinIntensity;
            }
            if (intensity > mMaxIntensity) {
                intensity = mMaxIntensity;
            }
        }
        // test current too, if the change is out of range...
        if ((mCurrentIntensity < mMinIntensity) || (mCurrentIntensity > mMaxIntensity)) {
            log.debug("correcting out of range current intensity: {}", mCurrentIntensity);
            if (mCurrentIntensity < mMinIntensity) {
                mCurrentIntensity = mMinIntensity;
            }
            if (mCurrentIntensity > mMaxIntensity) {
                mCurrentIntensity = mMaxIntensity;
            }
        }
        
        if (log.isDebugEnabled()) {
            log.debug("sendIntensity({}) maxDimStep: {}", intensity, maxDimStep);
        }

        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep)) {
            log.error("newStep wrong: {} intensity: {} mCurrentIntensity {}", newStep, intensity, mCurrentIntensity);
            return;
        }

        // update dmxArray
        tc.sendDmxSequence(this.unitid, (byte) newStep);
    }

    /**
     * Number of steps from dim to bright is maintained in specific
     * SerialTrafficController implementation
     */
    @Override
    protected int getNumberOfSteps() {
        return tc.getNumberOfIntensitySteps();
    }

    /**
     * Send a On/Off Command to the hardware
     */
    @Override
    protected void sendOnOffCommand(int newState) {
        if (log.isDebugEnabled()) {
            log.debug("sendOnOff({}) Current: {}", newState, mState);
        }

        // figure out command 
        byte newDim;
        if (newState == ON) {
            newDim = (byte) maxDimStep;
            // set new intensity, skip stepping
            mCurrentIntensity = 1;
            mTransitionTargetIntensity = 1;
        } else if (newState == OFF) {
            newDim = 0;
            // set new intensity, skip stepping
            mCurrentIntensity = 0;
            mTransitionTargetIntensity = 0;
        } else {
            log.warn("illegal state requested for Light: {}", getSystemName());
            return;
        }

        log.debug("set state {} unitid {} value {}", newState, unitid, newDim);
        
        // send value to array
        tc.sendDmxSequence(this.unitid, newDim);

        if (log.isDebugEnabled()) {
            log.debug("sendOnOff() unit {} state {} value {} ", this.unitid, newState, newDim);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificDmxLight.class);
}


