// SpecificLight.java

package jmri.jmrix.powerline.insteon2412s;

import jmri.jmrix.powerline.*;

/**
 * Implementation of the Light Object for Insteon 2412S interfaces.
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
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008, 2009
 * @author      Ken Cameron Copyright (C) 2009, 2010
 * @version     $Revision: 1.4 $
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
     * Loaded from SerialTrafficController.getNumberOfIntensitySteps();
     */
     int maxDimStep = 0;
     
    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName) {
        super(systemName);
        maxDimStep = SerialTrafficController.instance().getNumberOfIntensitySteps();
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in SerialLightManager
     */
    public SpecificLight(String systemName, String userName) {
        super(systemName, userName);
        maxDimStep = SerialTrafficController.instance().getNumberOfIntensitySteps();
    }

    /**
     * Optionally, force control to a known "dim count".
     * <p>
     * Invoked the first time intensity is set.
     * @param intensity The next intensity value that will be set
     */
    protected void initIntensity(double intensity) {
    	if (log.isDebugEnabled()) {
            log.debug("initIntensity("+intensity+")");
    	}
        
        maxDimStep = SerialTrafficController.instance().getNumberOfIntensitySteps();

        // Set initial state
            
        // see if going to stabilize at on or off
        if (intensity <= 0.5) {
            if (!isInsteon) {
                // going to low, send a real off
                X10Sequence out3 = new X10Sequence();
                out3.addAddress(housecode, devicecode);
                out3.addFunction(housecode, X10Sequence.FUNCTION_OFF, 0);
                SerialTrafficController.instance().sendX10Sequence(out3, null);
                // going to low, send max dim count low
                X10Sequence out2 = new X10Sequence();
                out2.addAddress(housecode, devicecode);
                out2.addFunction(housecode, X10Sequence.FUNCTION_DIM, maxDimStep);
                SerialTrafficController.instance().sendX10Sequence(out2, null);
            } else {
                // going to low, send a real off

                InsteonSequence out3 = new InsteonSequence();
                out3.addFunction(insteonaddress, InsteonSequence.FUNCTION_OFF, 0);
                SerialTrafficController.instance().sendInsteonSequence(out3, null);

                // going to low, send max dim count low
                InsteonSequence out2 = new InsteonSequence();
                out2.addFunction(insteonaddress, InsteonSequence.FUNCTION_DIM, maxDimStep);
                SerialTrafficController.instance().sendInsteonSequence(out2, null);
            }

            lastOutputStep = 0;
            
            if (log.isDebugEnabled()) {
            	log.debug("initIntensity: sent dim reset");
            }
        } else {
            if (!isInsteon) {
                // going to high, send a real on
                X10Sequence out3 = new X10Sequence();
                out3.addAddress(housecode, devicecode);
                out3.addFunction(housecode, X10Sequence.FUNCTION_ON, 0);
                SerialTrafficController.instance().sendX10Sequence(out3, null);
                // going to high, send max dim count high
                X10Sequence out2 = new X10Sequence();
                out2.addAddress(housecode, devicecode);
                out2.addFunction(housecode, X10Sequence.FUNCTION_BRIGHT, maxDimStep);
                // send
                SerialTrafficController.instance().sendX10Sequence(out2, null);
            } else {

                InsteonSequence out3 = new InsteonSequence();
                out3.addFunction(insteonaddress, InsteonSequence.FUNCTION_ON, 0);
                SerialTrafficController.instance().sendInsteonSequence(out3, null);

                InsteonSequence out2 = new InsteonSequence();
                out2.addFunction(insteonaddress, InsteonSequence.FUNCTION_DIM, maxDimStep);
                SerialTrafficController.instance().sendInsteonSequence(out2, null);
            }
            
            lastOutputStep = maxDimStep;
            
            if (log.isDebugEnabled()) {
            	log.debug("initIntensity: sent bright reset");
            }
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
        if (!isInsteon && lastOutputStep < 0) initIntensity(intensity);

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
            if (log.isDebugEnabled()) {
            	log.debug("intensity " + intensity + " within current step, return");
            }
            return;
        
        } else if (sendSteps > 0) {
            if (!isInsteon) {
                function = X10Sequence.FUNCTION_BRIGHT;
            } else {
                function = InsteonSequence.FUNCTION_BRIGHT;
            }
            if (log.isDebugEnabled()) {
            	log.debug("function bright");
            }
        }
        else {
            if (!isInsteon) {
                function = X10Sequence.FUNCTION_DIM;
            } else {
                function = InsteonSequence.FUNCTION_DIM;
            }
            if (log.isDebugEnabled()) {
            	log.debug("function dim");
            }
        }

        // check for errors
        if ((sendSteps <- maxDimStep) || (sendSteps > maxDimStep))
            log.error("sendSteps wrong: " + sendSteps + " intensity: " + intensity);
            
        int deltaDim = Math.abs(sendSteps);

        lastOutputStep = newStep;
        
        if (!isInsteon) {
            // create output sequence of address, then function
            X10Sequence out = new X10Sequence();
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, function, deltaDim);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);
        } else {
            // create output sequence of address, then function
            InsteonSequence out = new InsteonSequence();
            out.addFunction(insteonaddress, function, newStep);
            // send
            SerialTrafficController.instance().sendInsteonSequence(out, null);
        }

    	if (log.isDebugEnabled()) {
    		if (isInsteon) {
    			log.debug("sendIntensity(" + intensity + ") addr " + insteonaddress + "  " + newStep + " funct: " + function);
    		} else {
    			log.debug("sendIntensity(" + intensity + ") house " + X10Sequence.houseCodeToText(housecode) + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
    		}
        }
    }

    /** 
     * Number of steps from dim to bright is 
     * maintained in specific SerialTrafficController implementation
     */
    protected int getNumberOfSteps() {
        return SerialTrafficController.instance().getNumberOfIntensitySteps();
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

        if (log.isDebugEnabled()) {
        	log.debug("set state "+newState+" house "+housecode+" device "+devicecode);
        }

        if (!isInsteon) {
            // create output sequence of address, then function
            X10Sequence out = new X10Sequence();
            out.addAddress(housecode, devicecode);
            out.addFunction(housecode, function, 0);
            // send
            SerialTrafficController.instance().sendX10Sequence(out, null);
        
    	    if (log.isDebugEnabled()) {
    		    log.debug("end sendOnOff(" + newState + ")  house " + housecode + " device " + devicecode + " funct: " + function);
            }
        } else {
            // create output sequence of just address and function together
            InsteonSequence out = new InsteonSequence();
            out.addFunction(insteonaddress, function, 0);
            // send
            SerialTrafficController.instance().sendInsteonSequence(out, null);

            if (log.isDebugEnabled()) {
    		    log.debug("end sendOnOff(" + newState + ")  insteon " + insteonaddress  + " funct: " + function);
            }
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SpecificLight.class.getName());
}

/* @(#)SerialLight.java */
