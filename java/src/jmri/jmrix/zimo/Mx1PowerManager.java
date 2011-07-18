// Mx1PowerManager.java

package jmri.jmrix.zimo;

import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			$Revision$
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1PowerManager implements PowerManager, Mx1Listener {

	public Mx1PowerManager() {
		// connect to the TrafficManager
		tc = Mx1TrafficController.instance();
		tc.addMx1Listener(~0, this);
        }

    public String getUserName() { return "Mx1"; }

	int power = UNKNOWN;

	public void setPower(int v) throws JmriException {
		power = UNKNOWN;
		checkTC();
                if (v==ON) {
                  // send GPON
                  Mx1Message m = new Mx1Message(3);
                  m.setElement(0, 0x53);
                  m.setElement(1, 0x45);
                  tc.sendMx1Message(m, this);
                  }
                else if (v==OFF) {
		  // send GPOFF
		  Mx1Message m = new Mx1Message(3);
                  m.setElement(0, 0x53);
                  m.setElement(1, 0x41);
                  tc.sendMx1Message(m, this);
                  }
                // request status
                Mx1Message m = new Mx1Message(2);
                m.setElement(0, 0x5A);
                tc.sendMx1Message(m, this);
		firePropertyChange("Power", null, null);
	}

	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeMx1Listener(~0, this);
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

	Mx1TrafficController tc = null;

        // to listen for status changes from net
	public void message(Mx1Message m) {
                if (m.getElement(0) == 0x5a) {
			if((m.getElement(2)&0x02) == 0x02) {
                        power = ON;
			firePropertyChange("Power", null, null);
                        }
                        else {
                        power = OFF;
			firePropertyChange("Power", null, null);
			}

		}
	}

}


/* @(#)Mx1PowerManager.java */
