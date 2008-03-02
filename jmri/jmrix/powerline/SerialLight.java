// SerialLight.java

package jmri.jmrix.powerline;

import jmri.AbstractVariableLight;
import jmri.Sensor;
import jmri.Turnout;
import java.util.Date;

/**
 * SerialLight.java
 *
 * Implementation of the Light Object
 * <P>
 * Uses X10 dimming commands to set intensity unless
 * the value is 0 or 1, in which case it uses on/off commands.
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version     $Revision: 1.7 $
 */
public class SerialLight extends AbstractVariableLight {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName) {
        super(systemName);
        // Initialize the Light
        initializeLight();
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SerialLight(String systemName, String userName) {
        super(systemName, userName);
        initializeLight();
    }
        
    /**
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */
    private void initializeLight() {
        // Extract the Bit from the name
        mBit = SerialAddress.getBitFromSystemName(getSystemName());
        // Set defaults for all other instance variables
        setControlType( NO_CONTROL );
        setControlSensor( null );
        setControlSensorSense(Sensor.ACTIVE);
        setFastClockControlSchedule( 0,0,0,0 );
        setControlTurnout( null );
        setControlTurnoutState( Turnout.CLOSED );
    }

    private void initIntensity() {
        // Set initial state
        // address message, then function
        int housecode = ((mBit-1)/16)+1;
        int devicecode = ((mBit-1)%16)+1;
        // first set off
        SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
        SerialMessage m2 = SerialMessage.getFunction(housecode, X10.FUNCTION_OFF);
        // send
        SerialTrafficController.instance().sendSerialMessage(m1, null);
        SerialTrafficController.instance().sendSerialMessage(m2, null);
        log.debug("initIntensity: sent off");
        // then set all dim
        m1 = SerialMessage.getAddress(housecode, devicecode);
        m2 = SerialMessage.getFunctionDim(housecode, X10.FUNCTION_DIM, 22);
        // send
        SerialTrafficController.instance().sendSerialMessage(m1, null);
        SerialTrafficController.instance().sendSerialMessage(m2, null);
        log.debug("initIntensity: sent dim reset");
        mDimInit = true;
    	log.debug("init done");
    }
    
    /**
     *  System-dependent instance variables
     */

    int mBit = 0;                // bit within the node

    boolean mDimInit = false;    // entering dimming mode, send init
        
    /**
     *  Request from superclass to set the current state of this Light
     */
	protected void doNewState(int oldState, int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("doNewState(" +oldState+","+newState+")");
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (mNode == null) {
            // node does not exist, ignore call
            return;
        }

        double newIntensity = getTargetIntensity();
    	if (newState == ON) {
    		newIntensity = getMaxIntensity();
    	} else if (newState == OFF) {
    		newIntensity = getMinIntensity();
    	} else {
    	    // really should not happen
    	    throw new IllegalArgumentException("invalid state request "+newState);
        }
    	
        if ( newIntensity!=1.0 && newIntensity!=0.0) {
            // dim to value
        	updateIntensity(newIntensity);
        } else {
            // just go straight
	        sendOnOffCommand(newState);
        }
	
    }

    /**
     * Request from superclass to set intensity above max
     */
    protected void updateIntensityHigh(double intensity) {
        updateIntensity(intensity);
        super.updateIntensityHigh(intensity);
    }

    /**
     * Request from superclass to set intensity between min and max
     */
    protected void updateIntensityIntermediate(double intensity) {
        updateIntensity(intensity);
        super.updateIntensityIntermediate(intensity);
    }

    /**
     * Request from superclass to set intensity below min
     */
    protected void updateIntensityLow(double intensity) {
        updateIntensity(intensity);
        super.updateIntensityLow(intensity);
    }

    /**
     * Send a Dim/Bright commands to the hardware
     */
    private void updateIntensity(double intensity) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendDimCommand(" + intensity + ")");
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (mNode == null) {
            // node does not exist, ignore call
            return;
        }
        
        if (!mDimInit) initIntensity();
        
        // figure out the function code
        int function;
        if (intensity >= getCurrentIntensity()) {
            function = X10.FUNCTION_BRIGHT;
        	log.debug("function bright");
        }
        else if (intensity < getCurrentIntensity()) {
            function = X10.FUNCTION_DIM;
        	log.debug("function dim");
        }
        else {
            log.warn("illegal state requested for Light: " + getSystemName());
            return;
        }
        int housecode = ((mBit-1)/16)+1;
        int devicecode = ((mBit-1)%16)+1;
        double diffDim = intensity - getCurrentIntensity();
        int deltaDim = (int)(22 * Math.abs(diffDim));
        if (deltaDim != 0) {
	        // address message, then function
	        SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
	        SerialMessage m2 = SerialMessage.getFunctionDim(housecode, function, deltaDim);
	        // send
	        SerialTrafficController.instance().sendSerialMessage(m1, null);
	        SerialTrafficController.instance().sendSerialMessage(m2, null);
        }
    	if (log.isDebugEnabled()) {
    		log.debug("sendDimCommand(" + intensity + ") house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
        }
    }

    /**
     *  Send a On/Off Command to the hardware
     */
    private void sendOnOffCommand(int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newState + ")\nCurrent: " + mState);
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (mNode == null) {
            // node does not exist, ignore call
            return;
        }

        // force reinit at next dim operation
        mDimInit = false;

        // figure out command 
        int function;
        double newDim;
        if (newState == ON) {
        	function = X10.FUNCTION_ON;
        	newDim = 1;
        }
        else if (newState==OFF) {
        	function = X10.FUNCTION_OFF;
        	newDim = 0;
        }
        else {
            log.warn("illegal state requested for Light: "+getSystemName());
            return;
        }

        int housecode = ((mBit-1)/16)+1;
        int devicecode = ((mBit-1)%16)+1;
        log.debug("set state "+newState+" house "+housecode+" device "+devicecode);
        // address message, then content
        SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
        SerialMessage m2 = SerialMessage.getFunction(housecode, function);
        // send
        SerialTrafficController.instance().sendSerialMessage(m1, null);
        SerialTrafficController.instance().sendSerialMessage(m2, null);
        
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newDim + ")  house " + housecode + " device " + devicecode + " funct: " + function);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
