// SRCPTurnout.java

package jmri.jmrix.srcp;

import org.apache.log4j.Logger;
import jmri.implementation.AbstractTurnout;
import jmri.Turnout;

/**
 * SRCP implementation of the Turnout interface.
 * <P>
 *  This object doesn't listen to the SRCP communications.  This is because
 *  it should be the only object that is sending messages for this turnout;
 *  more than one Turnout object pointing to a single device is not allowed.
 *
 * Description:		extend jmri.AbstractTurnout for SRCP layouts
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public class SRCPTurnout extends AbstractTurnout {

	/**
	 * SRCP turnouts use the NMRA number (0-511) as their numerical identification.
	 */
	public SRCPTurnout(int number) {
            super("DT"+number);
            _number = number;
            // At construction, register for messages
	}

	public int getNumber() { return _number; }

	// Handle a request to change state by sending a formatted DCC packet
	protected void forwardCommandChangeToLayout(int s) {
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
    protected void turnoutPushbuttonLockout(boolean _pushButtonLockout){
		if (log.isDebugEnabled()) log.debug("Send command to " + (_pushButtonLockout ? "Lock" : "Unlock")+ " Pushbutton ET"+_number);
    }

	// data members
	int _number;   // turnout number

	protected void sendMessage(boolean closed) {
		// get the message text
        String text;
        if (closed) 
            text = "SET 1 GA "+_number+" 0 0 -1\n";
        else // thrown
            text = "SET 1 GA "+_number+" 0 1 -1\n";
            
        // create and send the message itself
		SRCPTrafficController.instance().sendSRCPMessage(new SRCPMessage(text), null);

	}

	static Logger log = Logger.getLogger(SRCPTurnout.class.getName());

}


/* @(#)SRCPTurnout.java */
