/** 
 * NceTurnoutManager.java
 *
 * Description:		Implement turnout manager for NCE systems
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */
 
// System names are "LTnnn", where nnn is the turnout number without padding.

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.Turnout;

public class NceTurnoutManager extends jmri.AbstractTurnoutManager implements NceListener {

	// ABC implementations
	
	// to free resources when no longer used
	public void dispose() throws JmriException {
	}

	// NCE-specific methods
	
	public void putByUserName(String s, NceTurnout t) {
		_tuser.put(s, t);
		// find the system name, and put that way also
		String system = "LT"+t.getNumber();
		_tsys.put(system, t);
	}

	public void putBySystemName(NceTurnout t) {
		String system = "LT"+t.getNumber();
		_tsys.put(system, t);
	}
	
	public Turnout newTurnout(String systemName, String userName) {
		// get number from name
		if (!systemName.startsWith("LT")) {
			log.error("Invalid system name for LocoNet turnout: "+systemName);
			return null;
		}
		int addr = Integer.valueOf(systemName.substring(2)).intValue();
		NceTurnout t = new NceTurnout(addr);
		
		_tsys.put(systemName, t);
		_tuser.put(userName, t);
		return t;
	}

	// ctor has to register for LocoNet events
	public NceTurnoutManager() {
		NceTrafficController.instance().addNceListener(this);	
	}
		
	// listen for turnouts, creating them as needed
	public void message(NceMessage l) {
		// parse message type
		int addr = 0;
		// reach here for NCE switch command; make sure we know about this one
		String s = "LN"+addr;
		if (null == getBySystemName(s)) {
			// need to store a new one
			NceTurnout t = new NceTurnout(addr);
			putBySystemName(t);
		}
	}

	public void reply(NceReply l) {
		// doesn't do anything, as not looking for anything from reply
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManager.class.getName());

}


/* @(#)NceTurnoutManager.java */
