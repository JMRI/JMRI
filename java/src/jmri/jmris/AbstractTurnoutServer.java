//AbstractTurnoutServer.java

package jmri.jmris;

import org.apache.log4j.Logger;
import java.io.*;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Turnout;

/**
 * Abstract interface between the a JMRI turnout and a 
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision$
 */

abstract public class AbstractTurnoutServer {

   public AbstractTurnoutServer(){
     turnouts = new ArrayList<String>();
   }

    /*
     * Protocol Specific Abstract Functions
     */

     abstract public void sendStatus(String turnoutName,int Status) throws IOException; 
     abstract public void sendErrorStatus(String turnoutName) throws IOException;
     abstract public void parseStatus(String statusString) throws jmri.JmriException,java.io.IOException;
	
    synchronized protected void addTurnoutToList(java.lang.String turnoutName) {
         if (!turnouts.contains(turnoutName) ) {
             turnouts.add(turnoutName);
             InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName)
                     .addPropertyChangeListener(new TurnoutListener(turnoutName));
         }
    }

    synchronized protected void removeTurnoutFromList(java.lang.String turnoutName) {
         if (turnouts.contains(turnoutName) ) {
             turnouts.remove(turnoutName);
         }
    }



    public void closeTurnout(java.lang.String turnoutName) {
                Turnout turnout=null;

		// load address from switchAddrTextField
		try {
                        addTurnoutToList(turnoutName);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
			if (turnout == null) {
				log.error("Turnout " + turnoutName
						+ " is not available");
			} else {
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
                Turnout turnout=null;
		// load address from switchAddrTextField
		try {
			addTurnoutToList(turnoutName);
			turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);

			if (turnout == null) {
				log.error("Turnout " + turnoutName
						+ " is not available");
			} else {
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

    class TurnoutListener implements java.beans.PropertyChangeListener {

      TurnoutListener(String turnoutName) {
          name=turnoutName;
          turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
       }

      // update state as state of turnout changes
      public void propertyChange(java.beans.PropertyChangeEvent e) {
    	  // If the Commanded State changes, show transition state as "<inconsistent>" 
          if (e.getPropertyName().equals("KnownState")) {
              int now = ((Integer) e.getNewValue()).intValue();
              try {
                  sendStatus(name,now);
              } catch(java.io.IOException ie) {
                  log.debug("Error Sending Status");
                  // if we get an error, de-register
                  turnout.removePropertyChangeListener(this);
                  removeTurnoutFromList(name);
              }
          }
       }
    
       String name=null;
       Turnout turnout = null;

    }

    protected ArrayList<String> turnouts = null;

    String newState = "";


    static Logger log = Logger.getLogger(AbstractTurnoutServer.class.getName());

}
