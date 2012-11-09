//AbstractSignalMastServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import org.apache.log4j.Logger;

/**
 * Abstract interface between a JMRI signal mast and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21573 $
 */
abstract public class AbstractSignalMastServer {

    protected ArrayList<String> signalMasts = null;
    protected String newState = "";
    static Logger log = Logger.getLogger(AbstractSignalMastServer.class.getName());

    public AbstractSignalMastServer() {
        signalMasts = new ArrayList<String>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String signalMast, String Status) throws IOException;

    abstract public void sendErrorStatus(String signalMast) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addSignalMastToList(String signalMastName) {
        if (!signalMasts.contains(signalMastName)) {
            signalMasts.add(signalMastName);
            InstanceManager.signalMastManagerInstance().getSignalMast(signalMastName).addPropertyChangeListener(new SignalMastListener(signalMastName));
            if (log.isDebugEnabled()) log.debug("Added listener to signalMast " + signalMastName);
        }
    }

    synchronized protected void removeSignalMastFromList(String signalMastName) {
        if (signalMasts.contains(signalMastName)) {
            signalMasts.remove(signalMastName);
        }
    }

    protected void setSignalMastAspect(String signalMastName, String signalMastState) {
        SignalMast signalMast;
        try {
            addSignalMastToList(signalMastName);
            signalMast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMastName);
            if (signalMast == null) {
                log.error("SignalMast " + signalMastName + " is not available.");
            } else {
                if (signalMast.getAspect() != signalMastState) {
                    signalMast.setAspect(signalMastState);
                } else {
                    try {
                        sendStatus(signalMastName, signalMastState);
                    } catch (IOException ex) {
                        log.error("Error sending aspect ", ex);
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception setting signalMast " + signalMastName + " aspect:", ex);
        }
    }


    class SignalMastListener implements PropertyChangeListener {

        String name = null;
        SignalMast signalMast = null;

        SignalMastListener(String signalMastName) {
            name = signalMastName;
            signalMast = InstanceManager.signalMastManagerInstance().getSignalMast(signalMastName);
        }

        // update state as state of signalMast changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("Aspect") || e.getPropertyName().equals("Held") || e.getPropertyName().equals("Lit")) {
            	SignalMast sm = (SignalMast)e.getSource();
        		String state = sm.getAspect();
    			if ((sm.getHeld()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD)!=null)) {
    	    		state = "Held";
    			} else if ((sm.getLit()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK)!=null)) {
    	    		state = "Dark";
    			}
                try {
                    sendStatus(name, state);
                } catch (IOException ie) {
                    // if we get an error, de-register
                    if (log.isDebugEnabled()) log.debug("Unable to send status, removing listener from signalMast " + name);
                    signalMast.removePropertyChangeListener(this);
                    removeSignalMastFromList(name);
                }
            }
        }
    }
}
