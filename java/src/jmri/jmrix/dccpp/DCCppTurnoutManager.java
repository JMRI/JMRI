package jmri.jmrix.dccpp;

import jmri.Turnout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement turnout manager.
 * <P>
 * System names are "DCCppTnnn", where nnn is the turnout number without padding.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @author	Paul Bender Copyright (C) 2003-2010
 * @author	Mark Underwood Copyright (C) 2015
 */
public class DCCppTurnoutManager extends jmri.managers.AbstractTurnoutManager implements DCCppListener {

    final java.util.ResourceBundle rbt = java.util.ResourceBundle.getBundle("jmri.jmrix.dccpp.DCCppBundle");

    protected DCCppTrafficController tc = null;

    // ctor has to register for DCCpp events
    public DCCppTurnoutManager(DCCppTrafficController controller, String prefix) {
        super();
        tc = controller;
        this.prefix = prefix;
        tc.addDCCppListener(DCCppInterface.FEEDBACK, this);
    }

    public String getSystemPrefix() {
        return prefix;
    }
    protected String prefix = null;

    // DCCpp-specific methods
    public Turnout createNewTurnout(String systemName, String userName) {
        int addr = Integer.valueOf(systemName.substring(prefix.length() + 1)).intValue();
        Turnout t = new DCCppTurnout(prefix, addr, tc);
        t.setUserName(userName);
        return t;
    }

    // listen for turnouts, creating them as needed
    public void message(DCCppReply l) {
        if (log.isDebugEnabled()) {
            log.debug("recieved message: " + l.toString());
        }
        if (l.isTurnoutReply()) {
	    // parse message type
	    int addr = l.getTOIDInt();
	    if (addr >= 0) {
		// check to see if the address has been operated before
		// continuing.
		if (log.isDebugEnabled()) {
		    log.debug("message has address: " + addr);
		}
		// reach here for switch command; make sure we know 
		// about this one
		String s = prefix + typeLetter() + addr;
		if (null == getBySystemName(s)) {
		    // need to create a new one, and send the message on 
		    // to the newly created object.
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
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    @Deprecated
    static public DCCppTurnoutManager instance() {
        //if (_instance == null) _instance = new DCCppTurnoutManager();
        return _instance;
    }
    static DCCppTurnoutManager _instance = null;

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManager.class.getName());

}
