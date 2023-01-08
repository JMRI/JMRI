package jmri.jmrix.powerline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light class for X10-based subclasses.
 * <p>
 * Uses X10 dimming commands to set intensity unless the value is 0.0 or 1.0, in
 * which case it uses on/off commands only.
 * <p>
 * Since the dim/bright step of the hardware is unknown then the Light object is
 * first created, the first time the intensity (not state) is set to other than
 * 0.0 or 1.0, the output is run to it's maximum dim or bright step so that we
 * know the count is right.
 * <p>
 * Keeps track of the controller's "dim count", and if not certain forces it to
 * zero to be sure.
 * <p>
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author Ken Cameron Copyright (C) 2009, 2010 Converted to multiple connection
 * @author kcameron Copyright (C) 2023
 */
public class SerialDmxLight extends jmri.jmrix.powerline.SerialLight {

    // System-dependent instance variables
    /**
     * Current output step 0 to maxDimStep.
     * <p>
     * -1 means unknown
     */
    protected int lastOutputStep = -1;

    /**
     * Largest X10 dim step number available.
     * <p>
     * Loaded from SerialTrafficController.getNumberOfIntensitySteps();
     */
    protected int maxDimStep = 0;

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName system name
     * @param tc traffic controller
     */
    public SerialDmxLight(String systemName, SerialTrafficController tc) {
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
    public SerialDmxLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
        this.tc = tc;
        maxDimStep = tc.getNumberOfIntensitySteps();
    }

    SerialTrafficController tc = null;

    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     *
     * @param intensity The next intensity value that will be set
     */
    @Override
    protected void initIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("initIntensity({})", intensity);
        }

        maxDimStep = tc.getNumberOfIntensitySteps();
        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        boolean didOk = false;
        // update dmxArray
        tc.sendDmxSequence(this.unitid, (byte) newStep);

        if (log.isDebugEnabled()) {
            log.debug("sendIntensity({}) unitId {} intensity {} value {}: worked {}", this.unitid, intensity, newStep, didOk);
        }
    }

    /**
     * Send a Dim/Bright commands to the DMX hardware to reach a specific
     * intensity. Acts immediately, and changes no general state.
     * <p>
     * This sends "Dim" commands.
     */
    @Override
    protected void sendIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("sendIntensity({}) lastOutputStep: {} maxDimStep: {}", intensity, lastOutputStep, maxDimStep);
        }

        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep)) {
            log.error("newStep wrong: {} intensity: {}", newStep, intensity);
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
        } else if (newState == OFF) {
            newDim = 0;
        } else {
            log.warn("illegal state requested for Light: {}", getSystemName());
            return;
        }

        log.debug("set state {} house {} device {}", newState, housecode, devicecode);

        // send value to array
        tc.sendDmxSequence(this.unitid, newDim);

        if (log.isDebugEnabled()) {
            log.debug("sendOnOff({}) unit {} state {} value {} ", this.unitid, newState, newDim);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialDmxLight.class);
}


