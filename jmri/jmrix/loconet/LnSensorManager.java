// LnSensorManager.java

package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.Sensor;

/**
 * Manage the LocoNet-specific Sensor implementation.
 *
 * System names are "LSnnn", where nnn is the sensor number without padding.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class LnSensorManager extends jmri.AbstractSensorManager implements LocoNetListener {

	// ABC implementations

	// to free resources when no longer used
	public void dispose() throws JmriException {
	}

	// LocoNet-specific methods

	public Sensor newSensor(String systemName, String userName) {
		// if system name is null, supply one from the number in userName
		if (systemName == null) systemName = "LS"+userName;

		// get number from name
		if (!systemName.startsWith("LS")) {
			log.error("Invalid system name for LocoNet turnout: "+systemName);
			return null;
		}
		LnSensor s = new LnSensor(systemName);

		_tsys.put(systemName, s);
		_tuser.put(userName, s);
		return s;
	}

	// ctor has to register for LocoNet events
	public LnSensorManager() {
		LnTrafficController.instance().addLocoNetListener(~0, this);
	}

	// listen for sensors, creating them as needed
	public void message(LocoNetMessage l) {
		// parse message type
		LnSensorAddress a;
		switch (l.getOpCode()) {
        	case LnConstants.OPC_INPUT_REP: {               /* page 9 of Loconet PE */
	            int sw1 = l.getElement(1);
	            int sw2 = l.getElement(2);
				a = new LnSensorAddress(sw1, sw2);
				if (log.isDebugEnabled()) log.debug("INPUT_REP received with address "+a);
				break;
				}
			default:  // here we didn't find an interesting command
				return;
			}
		// reach here for loconet sensor input command; make sure we know about this one
		String s = a.getNumericAddress();
		if (null == getBySystemName(s)) {
			// need to store a new one
			if (log.isDebugEnabled()) log.debug("Create new LnSensor as "+s);
			newSensor(s, "");
		}
	}

	private int address(int a1, int a2) {
		// the "+ 1" in the following converts to throttle-visible numbering
		return (((a2 & 0x0f) * 128) + (a1 & 0x7f) + 1);
		}

	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManager.class.getName());

}


/* @(#)LnTurnoutManager.java */
