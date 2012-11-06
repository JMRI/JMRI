//AbstractSignalServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import org.apache.log4j.Logger;

/**
 * Abstract interface between the a JMRI signal head and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
abstract public class AbstractSignalHeadServer {

    protected ArrayList<String> signalHeads = null;
    protected String newState = "";
    static Logger log = Logger.getLogger(AbstractSignalHeadServer.class.getName());

    public AbstractSignalHeadServer() {
        signalHeads = new ArrayList<String>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String signalHead, int Status) throws IOException;

    abstract public void sendErrorStatus(String signalHead) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addSignalHeadToList(String signalHeadName) {
        if (!signalHeads.contains(signalHeadName)) {
            signalHeads.add(signalHeadName);
            InstanceManager.signalHeadManagerInstance().getSignalHead(signalHeadName).addPropertyChangeListener(new SignalHeadListener(signalHeadName));
        }
    }

    synchronized protected void removeSignalHeadFromList(String signalHeadName) {
        if (signalHeads.contains(signalHeadName)) {
            signalHeads.remove(signalHeadName);
        }
    }

    public void setSignalHeadAppearance(String signalHeadName, String signalHeadState) {
        this.setSignalHeadAppearance(signalHeadName, this.appearanceForName(signalHeadState));
    }

    protected void setSignalHeadAppearance(String signalHeadName, int signalHeadState) {
        SignalHead signalHead;
        try {
            addSignalHeadToList(signalHeadName);
            signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHeadName);
            if (signalHead == null) {
                // only log, since this may be from a remote system
                log.error("SignalHead " + signalHeadName + " is not available.");
            } else {
                if (signalHead.getAppearance() != signalHeadState) {
                    signalHead.setAppearance(signalHeadState);
                } else {
                    try {
                        sendStatus(signalHeadName, signalHeadState);
                    } catch (IOException ex) {
                        log.error("Error sending appearance", ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception setting signalHead " + signalHeadName + " appearance:", ex);
        }
    }

    protected String nameForAppearance(int appearance) {
        switch (appearance) {
            case SignalHead.DARK:
                return "DARK";
            case SignalHead.RED:
                return "RED";
            case SignalHead.FLASHRED:
                return "FLASHRED";
            case SignalHead.YELLOW:
                return "YELLOW";
            case SignalHead.FLASHYELLOW:
                return "FLASHYELLOW";
            case SignalHead.GREEN:
                return "GREEN";
            case SignalHead.FLASHGREEN:
                return "FLASHGREEN";
            case SignalHead.LUNAR:
                return "LUNAR";
            case SignalHead.FLASHLUNAR:
                return "FLASHLUNAR";
            case SignalHead.HELD:
                return "HELD";
            default:
                return "UNKNOWN";
        }
    }

    protected int appearanceForName(String name) {
        if (name.equals("DARK")) {
            return SignalHead.DARK;
        } else if (name.equals("RED")) {
            return SignalHead.RED;
        } else if (name.equals("FLASHRED")) {
            return SignalHead.FLASHRED;
        } else if (name.equals("YELLOW")) {
            return SignalHead.YELLOW;
        } else if (name.equals("FLASHYELLOW")) {
            return SignalHead.FLASHYELLOW;
        } else if (name.equals("GREEN")) {
            return SignalHead.GREEN;
        } else if (name.equals("FLASHGREEN")) {
            return SignalHead.FLASHGREEN;
        } else if (name.equals("LUNAR")) {
            return SignalHead.LUNAR;
        } else if (name.equals("FLASHLUNARDARK")) {
            return SignalHead.DARK;
        } else if (name.equals("FLASHLUNAR")) {
            return SignalHead.FLASHLUNAR;
        } else {
            return SignalHead.DARK;
        }
    }

    class SignalHeadListener implements PropertyChangeListener {

        String name = null;
        SignalHead signalHead = null;

        SignalHeadListener(String signalHeadName) {
            name = signalHeadName;
            signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(signalHeadName);
        }

        // update state as state of signalHead changes
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
                    signalHead.removePropertyChangeListener(this);
                    removeSignalHeadFromList(name);
                }
            }
        }
    }
}
