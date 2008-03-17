// XNetLight.java

package jmri.jmrix.lenz;

import jmri.AbstractLight;
import jmri.Sensor;
import jmri.Turnout;
// import jmri.Light;

/**
 * XNetLight.java
 *
 * Implementation of the Light Object for XPressNet
 * NOTE: This is a simplification of the XNetTurnout class.
 * <P>
 *  Based in part on SerialLight.java
 *
 * @author      Paul Bender Copyright (C) 2008
 * @version     $Revision: 1.1 $
 */
public class XNetLight extends AbstractLight implements XNetListener {

    /**
     * Create a Light object, with only system name.
     * <P>
     * 'systemName' was previously validated in LnLightManager
     */
    public XNetLight(String systemName) {
        super(systemName);
        // Initialize the Light
        initializeLight(systemName);
    }
    /**
     * Create a Light object, with both system and user names.
     * <P>
     * 'systemName' was previously validated in XNetLightManager
     */
    public XNetLight(String systemName, String userName) {
        super(systemName, userName);
        // Initialize the Light
        initializeLight(systemName);
    }

   /*
    *  Initilize the light object's parameters
    */
    private void initializeLight(String systemName) {
        // Save system name
        mSystemName = systemName;
        // Extract the Bit from the name
        mAddress = XNetLightManager.instance().getBitFromSystemName(systemName);
        // Set initial state
        setState( OFF );
        // Set defaults for all other instance variables
        setControlType( NO_CONTROL );
        setControlSensor( null );
        setControlSensorSense(Sensor.ACTIVE);
        setFastClockControlSchedule( 0,0,0,0 );
        setControlTurnout( null );
        setControlTurnoutState( Turnout.CLOSED );
        // At construction, register for messages
        XNetTrafficController.instance().addXNetListener(XNetInterface.FEEDBACK|XNetInterface.COMMINFO|XNetInterface.CS_INFO, this);

   }
        
    /**
     * Sets up system dependent instance variables and sets system
     *    independent instance variables to default values
     * Note: most instance variables are in AbstractLight.java
     */

    /**
     *  System dependent instance variables
     */
    String mSystemName = "";     // system name 
    protected int mState = OFF;  // current state of this light
    private int mOldState =mState; // save the old state
    int mAddress = 0;            // accessory output address

    /* Internal State Machine states. */

    static final int OFFSENT = 1;
    static final int COMMANDSENT = 2;
    static final int IDLE = 0;
    private int InternalState = IDLE;

    /**
     *  Return the current state of this Light
     */
    public int getState() { return mState; }

    /**
     *  Set the current state of this Light
     *     This routine requests the hardware to change.
     */
    public void setState(int newState) {
        if(newState!=ON && newState!=OFF) {
	   // Unsuported state
           log.warn("Unsupported state " +newState + " requested for light " +mSystemName);
	   return;
        }

        // find the command station
        LenzCommandStation cs = XNetTrafficController.instance().getCommandStation();
        // get the right packet
        XNetMessage msg = XNetMessage.getTurnoutCommandMsg(mAddress,
                                                  (newState & ON)!=0,
                                                  (newState & OFF)!=0,
                                                  true);
        InternalState=COMMANDSENT;         
        XNetTrafficController.instance().sendXNetMessage(msg, this);

        if (newState!=mState) {
                int oldState = mState;
                mState = newState;
            // notify listeners, if any
            firePropertyChange("KnownState", new Integer(oldState), new Integer(newState));
	}
        sendOffMessage();
    }

    /*
     *  Handle an incoming message from the XPressNet
     *  NOTE: We aren't registered as a listener, so This is only triggered 
     *  when we send out a message
     */
    synchronized public void message(XNetReply l) {
        if(log.isDebugEnabled()) log.debug("recieved message: " +l);  
        if(InternalState==OFFSENT) {
          // If an OFF was sent, we want to check for Communications
          // errors before we try to do anything else.
          if(l.isCommErrorMessage()) {
            /* this is a communications error */
            log.error("Communications error occured - message recieved was: " + 
l);
            sendOffMessage();
            return;
          } else  if(l.isCSBusyMessage()) {
            /* this is a communications error */
            log.error("Command station busy - message recieved was: " + l);
            sendOffMessage();
            return;
          } else if(l.isOkMessage()) {
            /* the command was successfully recieved */
            synchronized(this) {
               mOldState=mState;
               InternalState=IDLE;
            }
            return;
        } else if(InternalState==COMMANDSENT) {
          // If command was sent,, we want to check for Communications
          // errors before we try to do anything else.
          if(l.isCommErrorMessage()) {
            /* this is a communications error */
            log.error("Communications error occured - message recieved was: " + 
l);
            setState(mState);
            return;
          } else  if(l.isCSBusyMessage()) {
            /* this is a communications error */
            log.error("Command station busy - message recieved was: " + l);
            setState(mState);
            return;
          } else if(l.isOkMessage()) {
            /* the command was successfully recieved */
            sendOffMessage();
            }
            return;
          } 
        }
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }

    /* Send an "Off" message to the decoder for this output  */
    private synchronized void sendOffMessage() {
            // We need to tell the turnout to shut off the output.
            if(log.isDebugEnabled()) log.debug("Sending off message for light " + mAddress + " commanded state= " +mState);
            XNetMessage msg =  XNetMessage.getTurnoutCommandMsg(mAddress,
                                                  mState==ON,
                                                  mState==OFF,
                                                  false);
            XNetTrafficController.instance().sendXNetMessage(msg, this);
            
            // Set the known state to the commanded state.
            synchronized(this) {
               mOldState=mState; 
               InternalState = OFFSENT;
            }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetLight.class.getName());
}

/* @(#)XNetLight.java */
