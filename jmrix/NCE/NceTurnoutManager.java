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

public class NceTurnoutManager extends jmri.AbstractTurnoutManager {

	// ABC implementations
	
	// to free resources when no longer used
	public void dispose() throws JmriException {
		super.dispose();
	}

	// NCE-specific methods
	
	public void putBySystemName(NceTurnout t) {
		String system = "LT"+t.getNumber();
		_tsys.put(system, t);
	}
	
	public Turnout newTurnout(String systemName, String userName) {
		// return existing if there is one
		Turnout t;
		if ( (t = getByUserName(userName)) != null) return t;
		if ( (t = getBySystemName(systemName)) != null) return t;

		// get number from name
		if (!systemName.startsWith("NT")) {
			log.error("Invalid system name for NCE turnout: "+systemName);
			return null;
		}
		int addr = Integer.valueOf(systemName.substring(2)).intValue();
		t = new NceTurnout(addr);
		t.setUserName(userName);
		
		_tsys.put(systemName, t);
		_tuser.put(userName, t);
		t.addPropertyChangeListener(this);
		
		return t;
	}

	public NceTurnoutManager() {
	}
		
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceTurnoutManager.class.getName());

}


/* @(#)NceTurnoutManager.java */
