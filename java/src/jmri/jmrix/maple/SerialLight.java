// SerialLight.java
package jmri.jmrix.maple;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialLight.java
 *
 * Implementation of the Light Object for C/MRI
 * <P>
 * Based in part on SerialTurnout.java
 *
 * @author Dave Duchamp Copyright (C) 2004, 2009, 2010
 * @version $Revision$
 */
public class SerialLight extends AbstractLight {

    /**
     *
     */
    private static final long serialVersionUID = -7484666573192742681L;

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName) {
        super(systemName);
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName, String userName) {
        super(systemName, userName);
        initializeLight(systemName);
    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        mBit = SerialAddress.getBitFromSystemName(systemName);
        // Set initial state
        setState(OFF);
    }

    /**
     * System dependent instance variables
     */
    int mBit = 0;                // bit within the node

    /**
     * Set the current state of this Light This routine requests the hardware to
     * change. If this is really a change in state of this bit (tested in
     * SerialNode), a Transmit packet will be sent before this Node is next
     * polled.
     */
    protected void doNewState(int oldState, int newState) {
        if (newState == ON) {
            OutputBits.instance().setOutputBit(mBit, false);
        } else if (newState == OFF) {
            OutputBits.instance().setOutputBit(mBit, true);
        } else {
            log.warn("illegal state requested for Light: " + getSystemName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
