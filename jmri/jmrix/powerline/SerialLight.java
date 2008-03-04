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
 * the value is 0.0 or 1.0, in which case it uses on/off commands only.
 * <p>
 * Since the dim/bright step of the hardware is unknown then the Light
 * object is first create, the first time the intensity (not state)
 * is set, the lamp is run to it's maximum dim or bright step so
 * that we know the count is right.
 * <p>
 * Keeps track of the controller's "dim count", and if 
 * not certain forces it to zero to be sure.
 * <p>
 * 
 *
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version     $Revision: 1.10 $
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
     * Invoked from constructors to set up details.
     * Note: most instance variables are in AbstractLight and
     * AbstractVariableLight base classes.
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
    
    /**
     * Force control to a known "dim count".
     * Invoked
     * the first time intensity is set.
     */
    private void initIntensity(double intensity) {
        // Set initial state
        // address message, then function
        int housecode = ((mBit-1)/16)+1;
        int devicecode = ((mBit-1)%16)+1;
            
        // see if going to stabilize at on or off
        if (intensity<= 0.5) {
            // going to low, first set off
            SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
            SerialMessage m2 = SerialMessage.getFunction(housecode, X10.FUNCTION_OFF);
            // send
            SerialTrafficController.instance().sendSerialMessage(m1, null);
            SerialTrafficController.instance().sendSerialMessage(m2, null);
            log.debug("initIntensity: sent off");
            // then set to full dim
            m1 = SerialMessage.getAddress(housecode, devicecode);
            m2 = SerialMessage.getFunctionDim(housecode, X10.FUNCTION_DIM, 22);
            // send
            SerialTrafficController.instance().sendSerialMessage(m1, null);
            SerialTrafficController.instance().sendSerialMessage(m2, null);
            
            lastOutputStep = 0;
            
            log.debug("initIntensity: sent dim reset");
        } else {
            // going to high, first set on
            SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
            SerialMessage m2 = SerialMessage.getFunction(housecode, X10.FUNCTION_ON);
            // send
            SerialTrafficController.instance().sendSerialMessage(m1, null);
            SerialTrafficController.instance().sendSerialMessage(m2, null);
            log.debug("initIntensity: sent on");
            // then set to full dim
            m1 = SerialMessage.getAddress(housecode, devicecode);
            m2 = SerialMessage.getFunctionDim(housecode, X10.FUNCTION_BRIGHT, 22);
            // send
            SerialTrafficController.instance().sendSerialMessage(m1, null);
            SerialTrafficController.instance().sendSerialMessage(m2, null);
            
            lastOutputStep = 22;
            
            log.debug("initIntensity: sent bright reset");
        }
    }
    
    /**
     *  System-dependent instance variables
     */

    int mBit = 0;                // bit within the node

    // current output step 0 to 22
    int lastOutputStep = -1;  // -1 means unknown
    
    /**
     *  Request from superclass to set the current state of this Light.
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

        if ( newIntensity!=1.0 && newIntensity!=0.0) {
            // dim to value not at end of range
        	updateIntensity(newIntensity);
        } else {
            // go to full on or full off
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
     * Send a Dim/Bright commands to the X10 hardware 
     * to reach a specific intensity.
     */
    private void updateIntensity(double intensity) {
        
    	if (log.isDebugEnabled()) {
    		log.debug("updateIntensity(" + intensity + ")");
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(getSystemName());
        if (mNode == null) {
            // node does not exist, ignore call
            return;
        }
        
        // if we don't know the dim count, force it to a value.
        if (lastOutputStep < 0) initIntensity(intensity);

        // find the new correct dim count
        int newStep = (int)Math.round(intensity*22.);  // 22 is full on, 0 is full off, etc
        
        // check for errors
        if (newStep <0 || newStep>22)
            log.error("newStep wrong: "+newStep+" intensity: "+intensity);

        // find the number to send
        int sendSteps = newStep-lastOutputStep; // + for bright, - for dim
        
        // figure out the function code
        int function;
        if (sendSteps == 0) {
            // nothing to do!
            log.debug("intensity "+intensity+" within current step, return");
            return;
        
        } else if (sendSteps >0) {
            function = X10.FUNCTION_BRIGHT;
        	log.debug("function bright");
        }
        else {
            function = X10.FUNCTION_DIM;
        	log.debug("function dim");
        }

        // check for errors
        if (sendSteps <-22 || sendSteps>22)
            log.error("sendSteps wrong: "+sendSteps+" intensity: "+intensity);
            
        int housecode = ((mBit-1)/16)+1;
        int devicecode = ((mBit-1)%16)+1;
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        // address message, then function
        SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
        SerialMessage m2 = SerialMessage.getFunctionDim(housecode, function, deltaDim);
        // send
        SerialTrafficController.instance().sendSerialMessage(m1, null);
        SerialTrafficController.instance().sendSerialMessage(m2, null);

    	if (log.isDebugEnabled()) {
    		log.debug("updateIntensity(" + intensity + ") house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
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
