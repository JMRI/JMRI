// OlcbTurnout.java

package jmri.jmrix.openlcb;

import jmri.Turnout;

import jmri.jmrix.can.*;

/**
 * Turnout for OpenLCB connections.
 * 
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010, 2011
 * @version $Revision$
 */
public class OlcbTurnout extends jmri.implementation.AbstractTurnout 
                    implements CanListener {

    OlcbAddress addrThrown;   // go to thrown state
    OlcbAddress addrClosed;   // go to closed state

	protected OlcbTurnout(String systemName) {
		super(systemName);
        init(systemName);
	}

	protected OlcbTurnout(String systemName, String userName) {
		super(systemName, userName);
        init(systemName);
	}

    /**
     * Common initialization for both constructors.
     * <p>
     * 
     */
    private void init(String systemName) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(systemName.substring(2,systemName.length()));
        OlcbAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+systemName);
            return;
        }
        switch (v.length) {
            case 1:
                addrThrown = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (systemName.substring(2,3).equals("+")) {
                    addrClosed = new OlcbAddress("-"+systemName.substring(3,systemName.length()));
                } else if (systemName.substring(2,3).equals("-")) {
                    addrClosed = new OlcbAddress("+"+systemName.substring(3,systemName.length()));
                } else {
                    log.error("can't make 2nd event from systemname "+systemName);
                    return;
                }
                break;
            case 2:
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Turnout system name: "+systemName);
                return;
        }
        // connect
        TrafficController.instance().addCanListener(this);
    }

	/**
	 * Handle a request to change state by sending CBUS events.
	 * 
	 * @param s new state value
	 */
	protected void forwardCommandChangeToLayout(int s) {
        CanMessage m;
        if (s==Turnout.THROWN) {
            m = addrThrown.makeMessage();
            TrafficController.instance().sendCanMessage(m, this);
        } else if (s==Turnout.CLOSED) {
            m = addrClosed.makeMessage();
            TrafficController.instance().sendCanMessage(m, this);
        }
	}

    public void message(CanMessage f) {
        if (addrThrown.match(f)) {
            newCommandedState(THROWN);
        } else if (addrClosed.match(f)) {
            newCommandedState(CLOSED);
        }
    }

    public void reply(CanReply f) {
        if (addrThrown.match(f)) {
            newCommandedState(THROWN);
        } else if (addrClosed.match(f)) {
            newCommandedState(CLOSED);
        }
    }
    
	protected void turnoutPushbuttonLockout(boolean locked) {}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OlcbTurnout.class.getName());
}

/* @(#)OlcbTurnout.java */

