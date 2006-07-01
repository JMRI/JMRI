/**
 * XNetPowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 1.4 $
 */

package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

public class XNetPowerManager implements PowerManager, XNetListener {

	public XNetPowerManager() {
		// connect to the TrafficManager
		tc = XNetTrafficController.instance();
		tc.addXNetListener(~0, this);
	}

	int power = UNKNOWN;

	public void setPower(int v) throws JmriException {
		power = UNKNOWN;
		checkTC();
		if (v==ON) {
			// send RESUME_OPS
			XNetMessage l = new XNetMessage(3);
			l.setElement(0,XNetConstants.CS_REQUEST);
			l.setElement(1,XNetConstants.RESUME_OPS);
			tc.sendXNetMessage(l, this);
		} else if (v==OFF) {
			// send EMERGENCY_OFF
			XNetMessage l = new XNetMessage(3);
			l.setElement(0,XNetConstants.CS_REQUEST);
			l.setElement(1,XNetConstants.EMERGENCY_OFF);
			tc.sendXNetMessage(l, this);
		}
		firePropertyChange("Power", null, null);
	}

	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeXNetListener(~0, this);
		tc = null;
	}

	private void checkTC() throws JmriException {
		if (tc == null) throw new JmriException("attempt to use PowerManager after dispose");
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

	XNetTrafficController tc = null;

	// to listen for status changes from net
	public void message(XNetMessage m) {
		if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_INFO &&
                    m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_NORMAL_OPERATIONS) {
			power = ON;
			firePropertyChange("Power", null, null);
		}
		else if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_INFO ||
                         m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_BUSY &&
                         m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_EVERYTHING_OFF) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
	}

}


/* @(#)XNetPowerManager.java */
