/**
 * SprogTurnoutManager.java
 *
 * Description:		Implement turnout manager for Sprog systems
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Id: SprogTurnoutManager.java,v 1.1 2003-01-27 05:24:00 jacobsen Exp $
 */

// System names are "ETnnn", where nnn is the turnout number without padding.

package jmri.jmrix.sprog;

import jmri.JmriException;
import jmri.Turnout;

public class SprogTurnoutManager extends jmri.AbstractTurnoutManager {

	// ABC implementations

	// to free resources when no longer used
	public void dispose() throws JmriException {
		super.dispose();
	}

	// Sprog-specific methods

	public void putBySystemName(SprogTurnout t) {
		String system = "ET"+t.getNumber();
		_tsys.put(system, t);
	}

	public Turnout newTurnout(String systemName, String userName) {
		// if system name is null, supply one from the number in userName
		if (systemName == null) systemName = "ET"+userName;

		// return existing if there is one
		Turnout t;
		if ( (userName != null) && ((t = getByUserName(userName)) != null)) return t;
		if ( (t = getBySystemName(systemName)) != null) return t;

		// get number from name
		if (!systemName.startsWith("ET")) {
			log.error("Invalid system name for Sprog turnout: "+systemName);
			return null;
		}
		int addr = Integer.valueOf(systemName.substring(2)).intValue();
		t = new SprogTurnout(addr);
		t.setUserName(userName);

		_tsys.put(systemName, t);
		if (userName!=null) _tuser.put(userName, t);
		t.addPropertyChangeListener(this);

		return t;
	}

	public SprogTurnoutManager() {
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTurnoutManager.class.getName());

}


/* @(#)SprogTurnoutManager.java */
