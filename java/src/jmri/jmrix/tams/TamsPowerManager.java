// TamsPowerManager.java
package jmri.jmrix.tams;

import jmri.JmriException;
import jmri.PowerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PowerManager implementation for controlling layout power using binary P50x
 * commands
 *
 * Based on work by Bob Jacobsen and Kevin Dickerson
 *
 * @author Jan Boen
 * @version $Revision: 160524 $
 */
public class TamsPowerManager implements PowerManager, TamsListener {

    //This dummy message is used in case we expect a reply from polling
    static private TamsMessage myDummy() {
        log.debug("*** myDummy ***");
        TamsMessage m = new TamsMessage(2);
        m.setElement(0, TamsConstants.POLLMSG & TamsConstants.MASKFF);
        m.setElement(1, TamsConstants.XSTATUS & TamsConstants.MASKFF);
        m.setBinary(true);
        m.setReplyOneByte(true);
        m.setReplyType('P');
        return m;
    }
    //A local TamsMessage is held at all time
    //When no TamsMessage is being generated via the UI this dummy is used which means the TamsReply is a result of polling
    TamsMessage tm = myDummy();
    
    public TamsPowerManager(TamsTrafficController ttc) {
        log.debug("*** TamsPowerManager ***");
        // connect to the TrafficManager
        tc = ttc;
        tc.addTamsListener(this);
        tm = TamsMessage.getXStatus();
        tc.sendTamsMessage(tm, this);
        tc.addPollMessage(tm, this);
        log.debug("TamsMessage added to pollqueue = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
    }

    TamsTrafficController tc;
    Thread TamsPowerMonitorThread;

    public String getUserName() {
        return "Tams";
    }

    int power = UNKNOWN;

    public void setPower(int v) throws JmriException {
        log.debug("*** setPower ***");
        power = UNKNOWN; // while waiting for reply
        checkTC();
        if (v == ON) {
            // send message to turn on
            tm = TamsMessage.setXPwrOn();
            tc.sendTamsMessage(tm, null);//changed this to null in this method
        } else if (v == OFF) {
            // send message to turn off - twice
            TamsMessage tm = TamsMessage.setXPwrOff();
            tc.sendTamsMessage(tm, null);
            tc.sendTamsMessage(tm, null); //Does this work properly???
        }
        firePropertyChange("Power", null, null);
    }

    int lastRequest = 0;

    public int getPower() {
        log.debug("*** getPower ***");
        return power;
    }

    // to free resources when no longer used
    public void dispose() throws JmriException {
        TamsMessage tm = TamsMessage.getXStatus();
        tc.removePollMessage(tm, this);
        tc = null;
    }

    private void checkTC() throws JmriException {
        if (tc == null) {
            throw new JmriException("attempt to use TamsPowerManager after dispose");
        }
    }

    // to hear of changes
    java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

    public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        pcs.firePropertyChange(p, old, n);
    }

    public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    // to listen for status changes from Tams system
    public void reply(TamsReply tr) {
        if (tm.getReplyType() == 'P'){
            log.debug("*** TamsReply ***");
            log.debug("TamsMessage = " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(0) & 0xFF, "") + " " + jmri.util.StringUtil.appendTwoHexFromInt(tm.getElement(1) & 0xFF, "") + " and replyType = " + tm.getReplyType());
            log.debug("TamsReply = " + jmri.util.StringUtil.appendTwoHexFromInt(tr.getElement(0) & 0xFF, ""));
            boolean valid = false;
            if (tm.getElement(0) == TamsConstants.POLLMSG) {//Replying related to Poll Message
                log.debug("Reply to Poll Message");
                //reply to power status check is either 0 for off or 8 for on
                if (tm.getElement(1) == TamsConstants.XSTATUS) {//power status check
                    if ((tr.getElement(0) & TamsConstants.XPWRMASK) == 0x00) {
                        log.debug("Power status = OFF");
                        power = OFF;
                        firePropertyChange("Power", null, null);
                        valid = true;
                    }
                    if ((tr.getElement(0) & TamsConstants.XPWRMASK) == 0x08) {
                        log.debug("Power status = ON");
                        power = ON;
                        firePropertyChange("Power", null, null);
                        valid = true;
                    }
                }
            } else {//Reply related to UI message
                log.debug("Reply to UI Message");
                //reply to power on / power off is always 0x00 any other answer is not correct
                if (tm.getElement(1) == TamsConstants.XPWROFF) {
                    log.debug("Power set = OFF");
                    power = OFF;
                    firePropertyChange("Power", null, null);
                    valid = true;
                }
                if (tm.getElement(1) == TamsConstants.XPWRON) {
                    log.debug("Power set = ON");
                    power = ON;
                    firePropertyChange("Power", null, null);
                    valid = true;
                }
            }
            if (valid == false) {
                power = UNKNOWN;
                log.debug("Unknown reply in power manager " + tr.toString());
            }
        tm = myDummy();
        }
    }

    public void message(TamsMessage tm) {
        // messages are ignored
    }

    private final static Logger log = LoggerFactory.getLogger(TamsPowerManager.class.getName());
}

/* @(#)TamsPowerManager.java */
