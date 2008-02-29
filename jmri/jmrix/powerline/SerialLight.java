// SerialLight.java

package jmri.jmrix.powerline;

import jmri.AbstractLight;
import jmri.Sensor;
import jmri.Turnout;
import java.util.Date;

/**
 * SerialLight.java
 *
 * Implementation of the Light Object
 * <P>
 *  Based in part on SerialTurnout.java
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @author      Bob Jacobsen Copyright (C) 2006, 2007, 2008
 * @version     $Revision: 1.4 $
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
        mBit = SerialAddress.getBitFromSystemName(systemName);
        // Set defaults for all other instance variables
        setControlType( NO_CONTROL );
        setControlSensor( null );
        setControlSensorSense(Sensor.ACTIVE);
        setFastClockControlSchedule( 0,0,0,0 );
        setControlTurnout( null );
        setControlTurnoutState( Turnout.CLOSED );
        setCanDim(false);
    }

    private void initDimUse() {
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
        log.debug("initDimUse: sent off");
        // then set all dim
        m1 = SerialMessage.getAddress(housecode, devicecode);
        m2 = SerialMessage.getFunctionDim(housecode, X10.FUNCTION_DIM, 22);
        // send
        SerialTrafficController.instance().sendSerialMessage(m1, null);
        SerialTrafficController.instance().sendSerialMessage(m2, null);
        log.debug("initDimUse: sent dim reset");
        mDimInit = true;
    	log.debug("turn on diminit");
    }
    
    /**
     *  System dependent instance variables
     */
    String mSystemName = "";     // system name 
    protected int mState = OFF;  // current state of this light
    int mBit = 0;                // bit within the node
    double mCurrentDim = 0;			// 
    double mRequestedDim = 0;
    Date mLastDimChange = null;
    int mDimRate = 0;
    boolean mIsDimmable = true;
    boolean mDimInit = false;
    double mMinDimValue = 0;
    double mMaxDimValue = 1;

    /**
     * set whether to dim or just on/off
     */
    public void setCanDim(boolean state) {
    	if (mIsDimmable != state) {
    		log.debug("change dimmable to " + state);
    	}
    	mIsDimmable = state;
    }
    
    /**
     * return is light is set to dim or just on/off
     */
    public boolean isCanDim() {
    	return(mIsDimmable);
    }
    /**
     *  Return the current state of this Light
     */
    public int getState() { return mState; }
    
    /**
     *  Return the last requested dim of this Light
     */
    public double getDimRequest() { return mRequestedDim; }

    /**
     *  Return the current dim of this Light, will differ from request when dim rate in effect
     */
    public double getDimCurrent() { return mCurrentDim; }

    /**
     *  This Light implementation supports dimming
     */
    public boolean isDimSupported() { return true; }

    /**
     *  Return the current dim rate of this Light
     */
    public int getDimRate() {
    	return mDimRate;
    }
    public void setDimRate(int newRate) {
		if (newRate != mDimRate) {
	    	int oldRate = mDimRate;
	    	mDimRate = newRate;
            // notify listeners, if any
            firePropertyChange("KnownDimRate", new Integer(oldRate), new Integer(newRate));
		}
    }
    public boolean hasBeenDimmed() {
    	return(mDimInit);
    }

    /**
     * sets the minimum output for a dimmed light 
     */
    public void setDimMin(double v) {
    	if (v > 1 || v < 0) {
    		return;
    	}
    	mMinDimValue = v;
    }

    /**
     * gets the minimum output for a dimmed light 
     */
    public double getDimMin() {
    	return(mMinDimValue);
    }

    /**
     * sets the minimum output for a dimmed light 
     */
    public void setDimMax(double v) {
    	if (v > 1 || v < 0) {
    		return;
    	}
    	mMaxDimValue = v;
    }

    /**
     * gets the minimum output for a dimmed light 
     */
    public double getDimMax() {
    	return(mMaxDimValue);
    }
    
    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     *     If this is really a change in state of this 
     *         bit (tested in SerialNode), a Transmit packet
     *         will be sent before this Node is next polled.
     */
    public void setState(int newState) {
    	if (log.isDebugEnabled()) {
    		log.debug("setState(" + newState + ")\nCurrent: " + mState);
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(mSystemName);
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
        if (mIsDimmable) {
        	setDimRequest(newDim);
        } else {
	        // reset the dimInit
	        if (mDimInit) {
	        	mDimInit = false;
	        	log.debug("turn off diminit");
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
	
			if (newState != mState || newDim != mRequestedDim || newDim != mCurrentDim) {
		        if (log.isDebugEnabled()) {
		        	log.debug("setState(" + newState + ")/" + mState + " function: " + function + " newDim: " + newDim + "/" + mRequestedDim);
		        }
				int oldState = mState;
				double oldDim = mRequestedDim;
				if (oldState != newState) {
					mState = newState;
		            // notify listeners, if any
		            firePropertyChange("KnownState", new Integer(oldState), new Integer(newState));
				}
				if (oldDim != newDim) {
					mCurrentDim = newDim;
					mRequestedDim = newDim;
					firePropertyChange("KnownDim", new Double(oldDim), new Double(newDim));
				}
			}
        }
    }

    /**
     *  Set the current dim of this Light
     *     This routine requests the hardware to change.
     *     If this is really a change in state of this 
     *         bit (tested in SerialNode), a Transmit packet
     *         will be sent before this Node is next polled.
     */
    public void setDimRequest(double newDim) {
    	if (log.isDebugEnabled()) {
    		log.debug("setDim(" + newDim + ") mRequestedDim: " + mRequestedDim);
    	}
        SerialNode mNode = SerialAddress.getNodeFromSystemName(mSystemName);
        if (mNode == null) {
            // node does not exist, ignore call
            return;
        }
        if (newDim < 0 || newDim > 1) {
        	return ;
        }
        if (!mIsDimmable) {
        	log.debug("setDim passing to setState since dim isn't enabled");
        	if (newDim <= 0) {
        		setState(OFF);
        	} else {
        		setState(ON);
        	}
        } else {
	        if (!mDimInit) {
	        	initDimUse();
	        }
	
	        // figure out the function code
	        int function;
	        if (newDim >= mCurrentDim) {
	            function = X10.FUNCTION_BRIGHT;
	        	log.debug("function bright");
	        }
	        else if (newDim < mCurrentDim) {
	            function = X10.FUNCTION_DIM;
	        	log.debug("function dim");
	        }
	        else {
	            log.warn("illegal state requested for Light: " + getSystemName());
	            return;
	        }
	        if (newDim < mMinDimValue) {
	        	newDim = mMinDimValue;
	        }
	        if (newDim > mMaxDimValue) {
	        	newDim = mMaxDimValue;
	        }
	        int housecode = ((mBit-1)/16)+1;
	        int devicecode = ((mBit-1)%16)+1;
	        double diffDim = newDim - mRequestedDim;
	        int deltaDim = (int)(22 * Math.abs(diffDim));
	        if (deltaDim != 0) {
		        // address message, then check status, then function
		        SerialMessage m1 = SerialMessage.getAddress(housecode, devicecode);
		        SerialMessage m2 = SerialMessage.getFunctionDim(housecode, function, deltaDim);
		        // send
		        SerialTrafficController.instance().sendSerialMessage(m1, null);
		        SerialTrafficController.instance().sendSerialMessage(m2, null);
	        }
	
			if (newDim != mRequestedDim) {
		    	if (log.isDebugEnabled()) {
		    		log.debug("setDim(" + newDim + ")/" + mRequestedDim + " house " + housecode + " device " + devicecode + " deltaDim: " + deltaDim + " funct: " + function);
		        }
				double oldDim = mRequestedDim;
				mRequestedDim = newDim;
				mCurrentDim = newDim;
				if (newDim > 0) {
					mState = ON;
				} else {
					mState = OFF;
				}
	            // notify listeners, if any
	            firePropertyChange("KnownDim", new Double(oldDim), new Double(newDim));
			}
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLight.class.getName());
}

/* @(#)SerialLight.java */
