/**
 * XNetTurnoutManager.java
 *
 * Description:		Implement turnout manager
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */

// System names are "XTnnn", where nnn is the turnout number without padding.

package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.Turnout;

public class XNetTurnoutManager extends jmri.AbstractTurnoutManager implements XNetListener {

    final String prefix = "XT";

    /**
     * @return The system-specific prefix letter for a specific implementation
     */
    public char systemLetter() { return prefix.charAt(0); }

	// to free resources when no longer used
	public void dispose() throws JmriException {
	}

	// XNet-specific methods

	public void putBySystemName(XNetTurnout t) {
		String system = prefix+t.getNumber();
		_tsys.put(system, t);
	}

	public Turnout newTurnout(String systemName, String userName) {
		// if system name is null, supply one from the number in userName
		if (systemName == null) systemName = prefix+userName;

		// return existing if there is one
		Turnout t;
		if ( (userName!=null) && ((t = getByUserName(userName)) != null)) return t;
		if ( (t = getBySystemName(systemName)) != null) return t;

		// get number from name
		if (!systemName.startsWith(prefix)) {
			log.error("Invalid system name for XPressNet turnout: "+systemName);
			return null;
		}
		int addr = Integer.valueOf(systemName.substring(2)).intValue();
		t = new XNetTurnout(addr);
		t.setUserName(userName);

		_tsys.put(systemName, t);
		if (userName!=null) _tuser.put(userName, t);
		t.addPropertyChangeListener(this);

		return t;
	}

	// ctor has to register for XNet events
	public XNetTurnoutManager() {
		XNetTrafficController.instance().addXNetListener(~0, this);
	}

	// listen for turnouts, creating them as needed
	public void message(XNetMessage l) {
		// parse message type
		int addr = XNetTrafficController.instance()
                    .getCommandStation().getTurnoutMsgAddr(l);
        if (log.isDebugEnabled()) log.debug("message had address: "+addr);
        if (addr<=0)  return; // indicates no message
		// reach here for switch command; make sure we know about this one
		String s = prefix+addr;
		if (null == getBySystemName(s)) {
			// need to store a new one
			XNetTurnout t = new XNetTurnout(addr);
			putBySystemName(t);
		}
	}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutManager.class.getName());

}


/* @(#)XNetTurnoutManager.java */
