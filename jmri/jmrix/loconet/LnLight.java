// LnLight.java

package jmri.jmrix.loconet;

import jmri.AbstractLight;
// import jmri.SignalHead;
import jmri.Sensor;
import jmri.Turnout;
// import jmri.Light;

/**
 * LnLight.java
 *
 * Implementation of the Light Object for Loconet
 * <P>
 *  Based in part on SerialLight.java
 *
 * @author      Dave Duchamp Copyright (C) 2006
 * @version     $Revision: 1.5 $
 */
public class LnLight extends AbstractLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in LnLightManager
     */
    public LnLight(String systemName) {
        super(systemName);
        // Initialize the Light
        initializeLight(systemName);
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in LnLightManager
     */
    public LnLight(String systemName, String userName) {
        super(systemName, userName);
        initializeLight(systemName);
    }
        
    /**
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Extract the Bit from the name
        mBit = LnLightManager.instance().getBitFromSystemName(systemName);
        // Set initial state
        setState( OFF );
        // Set defaults for all other instance variables
        setControlType( NO_CONTROL );
        setControlSensor( null );
        setControlSensorSense(Sensor.ACTIVE);
        setFastClockControlSchedule( 0,0,0,0 );
        setControlTurnout( null );
        setControlTurnoutState( Turnout.CLOSED );
    }

    /**
     *  System dependent instance variables
     */
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
		LnTrafficController.instance().sendLocoNetMessage(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnLight.class.getName());
}

/* @(#)LnLight.java */
