//AbstractTurnoutServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI turnout and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
abstract public class AbstractTurnoutServer {

    public AbstractTurnoutServer() {
        turnouts = new ArrayList<String>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String turnoutName, int Status) throws IOException;

    abstract public void sendErrorStatus(String turnoutName) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addTurnoutToList(String turnoutName) {
        if (!turnouts.contains(turnoutName)) {
            turnouts.add(turnoutName);
            InstanceManager.turnoutManagerInstance().getTurnout(turnoutName)
                    .addPropertyChangeListener(new TurnoutListener(turnoutName));
        }
    }

    synchronized protected void removeTurnoutFromList(String turnoutName) {
        if (turnouts.contains(turnoutName)) {
            turnouts.remove(turnoutName);
        }
    }

    public Turnout initTurnout(String turnoutName) {
        Turnout turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
        this.addTurnoutToList(turnoutName);
        return turnout;
    }

    public void closeTurnout(String turnoutName) {
        // load address from switchAddrTextField
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            addTurnoutToList(turnoutName);
            if (turnout == null) {
                log.error("Turnout {} is not available", turnoutName);
            } else {
                log.debug("about to command CLOSED");
                // and set commanded state to CLOSED
                turnout.setCommandedState(Turnout.CLOSED);
            }
        } catch (Exception ex) {
            log.error("Error closing turnout", ex);
        }
    }

    public void throwTurnout(String turnoutName) {
        // load address from switchAddrTextField
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            addTurnoutToList(turnoutName);

            if (turnout == null) {
                log.error("Turnout {} is not available", turnoutName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("about to command THROWN");
                }
                // and set commanded state to THROWN
                turnout.setCommandedState(Turnout.THROWN);
            }
        } catch (Exception ex) {
            log.error("Error throwing turnout", ex);
        }
    }

    class TurnoutListener implements PropertyChangeListener {

        TurnoutListener(String turnoutName) {
            name = turnoutName;
            turnout = InstanceManager.turnoutManagerInstance().provideTurnout(turnoutName);
        }

        // update state as state of turnout changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                try {
                    sendStatus(name, now);
                } catch (IOException ie) {
                    log.debug("Error Sending Status");
                    // if we get an error, de-register
                    turnout.removePropertyChangeListener(this);
                    removeTurnoutFromList(name);
                }
            }
        }
        String name = null;
        Turnout turnout = null;
    }
    protected ArrayList<String> turnouts = null;
    String newState = "";
    static Logger log = LoggerFactory.getLogger(AbstractTurnoutServer.class.getName());
}
