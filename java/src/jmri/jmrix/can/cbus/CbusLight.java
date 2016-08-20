package jmri.jmrix.can.cbus;

import jmri.implementation.AbstractLight;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Light implementation for CBUS connections.
 *
 * @author Matthew Harris Copyright (C) 2015
 */
public class CbusLight extends AbstractLight
        implements CanListener {

    CbusAddress addrOn;   // go to on state
    CbusAddress addrOff;   // go to off state

    protected CbusLight(String prefix, String address, TrafficController tc) {
        super(prefix + "L" + address);
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
                addrOn = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrOff = new CbusAddress("-" + address.substring(1));
                } else if (address.startsWith("-")) {
                    addrOff = new CbusAddress("+" + address.substring(1));
                } else {
                    log.error("can't make 2nd event from systemname " + address);
                    return;
                }
                break;
            case 2:
                addrOn = v[0];
                addrOff = v[1];
                break;
            default:
                log.error("Can't parse CbusSensor system name: " + address);
                return;
        }
        // connect
        tc.addCanListener(this);
    }

    /**
     * Handle a request to change state by sending CBUS events.
     *
     */
    @Override
    protected void doNewState(int oldState, int newState) {
        CanMessage m;
        if (newState == ON) {
            m = addrOn.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
        } else if (newState == OFF) {
            m = addrOff.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
        } else {
            log.warn("illegal state requested for Light: " + getSystemName());
        }
    }

    @Override
    public void message(CanMessage f) {
        if (addrOn.match(f)) {
            setState(ON);
        } else if (addrOff.match(f)) {
            setState(OFF);
        }
    }

    @Override
    public void reply(CanReply f) {
        if (addrOn.match(f)) {
            setState(ON);
        } else if (addrOff.match(f)) {
            setState(OFF);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CbusLight.class.getName());
}
