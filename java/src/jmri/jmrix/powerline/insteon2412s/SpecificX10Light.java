package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for X10 receivers on Insteon 2412S
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
public class SpecificX10Light extends jmri.jmrix.powerline.SerialX10Light {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     *
     * @param systemName text for systemName of light
     * @param tc         tc for connection
     */
    public SpecificX10Light(String systemName, SerialTrafficController tc) {
        super(systemName, tc);
        this.tc = tc;
        // fixed number of steps for X10 Insteon
        maxDimStep = 22;
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     *
     * @param systemName text for systemName of light
     * @param tc         tc for connection
     * @param userName   text for userName of light
     */
    public SpecificX10Light(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
        this.tc = tc;
        maxDimStep = 22;
    }

    SerialTrafficController tc = null;

    // System-dependent instance variables
    /**
     * Send a Dim/Bright commands to the X10 hardware to reach a specific
     * intensity. Acts immediately, and changes no general state.
     * <p>
     * This sends "Extended Cmd Dim" commands.
     */
    @Override
    protected void sendIntensity(double intensity) {
        if (log.isDebugEnabled()) {
            log.debug("sendIntensity(" + intensity + ")" + " lastOutputStep: " + lastOutputStep + " maxDimStep: " + maxDimStep);
        }

        // if we don't know the dim count, force it to a value.
//        initIntensity(intensity);
        // find the new correct dim count
        int newStep = (int) Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc

        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep)) {
            log.error("newStep wrong: " + newStep + " intensity: " + intensity);
        }

        if (newStep == lastOutputStep) {
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

    private final static Logger log = LoggerFactory.getLogger(SpecificX10Light.class);
}


