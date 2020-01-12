package jmri.jmrix.powerline;

import jmri.implementation.AbstractVariableLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for Powerline devices.
 * <p>
 * For X10 devices, uses dimming commands to set intensity unless the value is
 * 0.0 or 1.0, in which case it uses on/off commands only.
 * <p>
 * For Insteon devices, uses direct setting of intensity level unless the value
 * is 0.0 or 1.0, in which case it uses on/off commands only.
 * <p>
 * For X10, since the dim/bright step of the hardware is unknown then the Light
 * object is first created, the first time the intensity (not state) is set to
 * other than 0.0 or 1.0, the output is run to it's maximum dim or bright step
 * so that we know the count is right.
 * <p>
 * For X10, keeps track of the controller's "dim count", and if not certain
 * forces it to zero to be sure.
 * <p>
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @author Ken Cameron Copyright (C) 2009, 2010 Converted to multiple connection
 * @author kcameron Copyright (C) 2011
 */
abstract public class SerialLight extends AbstractVariableLight {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName system name for light
     * @param tc traffic controller
     */
    public SerialLight(String systemName, SerialTrafficController tc) {
        super(systemName);
        this.tc = tc;
        // Initialize the Light
        initializeLight();
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in SerialLightManager
     * @param systemName system name for light
     * @param tc traffic controller
     * @param userName user name for light
     */
    public SerialLight(String systemName, SerialTrafficController tc, String userName) {
        super(systemName, userName);
        this.tc = tc;
        initializeLight();
    }

    SerialTrafficController tc = null;

    /**
     * Invoked from constructors to set up details. Note: most instance
     * variables are in AbstractLight and AbstractVariableLight base classes.
     */
    protected void initializeLight() {
        // Convert to the two-part X10 address
        housecode = tc.getAdapterMemo().getSerialAddress().houseCodeAsValueFromSystemName(getSystemName());
        devicecode = tc.getAdapterMemo().getSerialAddress().deviceCodeAsValueFromSystemName(getSystemName());
        // not an X10, try Insteon
        if (housecode == -1) {
            idhighbyte = tc.getAdapterMemo().getSerialAddress().idHighCodeAsValueFromSystemName(getSystemName());
            idmiddlebyte = tc.getAdapterMemo().getSerialAddress().idMiddleCodeAsValueFromSystemName(getSystemName());
            idlowbyte = tc.getAdapterMemo().getSerialAddress().idLowCodeAsValueFromSystemName(getSystemName());
        }
    }

    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     * <p>
     * Default implementation doesn't do anything.
     * @param intensity float for 0-&gt;1.0
     */
    protected void initIntensity(double intensity) {
    }

    // data members holding the address forms
    protected int housecode = -1;
    protected int devicecode = -1;
    protected int idhighbyte = -1;
    protected int idmiddlebyte = -1;
    protected int idlowbyte = -1;

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

        if (log.isDebugEnabled()) {
            log.debug("set state " + newState + " house " + X10Sequence.houseCodeToText(housecode) + " device " + devicecode);
        }

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

    private final static Logger log = LoggerFactory.getLogger(SerialLight.class);
}


