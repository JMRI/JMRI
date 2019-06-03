package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.InsteonSequence;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for Insteon receivers on Insteon 2412S
 * interfaces.
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
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009, 2010
 * @author Ken Cameron Copyright (C) 2009, 2010 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificInsteonLight extends jmri.jmrix.powerline.SerialLight {

    // System-dependent instance variables
    /**
     * Current output step 0 to maxDimStep.
     * <p>
     * -1 means unknown
     */
    int lastOutputStep = -1;

    /**
     * Largest Insteon dim step number available.
     */
    int maxDimStep = 255;

    /**
     * Value for retransmission
     */
    int maxHops = Constants.FLAG_MAXHOPS_DEFAULT;

    public int getMaxHops() {
        return maxHops;
    }

    public void setMaxHops(int maxHops) {
        if (maxHops <= Constants.FLAG_MASK_MAXHOPS && maxHops >= 0) {
            this.maxHops = maxHops;
        } else {
            log.error("setMaxHops out of range: " + maxHops);
        }
    }

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName text for systemName of light
     * @param tc         tc for connection
     */
    public SpecificInsteonLight(String systemName, SerialTrafficController tc) {
        super(systemName, tc);
        this.tc = tc;
        // maxDimStep = tc.getNumberOfIntensitySteps();
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName text for systemName of light
     * @param tc         tc for connection
     * @param userName   text for userName of light
     */
    public SpecificInsteonLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
        this.tc = tc;
        //maxDimStep = tc.getNumberOfIntensitySteps();
    }

    SerialTrafficController tc = null;

    /**
     * Invoked the first time intensity is set.
     *
     * @param intensity The next intensity value that will be set
     */
    @Override
    protected void initIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("initIntensity(" + intensity + ")");
        }
    }

    /**
     * Send a Dim/Bright command to the Insteon hardware to reach a specific
     * intensity. Acts immediately, and changes no general state.
     * <p>
     * This sends "Dim" commands.
     */
    @Override
    protected void sendIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("sendIntensity(" + intensity + ")" + " lastOutputStep: " + lastOutputStep + " maxDimStep: " + maxDimStep);
        }

        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep)) {
            log.error("newStep wrong: " + newStep + " intensity: " + intensity);
        }

        // do we have any change to make
        if (newStep == lastOutputStep) {
            // nothing to do!
            if (log.isDebugEnabled()) {
                log.debug("intensity " + intensity + " within current step, return");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("function set Intensity " + intensity);
        }

        // create output sequence of address, then function
        InsteonSequence out = new InsteonSequence();
        out.addFunction(idhighbyte, idmiddlebyte, idlowbyte, Constants.FUNCTION_REQ_STD, (Constants.FLAG_STD | (maxHops << Constants.FLAG_SHIFT_HOPSLEFT) | maxHops), Constants.CMD_LIGHT_CHG, newStep);
        // send
        tc.sendInsteonSequence(out, null);

        if (log.isDebugEnabled()) {
            log.debug("sendIntensity(" + intensity + ") addr " + idhighbyte + idmiddlebyte + idlowbyte + " newStep " + newStep);
        }

        lastOutputStep = newStep;
    }

    /**
     * Number of steps from dim to bright is maintained in specific
     * SerialTrafficController implementation
     */
    @Override
    protected int getNumberOfSteps() {
        return maxDimStep;
    }

    /**
     * Send a On/Off Command to the hardware
     */
    @Override
    protected void sendOnOffCommand(int newState) {
        if (log.isDebugEnabled()) {
            log.debug("start sendOnOff(" + newState + ") Current: " + mState);
        }

        // figure out command 
        int command1;
        if (newState == ON) {
            command1 = Constants.CMD_LIGHT_ON_FAST;
        } else if (newState == OFF) {
            command1 = Constants.CMD_LIGHT_OFF_FAST;
        } else {
            log.warn("illegal state requested for Light: " + getSystemName());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("set state " + newState + " " + getSystemName());
        }

        // create output sequence of just address and function together
        InsteonSequence out = new InsteonSequence();
        out.addFunction(idhighbyte, idmiddlebyte, idlowbyte, Constants.FUNCTION_REQ_STD, (Constants.FLAG_STD | (maxHops << Constants.FLAG_SHIFT_HOPSLEFT) | maxHops), command1, 0);
        // send
        tc.sendInsteonSequence(out, null);

        if (log.isDebugEnabled()) {
            log.debug("end sendOnOff(" + newState + ")  insteon " + StringUtil.twoHexFromInt(idhighbyte) + "." + StringUtil.twoHexFromInt(idmiddlebyte) + "." + StringUtil.twoHexFromInt(idlowbyte) + " cmd1: " + command1);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificInsteonLight.class);
}
