// OlcbSensor.java
package jmri.jmrix.openlcb;

import java.util.Timer;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extend jmri.AbstractSensor for OpenLCB controls.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008, 2010, 2011
 * @version $Revision$
 */
public class OlcbSensor extends AbstractSensor implements CanListener {

    /**
     *
     */
    private static final long serialVersionUID = -1114385900236113026L;
    static int ON_TIME = 500; // time that sensor is active after being tripped
    Timer timer = null;

    OlcbAddress addrActive;    // go to active state
    OlcbAddress addrInactive;  // go to inactive state

    public OlcbSensor(String prefix, String address, TrafficController tc) {
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
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            log.error("Did not find usable system name: " + address);
            return;
        }
        switch (v.length) {
            case 1:
                // momentary sensor
                addrActive = v[0];
                addrInactive = null;
                timer = new Timer(true);
                break;
            case 2:
                addrActive = v[0];
                addrInactive = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Sensor system name: " + address);
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
            m = addrActive.makeMessage();
            tc.sendCanMessage(m, this);
            setOwnState(Sensor.ACTIVE);
            if (addrInactive == null) {
                setTimeout();
            }
        } else if (s == Sensor.INACTIVE) {
            if (addrInactive != null) {
                m = addrInactive.makeMessage();
                tc.sendCanMessage(m, this);
            }
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
            if (addrInactive == null) {
                setTimeout();
            }
        } else if (addrInactive != null && addrInactive.match(f)) {
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
            if (addrInactive == null) {
                setTimeout();
            }
        } else if (addrInactive != null && addrInactive.match(f)) {
            setOwnState(Sensor.INACTIVE);
        }
    }

    /**
     * Have sensor return to inactive after delay, used if no inactive event was
     * specified
     */
    void setTimeout() {
        timer.schedule(new java.util.TimerTask() {
            public void run() {
                try {
                    setKnownState(Sensor.INACTIVE);
                } catch (jmri.JmriException e) {
                    log.error("error setting momentary sensor INACTIVE", e);
                }
            }
        }, ON_TIME);
    }

    public void dispose() {
        tc.removeCanListener(this);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensor.class.getName());

}


/* @(#)OlcbSensor.java */
