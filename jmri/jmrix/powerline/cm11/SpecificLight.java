// SpecificLight.java

package jmri.jmrix.powerline.cm11;

import jmri.AbstractVariableLight;
import jmri.Sensor;
import jmri.Turnout;
import jmri.jmrix.powerline.*;

import java.util.Date;

/**
 * Implementation of the Light Object for X10 CM11 interfaces.
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
 * @version     $Revision: 1.3 $
 */
public class SpecificLight extends jmri.jmrix.powerline.SerialLight {

    // System-dependent instance variables

    /** 
     * Current output step 0 to maxDimStep.
     * <p>
     *  -1 means unknown
     */
    int lastOutputStep = -1;
    
    /**
     * Largest X10 dim step number available.
     * <p>
     * Loaded from SerialTrafficController.maxX10DimStep();
     */
     int maxDimStep = 0;
     
    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName) {
        super(systemName);
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName, String userName) {
        super(systemName, userName);
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();
    }

    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     * @param intensity The next intensity value that will be set
     */
    protected void initIntensity(double intensity) {
        log.debug("initIntensity("+intensity+")");
        
        maxDimStep = SerialTrafficController.instance().maxX10DimStep();

        // Set initial state
            
        // see if going to stabilize at on or off
        if (intensity <= 0.5) {
            // going to low, send max dim count low
            X10Sequence out2 = new X10Sequence();
            out2.addAddress(housecode, devicecode);
            out2.addFunction(housecode, X10Sequence.FUNCTION_DIM, maxDimStep);
            SerialTrafficController.instance().sendX10Sequence(out2, null);

            lastOutputStep = 0;
            
            log.debug("initIntensity: sent dim reset");
        } else {
            // going to high, send max dim count high
            X10Sequence out2 = new X10Sequence();
            out2.addAddress(housecode, devicecode);
            out2.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, maxDimStep);
            // send
            SerialTrafficController.instance().sendX10Sequence(out2, null);
            
            lastOutputStep = maxDimStep;
            
            log.debug("initIntensity: sent bright reset");
        }
    }
    
    /**
     * Send a Dim/Bright commands to the X10 hardware 
     * to reach a specific intensity. Acts immediately, and 
     * changes no general state.
     *<p>
     * This sends "Dim" commands.  
     */
    protected void sendIntensity(double intensity) {
    	if (log.isDebugEnabled()) {
    		log.debug("sendIntensity(" + intensity + ")" + " lastOutputStep: " + lastOutputStep + " maxDimStep: " + maxDimStep);
    	}
                    
        // if we don't know the dim count, force it to a value.
        if (lastOutputStep < 0) initIntensity(intensity);

        // find the new correct dim count
        int newStep = (int)Math.round(intensity * maxDimStep);  // maxDimStep is full on, 0 is full off, etc
        
        // check for errors
        if ((newStep < 0) || (newStep > maxDimStep))
            log.error("newStep wrong: " + newStep + " intensity: " + intensity);

        // find the number to send
        int sendSteps = newStep - lastOutputStep; // + for bright, - for dim
        
        // figure out the function code
        int function;
        if (sendSteps == 0) {
            // nothing to do!
            log.debug("intensity " + intensity + " within current step, return");
            return;
        
        } else if (sendSteps > 0) {
            function = X10Sequence.FUNCTION_BRIGHT;
        	log.debug("function bright");
        }
        else {
            function = X10Sequence.FUNCTION_DIM;
        	log.debug("function dim");
        }

        // check for errors
        if ((sendSteps <- maxDimStep) || (sendSteps > maxDimStep))
            log.error("sendSteps wrong: " + sendSteps + " intensity: " + intensity);
            
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        // create output sequence of address, then function
        X10Sequence out = new X10Sequence();
        out.addAddress(housecode, devicecode);
        out.addFunction(housecode, function, deltaDim);
        // send
        SerialTrafficController.instance().sendX10Sequence(out, null);

    	if (log.isDebugEnabled()) {
    		log.debug("sendIntensity(" + intensity + ") house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
        }
    }

    /**
     *  Send a On/Off Command to the hardware
     */
    protected void sendOnOffCommand(int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("start sendOnOff(" + newState + ") Current: " + mState);
    	}

        // figure out command 
        int function;
        if (newState == ON) {
        	function = X10Sequence.FUNCTION_ON;
        }
        else if (newState==OFF) {
        	function = X10Sequence.FUNCTION_OFF;
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
    		log.debug("end sendOnOff(" + newState + ")  house " + housecode + " device " + devicecode + " funct: " + function);
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SpecificLight.class.getName());
}

/* @(#)SerialLight.java */
