package jmri.jmrix.acela;

import jmri.Turnout;
import jmri.implementation.AbstractTurnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AcelaLight.java
 *
 * Implementation of the Light Object for Acela
 * <P>
 * Based in part on SerialTurnout.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 *
 * @author	Bob Coleman Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaTurnout extends AbstractTurnout {

    final String prefix = "AT";

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in AcelaLightManager
     */
    public AcelaTurnout(String systemName) {
        super(systemName);
        initializeTurnout(systemName);
    }

    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in AcelaLightManager
     */
    public AcelaTurnout(String systemName, String userName) {
        super(systemName, userName);
        initializeTurnout(systemName);
    }

// Added to get rid of errors.
    /**
     * State value indicating output intensity is at or above maxIntensity
     */
//    public static final int ON          = 0x01;
    /**
     * State value indicating output intensity is at or below minIntensity
     */
//    public static final int OFF         = 0x00;
    /**
     * Sets up system dependent instance variables and sets system independent
     * instance variables to default values Note: most instance variables are in
     * AbstractLight.java
     */
    private void initializeTurnout(String systemName) {
        // Save system name
        mSystemName = systemName;

        // Extract the Bit from the name
        mBit = AcelaAddress.getBitFromSystemName(systemName);

        // Set initial state
//        setState( OFF );
        setState(UNKNOWN);
        // Set defaults for all other instance variables
/*
         setControlType( NO_CONTROL );
         setControlSensor( null );
         setControlSensorSense(Sensor.ACTIVE);
         setFastClockControlSchedule( 0,0,0,0 );
         setControlTurnout( null );
         setControlTurnoutState( Turnout.CLOSED );
         */
    }

    /**
     * System dependent instance variables
     */
    String mSystemName = "";     // system name 
//    protected int mState = OFF;  // current state of this light
    protected int mState = UNKNOWN;  // current state of this turnout
    int mBit = -1;                // global address from 0

    /**
     * Return the current state of this Light
     */
//  public int getState() { return mState; }
    /**
     * Set the current state of this Light This routine requests the hardware to
     * change. If this is really a change in state of this bit (tested in
     * AcelaNode), a Transmit packet will be sent before this Node is next
     * polled.
     */
    /*
     public void setState(int newState) {
     AcelaNode mNode = AcelaAddress.getNodeFromSystemName(mSystemName);

     if (mNode!=null) {
     if (newState==ON) {
     mNode.setOutputBit(mBit,true);
     } else if (newState==OFF) {
     mNode.setOutputBit(mBit,false);
     } else {
     log.warn("illegal state requested for Turnout: "+getSystemName());
     }
     }
	
     if (newState!=mState) {
     int oldState = mState;
     mState = newState;
            
     // notify listeners, if any
     firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
     }
     }
     */
    // Handle a request to change state by sending a turnout command
    protected void forwardCommandChangeToLayout(int s) {
        if ((s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ((s & Turnout.THROWN) != 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN " + s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true ^ getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false ^ getInverted());
        }
    }

    /**
     * Send a message to the layout to lock or unlock the turnout pushbuttons if
     * true, pushbutton lockout enabled
     */
    protected void turnoutPushbuttonLockout(boolean pushButtonLockout) {
        // Acela turnouts do not currently support lockout
/*
         if (log.isDebugEnabled())
         log.debug("Send command to "
         + (pushButtonLockout ? "Lock" : "Unlock")
         + " Pushbutton NT" + _number);
		
         byte[] bl = PushbuttonPacket.pushbuttonPkt(prefix, _number, pushButtonLockout);
         AcelaMessage m = AcelaMessage.sendPacketMessage(bl);
         AcelaTrafficController.instance().sendAcelaMessage(m, null);
         */
    }

    // Acela turnouts do support inversion
    public boolean canInvert() {
        return true;
    }

    //method which takes a turnout state as a parameter and adjusts it  as necessary
    //to reflect the turnout invert property
    private int adjustStateForInversion(int rawState) {

        if (getInverted() && (rawState == CLOSED || rawState == THROWN)) {
            if (rawState == CLOSED) {
                return THROWN;
            } else {
                return CLOSED;
            }
        } else {
            return rawState;
        }
    }

    protected void sendMessage(boolean closed) {
        int newState;
        if (closed) {
//            newState = adjustStateForInversion(ON);
            newState = adjustStateForInversion(CLOSED);
        } else {
//            newState = adjustStateForInversion(OFF);
            newState = adjustStateForInversion(THROWN);
        }

        AcelaNode mNode = AcelaAddress.getNodeFromSystemName(mSystemName);

        if (mNode != null) {
//            if (newState==ON) {
            if (newState == THROWN) {
                mNode.setOutputBit(mBit, true);
//            } else if (newState==OFF) {
            } else if (newState == CLOSED) {
                mNode.setOutputBit(mBit, false);
            } else {
                log.warn("illegal state requested for Turnout: " + getSystemName());
            }
        }

        if (newState != mState) {
            int oldState = mState;
            mState = newState;

            // notify listeners, if any
            firePropertyChange("KnownState", Integer.valueOf(oldState), Integer.valueOf(newState));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnout.class.getName());
}
