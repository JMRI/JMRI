// CbusSensor.java
package jmri.jmrix.can.cbus;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for CBUS controls.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version $Revision$
 */
public class CbusSensor extends AbstractSensor implements CanListener {

    /**
     *
     */
    private static final long serialVersionUID = -3589288718741372494L;
    CbusAddress addrActive;    // go to active state
    CbusAddress addrInactive;  // go to inactive state

    public CbusSensor(String prefix, String address, TrafficController tc) {
        super(prefix + "S" + address);
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
                addrActive = v[0];
                // need to complement here for addr 1
                // so address _must_ start with address + or -
                if (address.startsWith("+")) {
                    addrInactive = new CbusAddress("-" + address.substring(1));
                } else if (address.startsWith("-")) {
                    addrInactive = new CbusAddress("+" + address.substring(1));
                } else {
                    log.error("can't make 2nd event from systemname " + address);
                    return;
                }
                break;
            case 2:
                addrActive = v[0];
                addrInactive = v[1];
                break;
            default:
                log.error("Can't parse CbusSensor system name: " + address);
                return;
        }
        // connect
        tc.addCanListener(this);
    }

    /**
     * Request an update on status by sending CBUS message.
     * <p>
     * There is no known way to do this, so the request is just ignored.
     */
    public void requestUpdateFromLayout() {
    }

    /**
     * User request to set the state, which means that we broadcast that to all
     * listeners by putting it out on CBUS. In turn, the code in this class
     * should use setOwnState to handle internal sets and bean notifies.
     *
     * @throws jmri.JmriException
     */
    public void setKnownState(int s) throws jmri.JmriException {
        CanMessage m;
        if (s == Sensor.ACTIVE) {
            m = addrActive.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
            setOwnState(Sensor.ACTIVE);
        } else if (s == Sensor.INACTIVE) {
            m = addrInactive.makeMessage(tc.getCanid());
            tc.sendCanMessage(m, this);
            setOwnState(Sensor.INACTIVE);
        }
    }

    /**
     * Track layout status from messages being sent to CAN
     *
     */
    public void message(CanMessage f) {
        if (addrActive.match(f)) {
            setOwnState(Sensor.ACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(Sensor.INACTIVE);
        }
    }

    /**
     * Track layout status from messages being received from CAN
     *
     */
    public void reply(CanReply f) {
        if (addrActive.match(f)) {
            setOwnState(Sensor.ACTIVE);
        } else if (addrInactive.match(f)) {
            setOwnState(Sensor.INACTIVE);
        }
    }

    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensor.class.getName());

}


/* @(#)CbusSensor.java */
