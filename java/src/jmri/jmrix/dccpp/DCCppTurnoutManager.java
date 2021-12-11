package jmri.jmrix.dccpp;

import static jmri.jmrix.dccpp.DCCppConstants.MAX_TURNOUT_ADDRESS;

import java.util.Locale;
import javax.annotation.Nonnull;
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
     * Create a new DCC++ TurnoutManager.
     * Has to register for DCC++ events.
     *
     * @param memo the supporting system connection memo
     */
    public DCCppTurnoutManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getDCCppTrafficController();
        // set up listener
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
        // request list of turnouts
        tc.sendDCCppMessage(DCCppMessage.makeTurnoutListMsg(), this);
        // request list of outputs
        tc.sendDCCppMessage(DCCppMessage.makeOutputListMsg(), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public DCCppSystemConnectionMemo getMemo() {
        return (DCCppSystemConnectionMemo) memo;
    }

    // DCCpp-specific methods

    /**
     * {@inheritDoc}
     */
    @Nonnull
    @Override
    protected Turnout createNewTurnout(@Nonnull String systemName, String userName) throws IllegalArgumentException {
        // check if the output bit is available
        int bitNum = getBitFromSystemName(systemName);
        if (bitNum < 0) {
            throw new IllegalArgumentException("Cannot get Bit from System Name " + systemName);
        }
        // make the new Turnout object
        Turnout t = new DCCppTurnout(getSystemPrefix(), bitNum, tc);
        t.setUserName(userName);
        return t;
    }

    /**
     * {@inheritDoc}
     * Listen for turnouts, creating them as needed.
     */
    @Override
    public void message(DCCppReply l) {
        if (l.isTurnoutReply()) {
            log.debug("received Turnout Reply message: '{}'", l);
            // parse message type
            int addr = l.getTOIDInt();
            if (addr >= 0) {
                // check to see if the address has been operated before
                // continuing.
                log.debug("message has address: {}", addr);
                // reach here for switch command; make sure we know 
                // about this one
                String s = getSystemNamePrefix() + addr;
                DCCppTurnout found = (DCCppTurnout) getBySystemName(s);
                if ( found == null) {
                    // need to create a new one, set some attributes, 
                    //  and send the message on to the newly created object.
                    DCCppTurnout t = (DCCppTurnout) provideTurnout(s);
                    t.setFeedbackMode(Turnout.MONITORING);
//                    t.setComment(l.toComment());  //removed to avoid confusion since this is only set, not updated 
                    t.initmessage(l);
                } else {
                    // The turnout exists, forward this message to the 
                    // turnout
                    found.message(l);
                }
            }
        } else if (l.isOutputReply()) {
            log.debug("received Output Reply message: '{}'", l);
            // parse message type
            int addr = l.getOutputNumInt();
            if (addr >= 0) {
                // check to see if the address has been operated before
                // continuing.
                log.debug("message has address: {}", addr);
                // reach here for switch command; make sure we know 
                // about this one
                String s = getSystemNamePrefix() + addr;
                DCCppTurnout found = (DCCppTurnout) getBySystemName(s);
                if (found == null) {
                    // need to create a new one, and send the message on 
                    // to the newly created object.
                    DCCppTurnout t = (DCCppTurnout) provideTurnout(s);
                    t.setFeedbackMode(Turnout.EXACT);
//                  t.setComment(l.toComment());  //removed to avoid confusion since this is only set, not updated 
                    t.initmessage(l);
                } else {
                    // The turnout exists, forward this message to the 
                    // turnout
                    found.message(l);
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
    @Nonnull
    public String getClosedText() {
        return Bundle.getMessage("TurnoutStateClosed");
    }

    /**
     * Get text to be used for the Turnout.THROWN state in user communication.
     * Allows text other than "THROWN" to be use with certain hardware system to
     * represent the Turnout.THROWN state.
     */
    @Override
    @Nonnull
    public String getThrownText() {
        return Bundle.getMessage("TurnoutStateThrown");
    }

    /**
     * Listen for the outgoing messages (to the command station)
     */
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle message timeout notification
    // If the message still has retries available, reduce retries and send it back to the traffic controller.
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}' , {} retries available.", msg, msg.getRetries());
        if (msg.getRetries() > 0) {
            msg.setRetries(msg.getRetries() - 1);
            tc.sendDCCppMessage(msg, this);
        }        
    }

    /** {@inheritDoc} */
    @Override
    public boolean allowMultipleAdditions(@Nonnull String systemName) {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NameValidity validSystemNameFormat(@Nonnull String systemName) {
        return (getBitFromSystemName(systemName) != -1) ? NameValidity.VALID : NameValidity.INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public String validateSystemNameFormat(@Nonnull String systemName, @Nonnull Locale locale) {
        return validateIntegerSystemNameFormat(systemName, 0, MAX_TURNOUT_ADDRESS, locale);
    }

    /**
     * Get the bit address from the system name.
     *
     * @param systemName a valid Turnout System Name
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
