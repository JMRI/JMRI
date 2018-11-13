package jmri.jmrix.can.cbus;

import jmri.Turnout;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.jmrix.can.cbus.CbusMessage;
import jmri.jmrix.can.cbus.CbusOpCodes;
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
        // build local addresses
        CbusAddress a = new CbusAddress(address);
        CbusAddress[] v = a.split();
        if (v == null) {
            log.error("Did not find usable system name: " + address);
            return;
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
                    log.error("can't make 2nd event from systemname " + address);
                    return;
                }
                break;
            case 2:
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
                log.error("Can't parse CbusSensor system name: " + address);
                return;
        }
        // connect
        tc.addCanListener(this);
    }

    /**
     * Request an update on status by sending CBUS request message to thrown address.
     */
    @Override
    public void requestUpdateFromLayout() {
        CanMessage m;
        m = addrThrown.makeMessage(tc.getCanid());
        int opc = CbusMessage.getOpcode(m);
        if (CbusOpCodes.isShortEvent(opc)) {
            m.setOpCode(CbusConstants.CBUS_ASRQ);
        }
        else {
            m.setOpCode(CbusConstants.CBUS_AREQ);
        }
        tc.sendCanMessage(m, this);
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
            if (getInverted()){
                m = addrClosed.makeMessage(tc.getCanid());
            } else {
                m = addrThrown.makeMessage(tc.getCanid());
            }
            tc.sendCanMessage(m, this);
        } else if (s == Turnout.CLOSED) {
            if (getInverted()){
                m = addrThrown.makeMessage(tc.getCanid());
            }
            else {
                m = addrClosed.makeMessage(tc.getCanid());
            }
            tc.sendCanMessage(m, this);
        }
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void message(CanMessage f) {
        if (addrThrown.match(f)) {
            newCommandedState(!getInverted() ? THROWN : CLOSED);
        } else if (addrClosed.match(f)) {
            newCommandedState(!getInverted() ? CLOSED : THROWN);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(CanReply f) {
        // convert response events to normal
        f = CbusMessage.opcRangeToStl(f);
        if (addrThrown.match(f)) {
            newCommandedState(!getInverted() ? THROWN : CLOSED);
        } else if (addrClosed.match(f)) {
            newCommandedState(!getInverted() ? CLOSED : THROWN);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canInvert() {
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusTurnout.class);
}
