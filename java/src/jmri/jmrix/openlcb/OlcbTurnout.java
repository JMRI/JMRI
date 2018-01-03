package jmri.jmrix.openlcb;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import jmri.NamedBean;
import jmri.Turnout;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.VersionedValueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;
import javax.annotation.CheckReturnValue;

/**
 * Turnout for OpenLCB connections.
 * <p>
 * State Diagram for read and write operations  (click to magnify):
 * <a href="doc-files/OlcbTurnout-State-Diagram.png"><img src="doc-files/OlcbTurnout-State-Diagram.png" alt="UML State diagram" height="50%" width="50%"></a>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010, 2011
 */

 /*
 * @startuml jmri/jmrix/openlcb/doc-files/OlcbTurnout-State-Diagram.png
 * CLOSED --> CLOSED: Event 1
 * THROWN --> CLOSED: Event 1
 * THROWN --> THROWN: Event 0
 * CLOSED --> THROWN: Event 0
 * [*] --> UNKNOWN
 * UNKNOWN --> CLOSED: Event 1\nEvent 1 Produced msg with valid set\nEvent 1 Consumed msg with valid set
 * UNKNOWN --> THROWN: Event 0\nEvent 1 Produced msg with valid set\nEvent 0 Consumed msg with valid set
 * state INCONSISTENT
 * @enduml
*/


public class OlcbTurnout extends jmri.implementation.AbstractTurnout {

    OlcbAddress addrThrown;   // go to thrown state
    OlcbAddress addrClosed;   // go to closed state
    final OlcbInterface iface;

    VersionedValueListener<Boolean> turnoutListener;
    BitProducerConsumer pc;
    EventTable.EventTableEntryHolder thrownEventTableEntryHolder = null;
    EventTable.EventTableEntryHolder closedEventTableEntryHolder = null;

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
     * Common initialization for constructor.
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
        if (thrownEventTableEntryHolder != null) {
            thrownEventTableEntryHolder.release();
            thrownEventTableEntryHolder = null;
        }
        if (closedEventTableEntryHolder != null) {
            closedEventTableEntryHolder.release();
            closedEventTableEntryHolder = null;
        }
        thrownEventTableEntryHolder = iface.getEventTable().addEvent(addrThrown.toEventID(), getEventName(true));
        closedEventTableEntryHolder = iface.getEventTable().addEvent(addrClosed.toEventID(), getEventName(false));
    }

    /**
     * Computes the display name of a given event to be entered into the Event Table.
     * @param isThrown true for thrown event, false for closed event
     * @return user-visible string to represent this event.
     */
    private String getEventName(boolean isThrown) {
        String name = getUserName();
        if (name == null) name = mSystemName;
        String msgName = isThrown ? "TurnoutThrownEventName": "TurnoutClosedEventName";
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
        if (thrownEventTableEntryHolder != null) {
            thrownEventTableEntryHolder.getEntry().updateDescription(getEventName(true));
        }
        if (closedEventTableEntryHolder != null) {
            closedEventTableEntryHolder.getEntry().updateDescription(getEventName(false));
        }
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
    @Override
    protected void forwardCommandChangeToLayout(int s) {
        if (s == Turnout.THROWN) {
            turnoutListener.setFromOwnerWithForceNotify(true);
            if (_activeFeedbackType == MONITORING) {
                newKnownState(THROWN);
            }
        } else if (s == Turnout.CLOSED) {
            turnoutListener.setFromOwnerWithForceNotify(false);
            if (_activeFeedbackType == MONITORING) {
                newKnownState(CLOSED);
            }
        }
    }

    @Override
    protected void turnoutPushbuttonLockout(boolean locked) {
        // TODO: maybe we could get another pair of events in the address and use that event pair
        // to perform a lockout change on the turnout decoder itself.
    }

    /*
     * since the events that drive a turnout can be whichever state a user
     * wants, the order of the event pair determines what is the 'closed' state
     */
    @Override
    public boolean canInvert() {
        return false;
    }

    @Override
    public void dispose() {
        if (thrownEventTableEntryHolder != null) {
            thrownEventTableEntryHolder.release();
            thrownEventTableEntryHolder = null;
        }
        if (closedEventTableEntryHolder != null) {
            closedEventTableEntryHolder.release();
            closedEventTableEntryHolder = null;
        }
        if (turnoutListener != null) turnoutListener.release();
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

    private final static Logger log = LoggerFactory.getLogger(OlcbTurnout.class);

}
