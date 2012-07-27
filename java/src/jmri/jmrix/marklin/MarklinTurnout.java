// MarklinTurnout.java

package jmri.jmrix.marklin;

import jmri.implementation.AbstractTurnout;
import jmri.Turnout;


/**
 * Implement a Turnout via Marklin communications.
 * <P>
 * This object doesn't listen to the Marklin communications.  This is because
 * it should be the only object that is sending messages for this turnout;
 * more than one Turnout object pointing to a single device is not allowed.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version	$Revision: 18574 $
 */
public class MarklinTurnout extends AbstractTurnout 
                         implements MarklinListener {

    String prefix;
    
    /**
     * Marklin turnouts use the NMRA number (0-2040) as their numerical identification
     * in the system name.
     * @param number address of the turnout
     */
    public MarklinTurnout(int number, String prefix, MarklinTrafficController etc) {
    	super(prefix+"T"+number);
    	_number = number;
        this.prefix = prefix;
        tc = etc;
        tc.addMarklinListener(this);
     }
    
    MarklinTrafficController tc;
      
     // Handle a request to change state by sending a turnout command
    protected void forwardCommandChangeToLayout(int s) {
        // implementing classes will typically have a function/listener to get
        // updates from the layout, which will then call
        //		public void firePropertyChange(String propertyName,
        //										Object oldValue,
        //										Object newValue)
        // _once_ if anything has changed state (or set the commanded state directly)

        // sort out states
        if ( (s & Turnout.CLOSED) > 0) {
            // first look for the double case, which we can't handle
            if ( (s & Turnout.THROWN) > 0) {
                // this is the disaster case!
                log.error("Cannot command both CLOSED and THROWN "+s);
                return;
            } else {
                // send a CLOSED command
                sendMessage(true^getInverted());
            }
        } else {
            // send a THROWN command
            sendMessage(false^getInverted());
        }
    }
    
    // data members
    int _number;   // turnout number
    
    /**
     * Set the turnout known state to reflect what's been observed
     * from the command station messages. A change there means that
     * somebody commanded a state change (e.g. somebody holding a 
     * throttle), and that command has already taken effect.
     * Hence we use "newCommandedState" to indicate it's taken place.
     * Must be followed by "newKnownState" to complete the turnout
     * action. 
     *
     * @param state Observed state, updated state from command station
     */
    synchronized void setCommandedStateFromCS(int state) {
		if ((getFeedbackMode() != DIRECT))
			return;
		
		newCommandedState(state);
	}
    
    /**
     * Set the turnout known state to reflect what's been observed
     * from the command station messages. A change there means that
     * somebody commanded a state change (e.g. somebody holding a 
     * throttle), and that command has already taken effect.
     * Hence we use "newKnownState" to indicate it's taken place.
     * <P>
     * @param state Observed state, updated state from command station
     */
    synchronized void setKnownStateFromCS(int state) {
         newCommandedState(state);
         if (getFeedbackMode()==DIRECT) newKnownState(state);
	}
	
	public void turnoutPushbuttonLockout(boolean b) {}
    
    /**
     * Marklin turnouts can be inverted
     */
    @Override
    public boolean canInvert(){return true;}
        
    
    final static int UNKNOWN = MarklinConstants.PROTOCOL_UNKNOWN;
    final static int DCC = MarklinConstants.PROTOCOL_DCC;
    final static int MM2 = MarklinConstants.PROTOCOL_MM2;
    final static int SFX = MarklinConstants.PROTOCOL_SX;
    
    int protocol = UNKNOWN;
    
    /**
     * Tell the layout to go to new state.
     * @param closed State of the turnout to be sent to the command station
     */
    protected void sendMessage(final boolean newstate) {
        MarklinMessage m = MarklinMessage.getSetTurnout(getCANAddress(), (newstate? 1:0), 0x01);
        tc.sendMarklinMessage(m, this);

         meterTimer.schedule(new java.util.TimerTask(){
            boolean state = newstate;
            public void run() {
                try {
                    sendOffMessage((state? 1:0));
                } catch (Exception e) {
                    log.error("Exception occured while sending delayed off to turnout: "+e);
                }
            }
         }, METERINTERVAL);
    }
    
    int getCANAddress(){
        switch(protocol){
            case DCC : return _number+MarklinConstants.DCCACCSTART-1;
            default : return _number+MarklinConstants.MM1ACCSTART-1;
        }
    }
        
    // to listen for status changes from Marklin system
    public void reply(MarklinReply m) {
        if(m.getPriority()==MarklinConstants.PRIO_1 && m.getCommand()>=MarklinConstants.ACCCOMMANDSTART && m.getCommand()<=MarklinConstants.ACCCOMMANDEND ){
            log.info(m.toHexString());
            if(protocol==UNKNOWN) {
                if(m.getAddress()==_number+MarklinConstants.MM1ACCSTART-1){
                    protocol=MM2;
                } else if(m.getAddress()==_number+MarklinConstants.DCCACCSTART-1){
                    protocol=DCC;
                } else {
                    //Message is not for us.
                    return;
                }
            }
            log.info("Protocol set to " + protocol);
            if(m.getAddress()==getCANAddress()){
                switch (m.getElement(9)){
                    case 0x00   : setKnownStateFromCS(Turnout.THROWN); break;
                    case 0x01   : setKnownStateFromCS(Turnout.CLOSED); break;
                    default:    log.warn("Unknown state command " + m.getElement(9));
                }
            }
        }
    }
    
    public void message(MarklinMessage m) {
        // messages are ignored
    }
    
    protected void sendOffMessage(int state){
        MarklinMessage m = MarklinMessage.getSetTurnout(getCANAddress(), state, 0x00);
        tc.sendMarklinMessage(m, this);
    }
    
    static final int METERINTERVAL = 100;  // msec wait before closed
    static java.util.Timer meterTimer = new java.util.Timer(true);

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MarklinTurnout.class.getName());
}

/* @(#)MarklinTurnout.java */

