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
 * @author kcameron Copyright (C) 2011
 */
public class SerialX10Light extends jmri.jmrix.powerline.SerialLight {

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
    public SerialX10Light(String systemName, SerialTrafficController tc) {
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
    public SerialX10Light(String systemName, SerialTrafficController tc, String userName) {
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
            log.debug("initIntensity(" + intensity + ")");
        }

        maxDimStep = tc.getNumberOfIntensitySteps();

        // Set initial state
        // see if going to stabilize at on or off
        if (intensity <= 0.5) {
            // going to low, send a real off
            X10Sequence out3 = new X10Sequence();
            out3.addAddress(housecode, devicecode);
            out3.addFunction(housecode, X10Sequence.FUNCTION_OFF, 0);
            tc.sendX10Sequence(out3, null);
            // going to low, send max dim count low
            X10Sequence out2 = new X10Sequence();
            out2.addAddress(housecode, devicecode);
            out2.addFunction(housecode, X10Sequence.FUNCTION_DIM, maxDimStep);
            tc.sendX10Sequence(out2, null);

            lastOutputStep = 0;

            if (log.isDebugEnabled()) {
                log.debug("initIntensity: sent dim reset");
            }
        } else {
            // going to high, send a real on
            X10Sequence out3 = new X10Sequence();
            out3.addAddress(housecode, devicecode);
            out3.addFunction(housecode, X10Sequence.FUNCTION_ON, 0);
            tc.sendX10Sequence(out3, null);
            // going to high, send max dim count high
            X10Sequence out2 = new X10Sequence();
            out2.addAddress(housecode, devicecode);
            out2.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, maxDimStep);
            // send
            tc.sendX10Sequence(out2, null);

            lastOutputStep = maxDimStep;

            if (log.isDebugEnabled()) {
                log.debug("initIntensity: sent bright reset");
            }
        }
    }

    /**
     * Send a Dim/Bright commands to the X10 hardware to reach a specific
     * intensity. Acts immediately, and changes no general state.
     * <p>
     * This sends "Dim" commands.
     */
    @Override
    protected void sendIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("sendIntensity(" + intensity + ")" + " lastOutputStep: " + lastOutputStep + " maxDimStep: " + maxDimStep);
        }

        // if we don't know the dim count, force it to a value.
//        if (lastOutputStep < 0) initIntensity(intensity);
        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep)) {
            log.error("newStep wrong: " + newStep + " intensity: " + intensity);
        }

        if (newStep == 0) {
            // nothing to do!
            if (log.isDebugEnabled()) {
                log.debug("intensity " + intensity + " within current step, return");
            }
            return;

        }

        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addExtData(housecode, devicecode, X10Sequence.EXTCMD_DIM, newStep);
        // send
        tc.sendX10Sequence(out, null);
        lastOutputStep = newStep;

        if (log.isDebugEnabled()) {
            log.debug("sendIntensity(" + intensity + ") house " + X10Sequence.houseValueToText(housecode) + " device " + devicecode + " newStep: " + newStep);
        }
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
            log.debug("sendOnOff(" + newState + ") Current: " + mState);
        }

        // figure out command 
        int function;
        double newDim;
        if (newState == ON) {
            function = X10Sequence.FUNCTION_ON;
            newDim = 1;
        } else if (newState == OFF) {
            function = X10Sequence.FUNCTION_OFF;
            newDim = 0;
        } else {
            log.warn("illegal state requested for Light: " + getSystemName());
            return;
        }

        log.debug("set state " + newState + " house " + housecode + " device " + devicecode);

        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, 0);
        // send
        tc.sendX10Sequence(out, null);

        if (log.isDebugEnabled()) {
            log.debug("sendOnOff(" + newDim + ")  house " + X10Sequence.houseValueToText(housecode) + " device " + devicecode + " funct: " + function);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialX10Light.class);
}


