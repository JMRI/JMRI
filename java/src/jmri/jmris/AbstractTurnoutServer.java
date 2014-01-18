//AbstractTurnoutServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI turnout and a network connection
 *
 * @author Paul Bender Copyright (C) 2010-2013
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
 */
abstract public class AbstractTurnoutServer {

    private final HashMap<String, TurnoutListener> turnouts;
    private final static Logger log = LoggerFactory.getLogger(AbstractTurnoutServer.class.getName());

    public AbstractTurnoutServer() {
        turnouts = new HashMap<String, TurnoutListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String turnoutName, int Status) throws IOException;

    abstract public void sendErrorStatus(String turnoutName) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addTurnoutToList(String turnoutName) {
        if (!turnouts.containsKey(turnoutName)) {
            turnouts.put(turnoutName, new TurnoutListener(turnoutName));
            InstanceManager.turnoutManagerInstance().getTurnout(turnoutName).addPropertyChangeListener(turnouts.get(turnoutName));
        }
    }

    synchronized protected void removeTurnoutFromList(String turnoutName) {
        if (turnouts.containsKey(turnoutName)) {
            InstanceManager.turnoutManagerInstance().getTurnout(turnoutName).removePropertyChangeListener(turnouts.get(turnoutName));
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
            if (!turnouts.containsKey(turnoutName)) {
                // enforce that initTurnout must be called before moving a
                // turnout
                sendErrorStatus(turnoutName);
                return;
            }
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            if (turnout == null) {
                log.error("Turnout {} is not available", turnoutName);
            } else {
                log.debug("about to command CLOSED");
                // and set commanded state to CLOSED
                turnout.setCommandedState(Turnout.CLOSED);
            }
        } catch (IOException ex) {
            log.error("Error closing turnout", ex);
        }
    }

    public void throwTurnout(String turnoutName) {
        // load address from switchAddrTextField
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
            if (!turnouts.containsKey(turnoutName)) {
                // enforce that initTurnout must be called before moving a
                // turnout
                sendErrorStatus(turnoutName);
                return;
            }

            if (turnout == null) {
                log.error("Turnout {} is not available", turnoutName);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("about to command THROWN");
                }
                // and set commanded state to THROWN
                turnout.setCommandedState(Turnout.THROWN);
            }
        } catch (IOException ex) {
            log.error("Error throwing turnout", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, TurnoutListener> turnout : this.turnouts.entrySet()) {
            InstanceManager.signalHeadManagerInstance().getSignalHead(turnout.getKey()).removePropertyChangeListener(turnout.getValue());
        }
        this.turnouts.clear();
    }

    protected TurnoutListener getListener(String turnoutName) {
        return new TurnoutListener(turnoutName);
    }

    protected class TurnoutListener implements PropertyChangeListener {

        protected TurnoutListener(String turnoutName) {
            name = turnoutName;
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutName);
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
}
