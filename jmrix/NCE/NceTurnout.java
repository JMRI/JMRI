/** 
 * NceTurnout.java
 *
 * Description:		extend jmri.AbstractTurnout for NCE layouts
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

import jmri.AbstractTurnout;

public class NceTurnout extends AbstractTurnout implements NceListener {

	public NceTurnout(int number) {  // a human-readable turnout number must be specified!
		_number = number;
		// At construction, register for messages
		NceTrafficController.instance().addNceListener(this);	
	}

	public int getNumber() { return _number; }
	
	// Handle a request to change state by sending a LocoNet command
	protected void forwardCommandChangeToLayout(int s) throws jmri.JmriException {
	}
	
	// implementing classes will typically have a function/listener to get
	// updates from the layout, which will then call 
	//		public void firePropertyChange(String propertyName,
	//										Object oldValue,
	//										Object newValue)	 
	// _once_ if anything has changed state (or set the commanded state directly)
	public void message(NceMessage l) {
		// parse message type
					if (log.isDebugEnabled()) log.debug("SW_REQ received with valid address");
					if (true)
						newCommandedState(CLOSED);
					else
						newCommandedState(THROWN);
	}
	
	public void reply(NceReply r) {
		// not listening for any particular reply
	}
	
	// data members
	int _number;   // turnout number
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnout.class.getName());

}


/* @(#)NceTurnout.java */
