package jmri.jmrix.lenz;

import java.util.Locale;
import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager for Lenz (XpresssNet) connections.
 * <p>
 * System names are "XTnnn", where X is the user configurable system prefix,
 * nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2003-2010
 * @navassoc 1 - 1 jmri.jmrix.lenz.XNetProgrammer
 */
public class XNetTurnoutManager extends jmri.managers.AbstractTurnoutManager implements XNetListener {

    // ctor has to register for XNet events
    public XNetTurnoutManager(XNetSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getXNetTrafficController();
        tc.addXNetListener(XNetInterface.FEEDBACK, this);
    }

    protected XNetTrafficController tc = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public XNetSystemConnectionMemo getMemo() {
        return (XNetSystemConnectionMemo) memo;
    }

    // XNet-specific methods

    /**
     * Create a new Turnout based on the system name.
     * Assumes calling method has checked that a Turnout with this
     * system name does not already exist.
     *
     * @return null if the system name is not in a valid format
     */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        // check if the output bit is available
        int bitNum = XNetAddress.getBitFromSystemName(systemName, getSystemPrefix());
        if (bitNum == -1) {
            return (null);
        }
        // create the new Turnout object
        Turnout t = new XNetTurnout(getSystemPrefix(), bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    /**
     * Listen for turnouts, creating them as needed.
     */
    @Override
    public void message(XNetReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: " + l);
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
                        String s = getSystemNamePrefix() + addr;
                        forwardMessageToTurnout(s,l);
                    }
                    if (addr % 2 != 0) {
                        // If the address we got was odd, we need to check to 
                        // see if the even address should be added as well.
                        a2 = l.getElement(i + 1);
                        if ((a2 & 0x0c) != 0) {
                            // reach here for switch command; make sure we know 
                            // about this one
                            String s = getSystemNamePrefix() + (addr + 1);
                            forwardMessageToTurnout(s,l);
                        }
                    }
                }
            }
        }
    }

    protected void forwardMessageToTurnout(String s, XNetReply l){
        XNetTurnout t = (XNetTurnout) getBySystemName(s);
        if ( null == t ) {
           // need to create a new one, and send the message on 
           // to the newly created object.
           ((XNetTurnout) provideTurnout(s)).initmessage(l);
        } else {
           // The turnout exists, forward this message to the 
           // turnout
           t.message(l);
        }
    }

    /**
     * Get text to be used for the Turnout.CLOSED state in user communication.
     * Allows text other than "CLOSED" to be use with certain hardware system to
     * represent the Turnout.CLOSED state.
     */
    @Override
    public String getClosedText() {
        return Bundle.getMessage("TurnoutStateClosed");
    }

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state.
     */
    @Override
    public String getThrownText() {
        return Bundle.getMessage("TurnoutStateThrown");
    }

    // listen for the messages to the LI100/LI101
    @Override
    public void message(XNetMessage l) {
    }

    // Handle a timeout notification
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String name, Locale locale) {
        return validateIntegerSystemNameFormat(name,
                XNetAddress.MINSENSORADDRESS,
                XNetAddress.MAXSENSORADDRESS,
                locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (XNetAddress.validSystemNameFormat(systemName, 'T', getSystemPrefix()));
    }

    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    @Deprecated
    static public XNetTurnoutManager instance() {
        //if (_instance == null) _instance = new XNetTurnoutManager();
        return _instance;
    }
    static XNetTurnoutManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(XNetTurnoutManager.class);

}
