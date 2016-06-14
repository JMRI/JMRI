// TamsTurnoutManager.java
package jmri.jmrix.tams;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Tams systems. Reworked to support binary
 * commands and polling of command station
 * <P>
 *
 * Based on work by Bob Jacobsen & Kevin Dickerson
 *
 * @author Jan Boen
 * @version $Revision: 20160607 $
 */
public class TamsTurnoutManager extends jmri.managers.AbstractTurnoutManager implements TamsListener {

    public TamsTurnoutManager(TamsSystemConnectionMemo memo) {
        adaptermemo = memo;
        prefix = adaptermemo.getSystemPrefix();
        tc = adaptermemo.getTrafficController();
        //Request status of turnout changes
        TamsMessage m = TamsMessage.getXEvtTrn();
        tc.sendTamsMessage(m, this);
        tc.addPollMessage(m, this);
    }

    TamsTrafficController tc;
    TamsSystemConnectionMemo adaptermemo;

    String prefix;

    public String getSystemPrefix() {
        return prefix;
    }

    public Turnout createNewTurnout(String systemName, String userName) {
        int addr;
        try {
            addr = Integer.valueOf(systemName.substring(getSystemPrefix().length() + 1)).intValue();
        } catch (java.lang.NumberFormatException e) {
            log.error("failed to convert systemName " + systemName + " to a turnout address");
            return null;
        }
        Turnout t = new TamsTurnout(addr, getSystemPrefix(), tc);
        t.setUserName(userName);
        return t;
    }

    boolean noWarnDelete = false;

    public void message(TamsMessage m) {
        //TamsMessages are ignored
    }

    // to hear of changes - copied from PowerManager
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

    public void reply(TamsReply r) {//To listen for Turnout status changes
        log.info("*** TamsReply ***");
        TamsMessage tm = TamsMessage.getXEvtTrn();
        log.info("ReplyType = " +
                tm.getReplyType() +
                ", Binary? = " +
                tm.isBinary() +
                ", OneByteReply = " +
                tm.getReplyOneByte());
        if (tm.getReplyType() == 'T') {//Only handle Turnout events
            if (tm.isBinary() == true) {//Typical polling message
                if ((r.getNumDataElements() > 1) && (r.getElement(0) > 0x00) && (r.getElement(0) != 'T')) {
                    //Here we break up a long turnout related TamsReply into individual turnout status'
                    for (int i = 1; i < r.getNumDataElements() - 1; i = i + 2) {
                        //create a new TamsReply and pass it to the decoder
                        TamsReply tr = new TamsReply();
                        tr.setBinary(r.isBinary());
                        tr.setElement(0, r.getElement(i));
                        tr.setElement(1, r.getElement(i + 1));
                        log.info("Going to pass this to the decoder = " + tr.toString());
                        //The decodeTurnoutState will do the actual decoding of each individual turnout
                        decodeTurnoutState(tr, prefix, tc);
                    }
                }
            } else {//xSR is an ASCII message
                //Nothing to do really
                log.info("Reply to ACSII command = " + r.toString());
            }
        }
    }

    void decodeTurnoutState(TamsReply r, String prefix, TamsTrafficController tc) {
        //reply to XEvtSen consists of 2 bytes per turnout
        //1: LSB of turnout address (A0 .. A7)
        //2: MSB of turnout address (A8 .. A10) incl. direction
        //bit#   7     6     5     4     3     2     1     0
        //+-----+-----+-----+-----+-----+-----+-----+-----+
        //|Color| Sts |  0  |  0  |  0  | A10 |  A9 |  A8 |
        //+-----+-----+-----+-----+-----+-----+-----+-----+
        //Color 1 = straight (green), 0 = turnout (red)
        //Sts 1 = on, 0 = off
        //A10..A8 MSBs of turnout address
        int lowerByte = r.getElement(0) & 0xff;
        int upperByte = r.getElement(1) & 0xff;
        log.info("Decoding turnout");
        log.info("Lower Byte: " + lowerByte);
        log.info("Upper Byte: " + upperByte);
        //Core logic to be added here
        int turnoutAddress = (upperByte & 0x07) * 256 + lowerByte;
        int turnoutState = Turnout.CLOSED;
        if ((upperByte & 0x80) == 0x80) {//Only need bit #7
            turnoutState = Turnout.THROWN;
        }
        log.info("Turnout Address: \"" + prefix + "T" + turnoutAddress + "\", state: " + turnoutState);

        //OK. Now how do we get the turnout to update in JMRI?
        //Next line provided via JMRI dev's
        TamsTurnout ttu = provideTurnout(prefix + "T" + turnoutAddress);
        ttu.setCommandedState(turnoutState);

        /*
         * if (!stopPolling) { synchronized (pollHandler) {
         * pollHandler.notify(); } }
         */
    }

    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutManager.class.getName());

}

/* @(#)TamsTurnoutManager.java */