package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.server.json.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between a JMRI signal head and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
abstract public class AbstractSignalHeadServer {

    private final HashMap<String, SignalHeadListener> signalHeads;
    private static final Logger log = LoggerFactory.getLogger(AbstractSignalHeadServer.class);

    public AbstractSignalHeadServer() {
        signalHeads = new HashMap<String, SignalHeadListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String signalHead, int Status) throws IOException;

    abstract public void sendErrorStatus(String signalHead) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException, JsonException;

    synchronized protected void addSignalHeadToList(String signalHeadName) {
        if (!signalHeads.containsKey(signalHeadName)) {
            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
            if(sh!=null) {
               SignalHeadListener shl = new SignalHeadListener(signalHeadName);
               sh.addPropertyChangeListener(shl);
               signalHeads.put(signalHeadName, shl );
               log.debug("Added listener to signalHead {}", signalHeadName);
            }
        }
    }

    synchronized protected void removeSignalHeadFromList(String signalHeadName) {
        if (signalHeads.containsKey(signalHeadName)) {
            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
            if(sh!=null) {
               sh.removePropertyChangeListener(signalHeads.get(signalHeadName));
               signalHeads.remove(signalHeadName);
            }
        }
    }

    public void setSignalHeadAppearance(String signalHeadName, String signalHeadState) {
        this.setSignalHeadAppearance(signalHeadName, this.appearanceForName(signalHeadState));
    }

    protected void setSignalHeadAppearance(String signalHeadName, int signalHeadState) {
        SignalHead signalHead;
        try {
            addSignalHeadToList(signalHeadName);
            signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
            if (signalHead == null) {
                // only log, since this may be from a remote system
                log.error("SignalHead " + signalHeadName + " is not available.");
            } else {
                if (signalHead.getAppearance() != signalHeadState || signalHead.getHeld()) {
                    if (signalHeadState == SignalHead.HELD) {
                        signalHead.setHeld(true);
                    } else {
                        if (signalHead.getHeld()) signalHead.setHeld(false);
                        signalHead.setAppearance(signalHeadState);
                    }
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

    public void dispose() {
        for (Map.Entry<String, SignalHeadListener> signalHead : this.signalHeads.entrySet()) {
            SignalHead sh = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHead.getKey());
            if(sh != null) {
               sh.removePropertyChangeListener(signalHead.getValue());
            }
        }
        this.signalHeads.clear();
    }

    class SignalHeadListener implements PropertyChangeListener {

        String name = null;
        SignalHead signalHead = null;

        SignalHeadListener(String signalHeadName) {
            name = signalHeadName;
            signalHead = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(signalHeadName);
        }

        // update state as state of signalHead changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("Appearance") || e.getPropertyName().equals("Held")) {
                SignalHead sh = (SignalHead) e.getSource();
                int state = sh.getAppearance();
                if (sh.getHeld()) {
                    state = SignalHead.HELD;
                }
                try {
                    sendStatus(name, state);
                } catch (IOException ie) {
                    // if we get an error, de-register
                    if (log.isDebugEnabled()) {
                        log.debug("Unable to send status, removing listener from signalHead " + name);
                    }
                    signalHead.removePropertyChangeListener(this);
                    removeSignalHeadFromList(name);
                }
            }
        }
    }
}
