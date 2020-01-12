package jmri.jmrix.loconet;

import jmri.implementation.AbstractLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Light Object for LocoNet
 * <p>
 * Based in part on SerialLight.java
 *
 * @author Dave Duchamp Copyright (C) 2006
 */
public class LnLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <p>
     * 'systemName' was previously validated in LnLightManager
     * 
     * @param systemName for the new bean
     * @param tc the LnTrafficController which handles the messaging
     * @param mgr the LnLightManager which manages this type of object bean
     */
    public LnLight(String systemName, LnTrafficController tc, LnLightManager mgr) {
        super(systemName);
        this.tc = tc;
        this.mgr = mgr;
        // Initialize the Light
        initializeLight(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <p>
     * 'systemName' was previously validated in LnLightManager
     * 
     * @param systemName for the new bean
     * @param userName for the new bean
     * @param tc the LnTrafficController which handles the messaging
     * @param mgr the LnLightManager which manages this type of object bean
     */
    public LnLight(String systemName, String userName, LnTrafficController tc, LnLightManager mgr) {
        super(systemName, userName);
        this.tc = tc;
        this.mgr = mgr;
        initializeLight(systemName);
    }

    LnTrafficController tc;
    LnLightManager mgr;

    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        mBit = mgr.getBitFromSystemName(systemName);
        // Set initial state
        setState(OFF);
    }

    int mBit = 0;                // address bit

    /**
     * Set the current state of this Light This routine requests the hardware to
     * change.
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_SW_REQ);
        // compute address fields
        int hiadr = (mBit - 1) / 128;
        int loadr = (mBit - 1) - hiadr * 128;
        // set bits for ON/OFF
        if (newState == ON) {
            hiadr |= 0x30;
        } else if (newState == OFF) {
            hiadr |= 0x10;
        } else {
            log.warn("illegal state requested for Light: " + getSystemName());
            hiadr |= 0x10;
        }
        // store and send
        l.setElement(1, loadr);
        l.setElement(2, hiadr);
        tc.sendLocoNetMessage(l);
    }

    private final static Logger log = LoggerFactory.getLogger(LnLight.class);

}
