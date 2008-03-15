// SerialLight.java

package jmri.jmrix.powerline;

import jmri.AbstractVariableLight;
import jmri.Sensor;
import jmri.Turnout;
import java.util.Date;

/**
 * Implementation of the Light Object for X10.
 * <P>
 * Uses X10 dimming commands to set intensity unless
 * the value is 0.0 or 1.0, in which case it uses on/off commands only.
 * <p>
 * Since the dim/bright step of the hardware is unknown then the Light
 * object is first created, the first time the intensity (not state)
 * is set to other than 0.0 or 1.0, 
 * the output is run to it's maximum dim or bright step so
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
 * @version     $Revision: 1.14 $
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
        // Extract the default 1-256 address from the name
        int tBit = SerialAddress.getBitFromSystemName(getSystemName());
        // Convert to the two-part X10 address
        housecode = ((tBit-1)/16)+1;
        devicecode = ((tBit-1)%16)+1;
        
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
            
        // see if going to stabilize at on or off
        if (intensity<= 0.5) {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to low, first set off
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_OFF, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_DIM, 22);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);

            lastOutputStep = 0;
            
            log.debug("initIntensity: sent dim reset");
        } else {
            // create output sequence
            X10Sequence out = new X10Sequence();
            // going to high, first set on
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, X10Sequence.FUNCTION_ON, 0);
            // then set to full dim
            out.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, 22);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);
            
            lastOutputStep = 22;
            
            log.debug("initIntensity: sent bright reset");
        }
    }
    
    // System-dependent instance variables

    /** 
     * Current output step 0 to 22.
     * <p>
     *  -1 means unknown
     */
    int lastOutputStep = -1;
    
    // data members holding the X10 address
    int housecode = -1;
    int devicecode = -1;
    
    /**
     *  Request from superclass to set the current state of this Light.
     */
	protected void doNewState(int oldState, int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("doNewState(" +oldState+","+newState+")");
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
        
        // are we doing intensity?
        if ( (intensity==0.0 || intensity==1.0) && (lastOutputStep < 0))
            return; // no, so let on/off handle this
            
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
            function = X10Sequence.FUNCTION_BRIGHT;
        	log.debug("function bright");
        }
        else {
            function = X10Sequence.FUNCTION_DIM;
        	log.debug("function dim");
        }

        // check for errors
        if (sendSteps <-22 || sendSteps>22)
            log.error("sendSteps wrong: "+sendSteps+" intensity: "+intensity);
            
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, deltaDim);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);

    	if (log.isDebugEnabled()) {
    		log.debug("updateIntensity(" + intensity + ") house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
        }
    }

    /**
     *  Send a On/Off Command to the hardware
     */
    private void sendOnOffCommand(int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newState + ") Current: " + mState);
    	}

        // figure out command 
        int function;
        double newDim;
        if (newState == ON) {
        	function = X10Sequence.FUNCTION_ON;
        	newDim = 1;
        }
        else if (newState==OFF) {
        	function = X10Sequence.FUNCTION_OFF;
        	newDim = 0;
        }
        else {
            log.warn("illegal state requested for Light: "+getSystemName());
            return;
        }

        log.debug("set state "+newState+" house "+housecode+" device "+devicecode);

        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, 0);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);
        
    	if (log.isDebugEnabled()) {
    		log.debug("sendOnOff(" + newDim + ")  house " + housecode + " device " + devicecode + " funct: " + function);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
