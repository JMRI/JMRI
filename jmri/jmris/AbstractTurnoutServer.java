//AbstractTurnoutServer.java

package jmri.jmris;

import java.io.*;

import jmri.InstanceManager;
import jmri.Turnout;

/**
 * Abstract interface between the a JMRI turnout and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 1.3 $
 */

abstract public class AbstractTurnoutServer implements java.beans.PropertyChangeListener {

   public AbstractTurnoutServer(){
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(int Status) throws IOException; 
     abstract public void sendErrorStatus() throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;
	
    public void closeTurnout(java.lang.String turnoutName) {
		// load address from switchAddrTextField
		try {
			if (turnout != null)
				turnout.removePropertyChangeListener(this);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
			if (turnout == null) {
				log.error("Turnout " + turnoutName
						+ " is not available");
			} else {
				turnout.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command CLOSED");
				// and set commanded state to CLOSED
				turnout.setCommandedState(jmri.Turnout.CLOSED);
			}
		} catch (Exception ex) {
			log.error("close turnout, exception: "
							+ ex.toString());
		}
	}

        public void throwTurnout(java.lang.String turnoutName) {
		// load address from switchAddrTextField
		try {
			if (turnout != null)
				turnout.removePropertyChangeListener(this);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);

			if (turnout == null) {
				log.error("Turnout " + turnoutName
						+ " is not available");
			} else {
				turnout.addPropertyChangeListener(this);
				if (log.isDebugEnabled())
					log.debug("about to command THROWN");
				// and set commanded state to THROWN
				turnout.setCommandedState(jmri.Turnout.THROWN);
			}
		} catch (Exception ex) {
			log.error("close turnout, exception: "
							+ ex.toString());
		}
	}

    // update state as state of turnout changes
    public void propertyChange(java.beans.PropertyChangeEvent e) {
    	// If the Commanded State changes, show transition state as "<inconsistent>" 
        if (e.getPropertyName().equals("KnownState")) {
            int now = ((Integer) e.getNewValue()).intValue();
            try {
               sendStatus(now);
            } catch(java.io.IOException ie) {
                  log.error("Error Sending Status");
            }
        }
     }
    
    protected Turnout turnout = null;

    String newState = "";


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(AbstractTurnoutServer.class.getName());

}
