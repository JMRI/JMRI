// TamsPowerManager.java

package jmri.jmrix.tams;

import org.apache.log4j.Logger;
import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version	$Revision: 17977 $
 */
public class TamsPowerManager implements PowerManager, TamsListener {

    public TamsPowerManager(TamsTrafficController etc) {
        // connect to the TrafficManager
        tc = etc;

        TamsMessage m = new TamsMessage("xY");
        tc.sendTamsMessage(m, this);
        tc.addPollMessage(m, this);
    }
    
    TamsTrafficController tc;
    Thread TamsPowerMonitorThread;

    public String getUserName() { return "Tams"; }

    int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v==ON) {
            // send message to turn on
            TamsMessage l = new TamsMessage("xGO");
            lastRequest=v;
            tc.sendTamsMessage(l, this);
        } else if (v==OFF) {
            lastRequest=v;
            // send message to turn off - twice
            TamsMessage l = new TamsMessage("xSTOP");
            tc.sendTamsMessage(l, this);
            tc.sendTamsMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }
    
    int lastRequest = 0;

    public int getPower() { return power;}

    // to free resources when no longer used
    public void dispose() throws JmriException {
        TamsMessage m = new TamsMessage("xY");
        tc.removePollMessage(m, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use TamsPowerManager after dispose");
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

    // to listen for status changes from Tams system
    public void reply(TamsReply m) {
        // power message?
        if(m.match("Pwr off")!=-1){
            power = OFF;
            firePropertyChange("Power", null, null);
        } else if (m.match("Pwr on")!=-1){
            power = ON;
            firePropertyChange("Power", null, null);
        } else if(m.getElement(0)==0x00){
            if(lastRequest==ON){
                power = ON;
            } else if (lastRequest==OFF){
                power = OFF;
            }
        } else {
            log.info("Unknown reply in power manager " + m.toString());
        }
    }
    
    public void message(TamsMessage m) {
        // messages are ignored
    }
    
    static Logger log = Logger.getLogger(TamsPowerManager.class.getName());
}


/* @(#)TamsPowerManager.java */

