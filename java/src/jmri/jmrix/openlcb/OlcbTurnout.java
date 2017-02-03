package jmri.jmrix.openlcb;

import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.VersionedValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.Turnout;
import jmri.jmrix.can.CanMessage;

/**
 * Turnout for OpenLCB connections.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010, 2011
 */
public class OlcbTurnout extends jmri.implementation.AbstractTurnout {

    OlcbAddress addrThrown;   // go to thrown state
    OlcbAddress addrClosed;   // go to closed state
    final OlcbInterface iface;

    VersionedValueListener<Boolean> turnoutListener;
    BitProducerConsumer pc;

    private static final String[] validFeedbackNames = {"MONITORING", "ONESENSOR", "TWOSENSOR",
            "DIRECT"};
    private static final int[] validFeedbackModes = {MONITORING, ONESENSOR, TWOSENSOR, DIRECT};
    private static final int validFeedbackTypes = MONITORING | ONESENSOR | TWOSENSOR | DIRECT;
    private static final int defaultFeedbackType = MONITORING;

    protected OlcbTurnout(String prefix, String address, OlcbInterface iface) {
        super(prefix + "T" + address);
        this.iface = iface;
        this._validFeedbackNames = validFeedbackNames;
        this._validFeedbackModes = validFeedbackModes;
        this._validFeedbackTypes = validFeedbackTypes;
        this._activeFeedbackType = defaultFeedbackType;
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
            case 2:
                addrThrown = v[0];
                addrClosed = v[1];
                break;
            default:
                log.error("Can't parse OpenLCB Turnout system name: " + address);
                return;
        }
    }

    /**
     * Helper function that will be invoked after construction once the feedback type has been
     * set. Used specifically for preventing double initialization when loading turnouts from XML.
     */
    public void finishLoad() {
        // Clear some objects first.
        if (turnoutListener != null) turnoutListener.release();
        if (pc != null) pc.release();
        turnoutListener = null;
        pc = null;

        int flags = 0;
        switch (_activeFeedbackType) {
            case MONITORING:
            default:
                flags = BitProducerConsumer.IS_PRODUCER | BitProducerConsumer.IS_CONSUMER |
                        BitProducerConsumer.LISTEN_EVENT_IDENTIFIED | BitProducerConsumer
                        .QUERY_AT_STARTUP;
                break;
            case DIRECT:
                flags = BitProducerConsumer.IS_PRODUCER;
                break;

        }
        pc = new BitProducerConsumer(iface, addrThrown.toEventID(), addrClosed.toEventID(), flags);
        turnoutListener = new VersionedValueListener<Boolean>(pc.getValue()) {
            @Override
            public void update(Boolean value) {
                int s = value ? THROWN : CLOSED;
                if (_activeFeedbackType != DIRECT) {
                    newCommandedState(s);
                    if (_activeFeedbackType == MONITORING) {
                        newKnownState(s);
                    }
                }
            }
        };
    }

    @Override
    public void setFeedbackMode(int mode) throws IllegalArgumentException {
        boolean recreate = (mode != _activeFeedbackType) && (pc != null);
        super.setFeedbackMode(mode);
        if (recreate) {
            finishLoad();
        }
    }

    /**
     * Handle a request to change state by sending CBUS events.
     *
     * @param s new state value
     */
    protected void forwardCommandChangeToLayout(int s) {
        CanMessage m;
        if (s == Turnout.THROWN) {
            turnoutListener.setFromOwner(true);
            if (_activeFeedbackType == MONITORING) {
                newKnownState(THROWN);
            }
        } else if (s == Turnout.CLOSED) {
            turnoutListener.setFromOwner(false);
            if (_activeFeedbackType == MONITORING) {
                newKnownState(CLOSED);
            }
        }
    }

    protected void turnoutPushbuttonLockout(boolean locked) {
        // TODO: maybe we could get another pair of events in the address and use that event pair
        // to perform a lockout change on the turnout decoder itself.
    }

    public void dispose() {
        if (turnoutListener != null) turnoutListener.release();
        if (pc != null) pc.release();
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbTurnout.class.getName());

}
