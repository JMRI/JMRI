// SRCPPowerManager.java

package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public class SRCPPowerManager implements PowerManager, SRCPListener {

	public SRCPPowerManager() {
		// connect to the TrafficManager
		tc = SRCPTrafficController.instance();
		tc.addSRCPListener(this);
	}

    public String getUserName() { return "SRCP"; }

	int power = UNKNOWN;

	boolean waiting = false;
	int onReply = UNKNOWN;

	public void setPower(int v) throws JmriException {
		power = UNKNOWN; // while waiting for reply
		checkTC();
		if (v==ON) {
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.ON;
			// send "Enable main track"
			SRCPMessage l = SRCPMessage.getEnableMain();
			tc.sendSRCPMessage(l, this);
		} else if (v==OFF) {
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.OFF;
			firePropertyChange("Power", null, null);
			// send "Kill main track"
			SRCPMessage l = SRCPMessage.getKillMain();
			tc.sendSRCPMessage(l, this);
		}
		firePropertyChange("Power", null, null);
	}

	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeSRCPListener(this);
		tc = null;
	}

	private void checkTC() throws JmriException {
		if (tc == null) throw new JmriException("attempt to use SRCPPowerManager after dispose");
	}

	// to hear of changes
	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);
	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
		}
	protected void firePropertyChange(String p, Object old, Object n) { pcs.firePropertyChange(p,old,n);}
	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
		}

	SRCPTrafficController tc = null;

	// to listen for status changes from SRCP system
	public void reply(SRCPReply m) {
		if (waiting) {
			power = onReply;
			firePropertyChange("Power", null, null);
		}
		waiting = false;
	}

	// to listen for status changes from SRCP system
	public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
            if(log.isDebugEnabled())
               log.debug("reply called with simpleNode " + n.jjtGetValue());
            reply(new SRCPReply(n));
        }

	public void message(SRCPMessage m) {
		if (m.isKillMain() ) {
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.OFF;
		} else if (m.isEnableMain()) {
			// configure to wait for reply
			waiting = true;
			onReply = PowerManager.ON;
		}
	}


        static Logger log = LoggerFactory.getLogger(SRCPPowerManager.class.getName());

}


/* @(#)SRCPPowerManager.java */
