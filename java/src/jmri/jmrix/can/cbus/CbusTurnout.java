package jmri.jmrix.can.cbus;

import jmri.Turnout;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Turnout for CBUS connections.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class CbusTurnout extends jmri.implementation.AbstractTurnout
        implements CanListener {

    CbusAddress addrThrown;   // go to thrown state
    CbusAddress addrClosed;   // go to closed state

    protected CbusTurnout(String prefix, String address, TrafficController tc) {
        super(prefix + "T" + address);
        this.tc = tc;
        init(address);

    }

    TrafficController tc;

    /**
     * Common initialization for both constructors.
     * <p>
     *
     */
    private void init(String address) {
        // log.debug("37 address : {} should have leading + or - for normal event", address);
        // build local addresses
        CbusAddress a = new CbusAddress(address);
        // log.debug("40 a : {} ", a);
        CbusAddress[] v = a.split();
        if (v == null) {
            // throw exception same as other turnout hardwares, should have been filtered by the turnout manager?
            throw new IllegalArgumentException("45 Turnout value: " + address 
                    + " not a usable system name.");
        }
        switch (v.length) {
            case 1:
                addrThrown = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrClosed = new CbusAddress("-" + address.substring(1));
                } else if (address.startsWith("-")) {
                    addrClosed = new CbusAddress("+" + address.substring(1));
                } else {
                    log.warn("57 Cannot make 2nd event from systemname: {} ", address);
                    return;
                }
                break;
            case 2:
                // log.debug ("61 The 2 part split is {} , {} ", v[0], v[1]);
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
            throw new IllegalArgumentException("68 Turnout value: " + address 
                    + " not a usable system name.");
        }
        // connect
        tc.addCanListener(this);
    }

    // Cbus Turnouts do not currently support inversion
    @Override
    public boolean canInvert() {
        return false;
    }
    
    /**
     * Handle a request to change state by sending CBUS events.
     *
     * @param s new state value
     */
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        CanMessage m;
        if (s == Turnout.THROWN) {
            m = addrThrown.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
        } else if (s == Turnout.CLOSED) {
            m = addrClosed.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
        }
    }

    @Override
    public void message(CanMessage f) {
        if (addrThrown.match(f)) {
            newCommandedState(THROWN);
        } else if (addrClosed.match(f)) {
            newCommandedState(CLOSED);
        }
    }

    @Override
    public void reply(CanReply f) {
        if (addrThrown.match(f)) {
            newCommandedState(THROWN);
        } else if (addrClosed.match(f)) {
            newCommandedState(CLOSED);
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnout.class);
}
