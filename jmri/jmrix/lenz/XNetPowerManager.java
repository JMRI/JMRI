/**
 * XNetPowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision: 2.1 $
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

	// to listen for Broadcast messages related to track power.
        // There are 4 messages to listen for
	public void message(XNetReply m) {
		if (log.isDebugEnabled()) log.debug("Message recieved: " +m.toString());
                // First, we check for a "normal operations resumed message"
                // This indicates the power to the track is ON
		if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_INFO &&
                    m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_NORMAL_OPERATIONS) {
			power = ON;
			firePropertyChange("Power", null, null);
		}
                // Next, we check for a Track Power Off message
                // This indicates the power to the track is OFF
		else if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_INFO &&
                         m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_EVERYTHING_OFF) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
                // Then, we check for an "Emergency Stop" message
                // This indicates the track power is ON, but all 
                // locomotives are stopped
		else if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.BC_EMERGENCY_STOP &&
                         m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_EVERYTHING_OFF) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
	}

        // listen for the messages to the LI100/LI101
        public void message(XNetMessage l) {
        }


	// Initialize logging information
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetPowerManager.class.getName());

}


/* @(#)XNetPowerManager.java */
