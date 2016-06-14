package jmri.jmrix.lenz;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager.
 * <P>
 * System names are "XTnnn", where nnn is the turnout number without padding.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Paul Bender Copyright (C) 2003-2010
 * @navassoc 1 - 1 jmri.jmrix.lenz.XNetProgrammer
 */
public class XNetTurnoutManager extends jmri.managers.AbstractTurnoutManager implements XNetListener {

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.jmrix.lenz.XNetBundle");

    protected XNetTrafficController tc = null;

    // ctor has to register for XNet events
    public XNetTurnoutManager(XNetTrafficController controller, String prefix) {
        super();
        tc = controller;
        this.prefix = prefix;
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
    }

    public String getSystemPrefix() {
        return prefix;
    }
    protected String prefix = null;

    // XNet-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        Turnout t = new XNetTurnout(prefix, addr, tc);
        t.setUserName(userName);
        return t;
    }

    // listen for turnouts, creating them as needed
    public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l);
        }
        if (l.isFeedbackBroadcastMessage()) {
            int numDataBytes = l.getElement(0) & 0x0f;
            for (int i = 1; i < numDataBytes; i += 2) {
                // parse message type
                int addr = l.getTurnoutMsgAddr(i);
                if (addr >= 0) {
                    // check to see if the address has been operated before
                    // continuing.
                    int a2 = l.getElement(i + 1);
                    if ((a2 & 0x03) != 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("message has address: " + addr);
                        }
                        // reach here for switch command; make sure we know 
                        // about this one
                        String s = prefix + typeLetter() + addr;
                        if (null == getBySystemName(s)) {
                            // need to create a new one, and send the message on 
                            // to the newly created object.
                            ((XNetTurnout) provideTurnout(s)).initmessage(l);
                        } else {
                            // The turnout exists, forward this message to the 
                            // turnout
                            ((XNetTurnout) getBySystemName(s)).message(l);
                        }
                    }
                    if (addr % 2 != 0) {
                        // If the address we got was odd, we need to check to 
                        // see if the even address should be added as well.
                        a2 = l.getElement(i + 1);
                        if ((a2 & 0x0c) != 0) {
                            // reach here for switch command; make sure we know 
                            // about this one
                            String s = prefix + typeLetter() + (addr + 1);
                            if (null == getBySystemName(s)) {
                                // need to create a new one, and send the message on 
                                // to the newly created object.
                                ((XNetTurnout) provideTurnout(s)).message(l);
                            } else {
                                // The turnout exists, forward this message to the 
                                // turnout
                                ((XNetTurnout) getBySystemName(s)).message(l);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system to
     * represent the Turnout.CLOSED state.
     */
    public String getClosedText() {
        return rbt.getString("TurnoutStateClosed");
    }

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state.
     */
    public String getThrownText() {
        return rbt.getString("TurnoutStateThrown");
    }

    // listen for the messages to the LI100/LI101
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    @Deprecated
    static public XNetTurnoutManager instance() {
        //if (_instance == null) _instance = new XNetTurnoutManager();
        return _instance;
    }
    static XNetTurnoutManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(XNetTurnoutManager.class.getName());

}

/* @(#)XNetTurnoutManager.java */
