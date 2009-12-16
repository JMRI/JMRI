// EcosTurnout.java

package jmri.jmrix.ecos;

import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * Implement a Turnout via Ecos communications.
 * <P>
 * This object doesn't listen to the Ecos communications.  This is because
 * it should be the only object that is sending messages for this turnout;
 * more than one Turnout object pointing to a single device is not allowed.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau (C) 2007
 * @version	$Revision: 1.5 $
 */
public class EcosTurnout extends AbstractTurnout 
                         implements EcosListener {

    final String prefix = "UT";

    int objectNumber;
    
    /**
     * Ecos turnouts use the NMRA number (0-2044) as their numerical identification
     * in the system name.
     */
    public EcosTurnout(int number) {
    	super("UT"+number);
    	_number = number;
        
    	// At construction, register for messages

        //If we are to monitor what is going on with the ecos setting turnouts, we need to be able to view control of them.
        // This is now down at the setObjectNumber

    	
    }

    void setObjectNumber(int o) { 
        objectNumber = o;
        EcosTrafficController.instance().addEcosListener(this);
        EcosMessage m;
        m = new EcosMessage("request("+objectNumber+", view)");
        EcosTrafficController.instance().sendEcosMessage(m, null);
    }
    
    static String[] modeNames = null;
    static int[] modeValues = null;
        
    public int getNumber() { return _number; }
        
      
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
		if ((getFeedbackMode() != MONITORING))
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
		if ((getFeedbackMode() != MONITORING))
			return;
	
		newKnownState(state);
	}
	
	public void turnoutPushbuttonLockout(boolean b) {}
    
    /**
     * ECOS turnouts can be inverted
     */
    public boolean canInvert(){return true;}
        
    
    /**
     * Tell the layout to go to new state.
     */
    protected void sendMessage(boolean closed) {
        EcosMessage m;
        // get control
        m = new EcosMessage("request("+objectNumber+", control)");
        EcosTrafficController.instance().sendEcosMessage(m, null);
        // set state
        m = new EcosMessage("set("+objectNumber+", state["+(closed?"0":"1")+"])");
        EcosTrafficController.instance().sendEcosMessage(m, null);
        // release control
        m = new EcosMessage("release("+objectNumber+", control)");
        EcosTrafficController.instance().sendEcosMessage(m, null);
    }
    
    //Think that this might want to be changed so that is checks for <END 0 (OK)>
    
    // to listen for status changes from Ecos system
    public void reply(EcosReply m) {
        // turnout message?
        String msg = m.toString();
        if (!msg.contains("<END 0 (OK)>")) return; //The result is not valid therefore we can not set it.
        if (msg.startsWith("<REPLY get("+objectNumber+",") || msg.startsWith("<EVENT "+objectNumber+">")) {
            int start = msg.indexOf("state[");
            int end = msg.indexOf("]");
            if (start>0 && end >0) {
                int newstate = UNKNOWN;
                String val = msg.substring(start+6, end);
                if (val.equals("0")) 
                    newstate = CLOSED;
                else if (val.equals("1"))
                    newstate = THROWN;
                else log.warn("val |"+val+"| from "+msg);
                // now set state
                log.debug("see new state "+newstate+" for "+_number);
                newCommandedState(newstate);
            }
        }
    }

    public void message(EcosMessage m) {
        // messages are ignored
    }

    
 
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosTurnout.class.getName());
}

/* @(#)EcosTurnout.java */


