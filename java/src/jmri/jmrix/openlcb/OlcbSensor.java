package jmri.jmrix.openlcb;

import org.openlcb.EventID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.VersionedValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

import jmri.Sensor;
import jmri.implementation.AbstractSensor;

/**
 * Extend jmri.AbstractSensor for OpenLCB controls.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2008, 2010, 2011
 */
public class OlcbSensor extends AbstractSensor {

    static int ON_TIME = 500; // time that sensor is active after being tripped
    Timer timer = null;

    OlcbAddress addrActive;    // go to active state
    OlcbAddress addrInactive;  // go to inactive state
    OlcbInterface iface;

    VersionedValueListener<Boolean> sensorListener;
    BitProducerConsumer pc;

    public OlcbSensor(String prefix, String address, OlcbInterface iface) {
        super(prefix + "S" + address);
        this.iface = iface;
        init(address);
    }

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
                pc = new BitProducerConsumer(iface, addrActive.toEventID(), new
                        EventID("00.00.00.00.00.00.00.00"), false);
                timer = new Timer(true);
                sensorListener = new VersionedValueListener<Boolean>(pc.getValue()) {
                    @Override
                    public void update(Boolean value) {
                        setOwnState(value ? Sensor.ACTIVE : Sensor.INACTIVE);
                        if (value) {
                            setTimeout();
                        }
                    }
                };
                break;
            case 2:
                addrActive = v[0];
                addrInactive = v[1];
                pc = new BitProducerConsumer(iface, addrActive.toEventID(),
                        addrInactive.toEventID(), false);
                sensorListener = new VersionedValueListener<Boolean>(pc.getValue()) {
                    @Override
                    public void update(Boolean value) {
                        setOwnState(value ? Sensor.ACTIVE : Sensor.INACTIVE);
                    }
                };
                break;
            default:
                log.error("Can't parse OpenLCB Sensor system name: " + address);
                return;
        }
    }

    /**
     * Request an update on status by sending CBUS message.
     * <p>
     * There is no known way to do this, so the request is just ignored.
     */
    @Override
    public void requestUpdateFromLayout() {
    }

    /**
     * User request to set the state, which means that we broadcast that to all
     * listeners by putting it out on CBUS. In turn, the code in this class
     * should use setOwnState to handle internal sets and bean notifies.
     *
     */
    @Override
    public void setKnownState(int s) throws jmri.JmriException {
        setOwnState(s);
        if (s == Sensor.ACTIVE) {
            sensorListener.setFromOwner(true);
            if (addrInactive == null) {
                setTimeout();
            }
        } else if (s == Sensor.INACTIVE) {
            sensorListener.setFromOwner(false);
        }
    }

    /**
     * Have sensor return to inactive after delay, used if no inactive event was
     * specified
     */
    void setTimeout() {
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                try {
                    setKnownState(Sensor.INACTIVE);
                } catch (jmri.JmriException e) {
                    log.error("error setting momentary sensor INACTIVE", e);
                }
            }
        }, ON_TIME);
    }

    @Override
    public void dispose() {
        if (sensorListener != null) sensorListener.release();
        if (pc != null) pc.release();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensor.class.getName());

}
