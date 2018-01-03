package jmri.jmrix.openlcb;

import java.util.Timer;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.implementation.AbstractSensor;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.VersionedValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;

/**
 * Extend jmri.AbstractSensor for OpenLCB controls.
 * <P>
 * @author Bob Jacobsen Copyright (C) 2008, 2010, 2011
 */
public class OlcbSensor extends AbstractSensor {

    static int ON_TIME = 500; // time that sensor is active after being tripped
    Timer timer = null;

    OlcbAddress addrActive;    // go to active state
    OlcbAddress addrInactive;  // go to inactive state
    OlcbInterface iface;

    VersionedValueListener<Boolean> sensorListener;
    BitProducerConsumer pc;
    EventTable.EventTableEntryHolder activeEventTableEntryHolder = null;
    EventTable.EventTableEntryHolder inactiveEventTableEntryHolder = null;

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
                pc = new BitProducerConsumer(iface, addrActive.toEventID(), BitProducerConsumer.nullEvent, false);
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
        activeEventTableEntryHolder = iface.getEventTable().addEvent(addrActive.toEventID(), getEventName(true));
        if (addrInactive != null) {
            inactiveEventTableEntryHolder = iface.getEventTable().addEvent(addrInactive.toEventID(), getEventName(false));
        }
    }

    /**
     * Computes the display name of a given event to be entered into the Event Table.
     * @param isActive true for sensor active, false for inactive.
     * @return user-visible string to represent this event.
     */
    private String getEventName(boolean isActive) {
        String name = getUserName();
        if (name == null) name = mSystemName;
        String msgName = isActive ? "SensorActiveEventName": "SensorInactiveEventName";
        return Bundle.getMessage(msgName, name);
    }

    /**
     * Updates event table entries when the user name changes.
     * @param s new user name
     * @throws NamedBean.BadUserNameException see {@link NamedBean}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setUserName(String s) throws NamedBean.BadUserNameException {
        super.setUserName(s);
        if (activeEventTableEntryHolder != null) {
            activeEventTableEntryHolder.getEntry().updateDescription(getEventName(true));
        }
        if (inactiveEventTableEntryHolder != null) {
            inactiveEventTableEntryHolder.getEntry().updateDescription(getEventName(false));
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
            sensorListener.setFromOwnerWithForceNotify(true);
            if (addrInactive == null) {
                setTimeout();
            }
        } else if (s == Sensor.INACTIVE) {
            sensorListener.setFromOwnerWithForceNotify(false);
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

    /*
     * since the events that drive a sensor can be whichever state a user
     * wants, the order of the event pair determines what is the 'active' state
     */
    @Override
    public boolean canInvert() {
        return false;
    }

    @Override
    public void dispose() {
        if (sensorListener != null) sensorListener.release();
        if (pc != null) pc.release();
        super.dispose();
    }

    /**
     * {@inheritDoc} 
     * 
     * Sorts by decoded EventID(s)
     */
    @CheckReturnValue
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull jmri.NamedBean n) {
        return OlcbSystemConnectionMemo.compareSystemNameSuffix(suffix1, suffix2);
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensor.class);

}
