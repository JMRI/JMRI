// LnLight.java

package jmri.jmrix.loconet;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractLight;

/**
 * LnLight.java
 *
 * Implementation of the Light Object for Loconet
 * <P>
 *  Based in part on SerialLight.java
 *
 * @author      Dave Duchamp Copyright (C) 2006
 * @version     $Revision$
 */
public class LnLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in LnLightManager
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
     * <P>
     * 'systemName' was previously validated in LnLightManager
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
        setState( OFF );
    }

    int mBit = 0;                // address bit

    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     */
	protected void doNewState(int oldState, int newState) {
		LocoNetMessage l = new LocoNetMessage(4);
		l.setOpCode(LnConstants.OPC_SW_REQ);
		// compute address fields
		int hiadr = (mBit-1)/128;
		int loadr = (mBit-1)-hiadr*128;
		// set bits for ON/OFF
		if (newState==ON) {
			hiadr |= 0x30;
		}
		else if (newState==OFF) {
			hiadr |= 0x10;
		}
		else {
			log.warn("illegal state requested for Light: "+getSystemName());
			hiadr |= 0x10;
        }
		// store and send
		l.setElement(1,loadr);
		l.setElement(2,hiadr);
		tc.sendLocoNetMessage(l);
    }

    static Logger log = Logger.getLogger(LnLight.class.getName());
}

/* @(#)LnLight.java */
