package jmri.jmrix.openlcb;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.implementation.AbstractStringIO;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.EventID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.VersionedValueListener;

/**
 * Send a message to the OpenLCB/LCC network
 *
 * @author Bob Jacobsen   Copyright (C) 2024
 */
public class OlcbStringIO extends AbstractStringIO {

    OlcbAddress addrActive;    // PCER address - only one!
    
    private final OlcbInterface iface;
    private final CanSystemConnectionMemo memo;

    BitProducerConsumer pc;
    EventTable.EventTableEntryHolder activeEventTableEntryHolder = null;
    private static final boolean DEFAULT_IS_AUTHORITATIVE = true;
    private static final boolean DEFAULT_LISTEN = true;
    private static final int PC_DEFAULT_FLAGS = BitProducerConsumer.DEFAULT_FLAGS &
            (~BitProducerConsumer.LISTEN_INVALID_STATE);


    public OlcbStringIO(String prefix, String address, CanSystemConnectionMemo memo) {
        super(prefix + "C" + address);
        log.info("ctor with {} and {}", prefix, address);
        this.memo = memo;
        if (memo != null) { // greatly simplify testing
            this.iface = memo.get(OlcbInterface.class);
        } else {
            this.iface = null;
        }
        init(address);
    }

    /**
     * Common initialization for constructor(s).
     * <p>
     *
     */
    private void init(String address) {
        // build local addresses
        OlcbAddress a = new OlcbAddress(address, memo);
        OlcbAddress[] v = a.split(memo);
        if (v == null) {
            log.error("Did not find usable system name: {}", address);
            return;
        }
        switch (v.length) {
            case 1:
                addrActive = v[0];
                break;
            default:
                log.error("Can't parse OpenLCB StringIO system name: {}", address);
        }

    }

    /**
     * Helper function that will be invoked after construction once the properties have been
     * loaded. Used specifically for preventing double initialization when loading StringIO from
     * XML.
     */
    void finishLoad() {
        log.info("finishLoad runs");
        int flags = PC_DEFAULT_FLAGS;
        flags = OlcbUtils.overridePCFlagsFromProperties(this, flags);
        log.debug("StringIO Flags: default {} overridden {} listen bit {}", PC_DEFAULT_FLAGS, flags,
                    BitProducerConsumer.LISTEN_EVENT_IDENTIFIED);
        disposePc();

        pc = new BitProducerConsumer(iface, 
                                    addrActive.toEventID(), 
                                    BitProducerConsumer.nullEvent, 
                                    flags);

        // we don't listen to the VersionedValueListener to set state

        activeEventTableEntryHolder = iface.getEventTable().addEvent(addrActive.toEventID(), getEventName(true));
    }

    private void disposePc() {
        if (pc != null) {
            pc.release();
            pc = null;
        }
    }

    /**
     * Computes the display name of a given event to be entered into the Event Table.
     * @param isActive true for StringIO active, false for inactive.
     * @return user-visible string to represent this event.
     */
    public String getEventName(boolean isActive) {
        String name = getUserName();
        if (name == null) name = mSystemName;
        String msgName = isActive ? "SensorActiveEventName": "SensorInactiveEventName";
        return Bundle.getMessage(msgName, name);
    }

    public EventID getEventID(boolean isActive) {
        return addrActive.toEventID();
    }

    @Override
    @CheckReturnValue
    @Nonnull
    public String getRecommendedToolTip() {
        return addrActive.toDottedString();
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
    }

    /**
     * Request an update on status by sending an OpenLCB message.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (pc != null) {
            pc.resetToDefault();
            pc.sendQuery();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void sendStringToLayout(String value) throws JmriException {
        // Only sets the known string and fires listeners.
        setString(value);
    }

    /** {@inheritDoc} */
    @Override
    public int getMaximumLength() {
        return 242; // Event With Payload limit
    }

    /** {@inheritDoc} */
    @Override
    protected boolean cutLongStrings() {
        return true;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbStringIO.class);

}
