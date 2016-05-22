// MarklinPowerManager.java

package jmri.jmrix.marklin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.JmriException;
import jmri.PowerManager;

/**
 * PowerManager implementation for controlling layout power.
 *
 * @author	Kevin Dickerson (C) 2012
 * @version	$Revision: 17977 $
 */
public class MarklinPowerManager implements PowerManager, MarklinListener {

    public MarklinPowerManager(MarklinTrafficController etc) {
        // connect to the TrafficManager
        tc = etc;
        tc.addMarklinListener(this);
        
    }
    
    MarklinTrafficController tc;

    public String getUserName() { return "Marklin"; }

    int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v==ON) {
            // send message to turn on
            MarklinMessage l = MarklinMessage.getEnableMain();
            tc.sendMarklinMessage(l, this);
        } else if (v==OFF) {
            // send message to turn off
            MarklinMessage l = MarklinMessage.getKillMain();
            tc.sendMarklinMessage(l, this);
        }
        firePropertyChange("Power", null, null);
    }

    public int getPower() { return power;}

    // to free resources when no longer used
    public void dispose() throws JmriException {
        tc.removeMarklinListener(this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) throw new JmriException("attempt to use MarklinPowerManager after dispose");
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

    // to listen for status changes from Marklin system
    public void reply(MarklinReply m) {
        // power message?
        if(m.getPriority()==MarklinConstants.PRIO_1 && m.getCommand()==MarklinConstants.SYSCOMMANDSTART && m.getAddress()==0x0000){
            switch (m.getElement(9)){
                case MarklinConstants.CMDGOSYS   : power = ON; break;
                case MarklinConstants.CMDSTOPSYS : power = OFF; break;
                case MarklinConstants.CMDHALTSYS : power = OFF; break;
                default:    log.warn("Unknown sub command " + m.getElement(9));
            }
            firePropertyChange("Power", null, null);
        }
    }

    public void message(MarklinMessage m) {
        // messages are ignored
    }
    static Logger log = LoggerFactory.getLogger(MarklinPowerManager.class.getName());
}


/* @(#)MarklinPowerManager.java */
