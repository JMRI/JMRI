package jmri.jmrix.dccpp;

import static jmri.jmrix.dccpp.DCCppConstants.MAX_TURNOUT_ADDRESS;

import java.util.Locale;
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

    /**
     * Create an new DCC++ TurnoutManager.
     * Has to register for DCC++ events.
     *
     * @param memo the supporting system connection memo
     */
    public DCCppTurnoutManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getDCCppTrafficController();
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DCCppSystemConnectionMemo getMemo() {
        return (DCCppSystemConnectionMemo) memo;
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
        t = new DCCppTurnout(getSystemPrefix(), bitNum, tc);
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
                String s = getSystemNamePrefix() + addr;
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
                String s = getSystemNamePrefix() + addr;
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
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return (getBitFromSystemName(systemName) != -1) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String validateSystemNameFormat(String systemName, Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 0, MAX_TURNOUT_ADDRESS, locale);
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName a valid LocoNet-based Turnout System Name
     * @return the turnout number extracted from the system name
     */
    public int getBitFromSystemName(String systemName) {
        try {
            validateSystemNameFormat(systemName, Locale.getDefault());
        } catch (IllegalArgumentException ex) {
            return -1;
        }
        return Integer.parseInt(systemName.substring(getSystemNamePrefix().length()));
    }

    /** {@inheritDoc} */
    @Override
    public String getEntryToolTip() {
        return Bundle.getMessage("AddOutputEntryToolTip");
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManager.class);

}
