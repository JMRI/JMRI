package jmri.jmrix.powerline.cp290;

import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.X10Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for X10 for CP290 interfaces.
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
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2010 Converted to
 * multiple connection
 * @author kcameron Copyright (C) 2011
 */
public class SpecificLight extends jmri.jmrix.powerline.SerialX10Light {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName systemName for light
     * @param tc         traffic controller for connection
     */
    public SpecificLight(String systemName, SerialTrafficController tc) {
        super(systemName, tc);
        this.tc = tc;
        maxDimStep = tc.getNumberOfIntensitySteps();
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName systemName for light
     * @param tc         tc for connection
     * @param userName   userName for light
     */
    public SpecificLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, tc, userName);
        this.tc = tc;
        maxDimStep = tc.getNumberOfIntensitySteps();
    }

    SerialTrafficController tc = null;

    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     */
    @Override
    protected void initIntensity(double intensity) {
        maxDimStep = tc.getNumberOfIntensitySteps();

        // Set initial state
        // see if going to stabilize at on or off
        if (intensity <= 0.5) {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to low, first set off
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_OFF, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_DIM, maxDimStep);
            // send
            tc.sendX10Sequence(out, null);

            lastOutputStep = 0;

            if (log.isDebugEnabled()) {
                log.debug("initIntensity: sent dim reset");
            }
        } else {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to high, first set on
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_ON, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, maxDimStep);
            // send
            tc.sendX10Sequence(out, null);

            lastOutputStep = maxDimStep;

            if (log.isDebugEnabled()) {
                log.debug("initIntensity: sent bright reset");
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpecificLight.class);
}
