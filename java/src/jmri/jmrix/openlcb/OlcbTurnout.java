// OlcbTurnout.java

package jmri.jmrix.openlcb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	protected OlcbTurnout(String prefix, String address, TrafficController tc) {
		super(prefix+"T"+address);
        this.tc =tc;
        init(address);
	}

    TrafficController tc;
    
    /**
     * Common initialization for both constructors.
     * <p>
     * 
     */
    private void init(String address) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v==null) {
            log.error("Did not find usable system name: "+address);
            return;
        }
        switch (v.length) {
            case 1:
                addrThrown = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrClosed = new OlcbAddress("-"+address);
                } else if (address.startsWith("-")) {
                    addrClosed = new OlcbAddress("+"+address);
                } else {
                    log.error("can't make 2nd event from systemname "+address);
                    return;
                }
                break;
            case 2:
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Turnout system name: "+address);
                return;
        }
        // connect
        tc.addCanListener(this);
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
            tc.sendCanMessage(m, this);
        } else if (s==Turnout.CLOSED) {
            m = addrClosed.makeMessage();
            tc.sendCanMessage(m, this);
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

	static Logger log = LoggerFactory.getLogger(OlcbTurnout.class.getName());
}

/* @(#)OlcbTurnout.java */

