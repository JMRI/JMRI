// SerialLight.java

package jmri.jmrix.grapevine;

import jmri.AbstractLight;
import jmri.Sensor;
import jmri.Turnout;

/**
 * Implementation of the Light interface using Grapevine
 * signal ports.  
 * <P>
 * The "On" state results in sending a "green" setting to the hardware
 * port; the "Off" state results in sending a "dark" setting to the hardware.
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version     $Revision: 1.5 $
 */
public class SerialLight extends AbstractLight {

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
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight(String systemName) {
        // Save system name
        mSystemName = systemName;

        // Extract the Bit from the name
        int num = SerialAddress.getBitFromSystemName(systemName); // bit one is address zero
        // num is 101-124, 201-224, 301-324, 401-424
        output = (num%100)-1; // 0-23
        bank = (num/100)-1;  // 0 - 3

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
    String mSystemName = "";     // system name 
    protected int mState = OFF;  // current state of this light
    int output;         // output connector number, 0-23
    int bank;           // bank number, 0-3

    /**
     *  Return the current state of this Light
     */
    public int getState() { return mState; }

    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     *     If this is really a change in state of this 
     *         bit (tested in SerialNode), a Transmit packet
     *         will be sent before this Node is next polled.
     */
    public void setState(int newState) {
        SerialNode mNode = SerialAddress.getNodeFromSystemName(mSystemName);
        if (mNode!=null) {
            if (newState==ON) {
                sendMessage(true);
            }
            else if (newState==OFF) {
                sendMessage(false);
            }
            else {
                log.warn("illegal state requested for Light: "+getSystemName());
            }
        }
		if (newState!=mState) {
			int oldState = mState;
			mState = newState;
            // notify listeners, if any
            firePropertyChange("KnownState", new Integer(oldState), new Integer(newState));
		}
    }

    protected void sendMessage(boolean on) {
        SerialNode tNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (tNode == null) {
            // node does not exist, ignore call
            log.error("Can't find node for "+getSystemName()+", command ignored");
            return;
        }
        boolean high = (output>=12);
        if (high) output = output-12;
        if ( (bank<0)||(bank>4) ) {
            log.error("invalid bank "+bank+" for Turnout "+getSystemName());
            bank = 0;
        }
        SerialMessage m = new SerialMessage(high?8:4);
        int i = 0;
        if (high) {
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
            m.setElement(i++,122);   // shift command
            m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
            m.setElement(i++,0x10);  // bank 1
            m.setParity(i-4);
        }
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 1
        m.setElement(i++, (output<<3)|(on ? 0 : 4));  // on is green, off is dark
        m.setElement(i++,tNode.getNodeAddress()|0x80);  // address 2
        m.setElement(i++, bank<<4); // bank is most significant bits
        m.setParity(i-4);
        SerialTrafficController.instance().sendSerialMessage(m, null);
    }

    // added dummy methods for dimmable stuff
    public double getDimRequest() {
    	if (mState == OFF) {
    		return(0);
    	} else {
    		return(1);
        }
    }
    public double getDimCurrent() {
    	if (mState == OFF) {
    		return(0);
    	} else {
    		return(1);
        }
    }
    public void setDimRequest(double v) {
    	if (v > 0) {
    		setState(ON);
    	} else {
    		setState(OFF);
    	}
    }
    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
