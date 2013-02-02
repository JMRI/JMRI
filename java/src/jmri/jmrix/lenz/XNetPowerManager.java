/**
 * XNetPowerManager.java
 *
 * Description:		PowerManager implementation for controlling layout power
 * @author			Bob Jacobsen Copyright (C) 2001
 * @author			Paul Bender Copyright (C) 2003-2010
 * @version			$Revision$
 */

package jmri.jmrix.lenz;

import org.apache.log4j.Logger;
import jmri.JmriException;
import jmri.PowerManager;

public class XNetPowerManager implements PowerManager, XNetListener {

	public XNetPowerManager(XNetSystemConnectionMemo memo) {
		// connect to the TrafficManager
		tc = memo.getXNetTrafficController();
		tc.addXNetListener(XNetInterface.CS_INFO, this);
        userName = memo.getUserName();
		// request the current command station status
		tc.sendXNetMessage(XNetMessage.getCSStatusRequestMessage(),this);
	}

    public String getUserName() { return "XPressNet"; }
    
    String userName = "XPressNet";

	int power = UNKNOWN;

	public void setPower(int v) throws JmriException {
		power = UNKNOWN;
		checkTC();
		if (v==ON) {
			// send RESUME_OPS
			tc.sendXNetMessage(XNetMessage.getResumeOperationsMsg(), this);
		} else if (v==OFF) {
			// send EMERGENCY_OFF
			tc.sendXNetMessage(XNetMessage.getEmergencyOffMsg(), this);
		}
		firePropertyChange("Power", null, null);
	}

	public int getPower() { return power;}

	// to free resources when no longer used
	public void dispose() throws JmriException {
		tc.removeXNetListener(XNetInterface.CS_INFO, this);
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
        // There are 5 messages to listen for
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
                // Next we check for a "Service Mode Entry" message
                // This indicatse track power is off on the mainline.
		else if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_INFO &&
                         m.getElement(1) == jmri.jmrix.lenz.XNetConstants.BC_SERVICE_MODE_ENTRY) {
			power = OFF;
			firePropertyChange("Power", null, null);
		}
		// Finally, we look at for the response to a Command 
                // Station Status Request
		else if (m.getElement(0) == jmri.jmrix.lenz.XNetConstants.CS_REQUEST_RESPONSE &&
                         m.getElement(1) == jmri.jmrix.lenz.XNetConstants.CS_STATUS_RESPONSE) {
                     int statusByte=m.getElement(2); 
                     if((statusByte&0x01)==0x01) {
                        // Command station is in Emergency Off Mode
			power = OFF;
			firePropertyChange("Power", null, null);
                     } else if ((statusByte&0x02)==0x02){
                        // Command station is in Emergency Stop Mode
			power = OFF;
			firePropertyChange("Power", null, null);
                     } else if ((statusByte&0x08)==0x08){
                        // Command station is in Service Mode, power to the 
                        // track is off
			power = OFF;
			firePropertyChange("Power", null, null);
                     } else if ((statusByte&0x40)==0x40){
                        // Command station is in Power Up Mode, and not yet on
			power = OFF;
			firePropertyChange("Power", null, null);
                     } else {
		        power = ON;
			firePropertyChange("Power", null, null);
		     }
              }

	}

        // listen for the messages to the LI100/LI101
        public void message(XNetMessage l) {
        }

        // Handle a timeout notification
        public void notifyTimeout(XNetMessage msg)
        {
          if(log.isDebugEnabled()) log.debug("Notified of timeout on message" + msg.toString());
        }

	// Initialize logging information
	static Logger log = Logger.getLogger(XNetPowerManager.class.getName());

}


/* @(#)XNetPowerManager.java */
