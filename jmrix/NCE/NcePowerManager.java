/** 
 * NcePowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @version			
 */

package jmri.jmrix.nce;

import jmri.JmriException;
import jmri.PowerManager;

import java.beans.PropertyChangeListener;

public class NcePowerManager implements PowerManager, NceListener {

	public NcePowerManager() {
		// connect to the TrafficManager
		tc = NceTrafficController.instance();
		tc.addNceListener(~0, this);
	}

	int power = UNKNOWN;
	
	public void setPower(int v) {
		power = v;
		if (v==ON) {
			// send GPON
			NceMessage l = new NceMessage(2);
			// load constants
			tc.sendNceMessage(l);
		} else if (v==OFF) {
			// send GPOFF
			NceMessage l = new NceMessage(2);
			//load contents
			tc.sendNceMessage(l);
		}
	}
	
	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeNceListener(~0, this);
		tc = null;
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
	
	NceTrafficController tc = null;

	// to listen for status changes from NCE system
	public void message(NceMessage m) {
		if (m.getOpCode() == 000) {  
			power = ON;
			firePropertyChange("PowerOn", null, null);
		}
		else if (m.getOpCode() == 000) {
			power = OFF;
			firePropertyChange("PowerOn", null, null);
		}
	}
	
}


/* @(#)NcePowerManager.java */
