package jmri.jmrix.maple;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for C/MRI
 * <p>
 * Based in part on SerialTurnout.java
 *
 * @author Dave Duchamp Copyright (C) 2004, 2009, 2010
 */
public class SerialLight extends AbstractLight {

    private MapleSystemConnectionMemo _memo = null;

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' has already been validated in SerialLightManager
     *
     * @param systemName the system name for this Light
     */
    public SerialLight(String systemName, MapleSystemConnectionMemo memo) {
        super(systemName);
        _memo = memo;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' has already been validated in SerialLightManager
     *
     * @param systemName the system name for this Light
     * @param userName   the user name for this Light
     */
    public SerialLight(String systemName, String userName, MapleSystemConnectionMemo memo) {
        super(systemName, userName);
        _memo = memo;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        mBit = SerialAddress.getBitFromSystemName(systemName, _memo.getSystemPrefix());
        // Set initial state
        setState(OFF);
    }

    /**
     * System dependent instance variables
     */
    int mBit = 0; // bit within the node

    /**
     * Set the current state of this Light This routine requests the hardware to
     * change. If this is really a change in state of this bit (tested in
     * SerialNode), a Transmit packet will be sent before this Node is next
     * polled.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        if (newState == ON) {
            _memo.getTrafficController().outputBits().setOutputBit(mBit, false);
        } else if (newState == OFF) {
            _memo.getTrafficController().outputBits().setOutputBit(mBit, true);
        } else {
            log.warn("illegal state requested for Light: {}", getSystemName());
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLight.class);

}
