package jmri.jmrix.dccpp;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement TurnoutManager for DCC++ systems.
 * <p>
 * System names are "DxppTnnn", where Dx is the system prefix and nnn is the turnout number without padding.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Mark Underwood Copyright (C) 2015
 */
public class DCCppTurnoutManager extends jmri.managers.AbstractTurnoutManager implements DCCppListener {

    protected DCCppTrafficController tc = null;
    protected String prefix = null;

    /**
     * Create an new DCC++ TurnoutManager.
     * Has to register for DCC++ events.
     *
     * @param controller the TrafficController to connect the TurnoutManager to
     * @param prefix the system connection prefix string as set for this connection in SystemConnectionMemo
     */
    public DCCppTurnoutManager(DCCppTrafficController controller, String prefix) {
        super();
        tc = controller;
        this.prefix = prefix;
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
    }

    @Override
    public String getSystemPrefix() {
        return prefix;
    }

    // DCCpp-specific methods

    /** {@inheritDoc} */
    @Override
    public Turnout createNewTurnout(String systemName, String userName) {
        Turnout t = null;
        // check if the output bit is available
        int bitNum = getBitFromSystemName(systemName);
        if (bitNum < 0) {
            return null;
        }
        // make the new Turnout object
        t = new DCCppTurnout(prefix, bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    /** {@inheritDoc}
    * Listen for turnouts, creating them as needed.
    */
    @Override
    public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("received message: {}", l.toString());
        }
        if (l.isTurnoutReply()) {
            // parse message type
            int addr = l.getTOIDInt();
            if (addr >= 0) {
                // check to see if the address has been operated before
                // continuing.
                log.debug("message has address: {}", addr);
                // reach here for switch command; make sure we know 
                // about this one
                String s = prefix + typeLetter() + addr;
                if (null == getBySystemName(s)) {
                    // need to create a new one, and send the message on 
                    // to the newly created object.
                    ((DCCppTurnout) provideTurnout(s)).setFeedbackMode(Turnout.MONITORING);
                    ((DCCppTurnout) provideTurnout(s)).initmessage(l);
                } else {
                    // The turnout exists, forward this message to the 
                    // turnout
                    ((DCCppTurnout) getBySystemName(s)).message(l);
                }
            }
        } else if (l.isOutputCmdReply()) {
            // parse message type
            int addr = l.getOutputNumInt();
            if (addr >= 0) {
                // check to see if the address has been operated before
                // continuing.
                log.debug("message has address: {}", addr);
                // reach here for switch command; make sure we know 
                // about this one
                String s = prefix + typeLetter() + addr;
                if (null == getBySystemName(s)) {
                    // need to create a new one, and send the message on 
                    // to the newly created object.
                    ((DCCppTurnout) provideTurnout(s)).setFeedbackMode(Turnout.EXACT);
                    ((DCCppTurnout) provideTurnout(s)).initmessage(l);
                } else {
                    // The turnout exists, forward this message to the 
                    // turnout
                    ((DCCppTurnout) getBySystemName(s)).message(l);
                }
            }
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

    /**
     * Listen for the messages to the LI100/LI101
     */
    @Override
    public void message(DCCppMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(String systemName) {
        return true;
    }

    /**
     * Get the bit address from the system name.
     *
     * @return -1 for failure
     */
    public int getBitFromSystemName(String systemName) {
        // validate the system Name leader characters
        if ((!systemName.startsWith(getSystemPrefix() + typeLetter()))) {
            // here if an illegal DCC++ turnout system name
            log.error("illegal character in header field of DCC++ turnout system name: {} prefix {} type {}",
                    systemName, getSystemPrefix(), typeLetter());
            return -1;
        }
        // name must be in the DCCppTnnnnn format (DCCPP is user configurable)
        int num = 0;
        try {
            num = Integer.parseInt(systemName.substring(
                    getSystemPrefix().length() + 1, systemName.length()));
        } catch (Exception e) {
            log.error("invalid character in number field of system name: {}", systemName);
            return -1;
        }
        if (num < 0) {
            log.error("invalid DCC++ turnout system name: {}", systemName);
            return -1;
        } else if (num > DCCppConstants.MAX_ACC_DECODER_JMRI_ADDR) {
            log.error("bit number out of range in DCC++ turnout system name: {}", systemName);
            return -1;
        }
        return num;
    }

    /**
     * Validate system name format.
     *
     * @return VALID if system name has a valid format, else return INVALID
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != 0) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    @Deprecated
    static public DCCppTurnoutManager instance() {
        //if (_instance == null) _instance = new DCCppTurnoutManager();
        return _instance;
    }
    static DCCppTurnoutManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManager.class);

}
