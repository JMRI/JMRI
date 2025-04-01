package jmri.jmrix.openlcb;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import jmri.JmriException;
import jmri.NamedBean;
import jmri.implementation.AbstractStringIO;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.Connection;
import org.openlcb.EventID;
import org.openlcb.MessageDecoder;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.EventTable;

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
    private static final int PC_DEFAULT_FLAGS = BitProducerConsumer.DEFAULT_FLAGS &
            (~BitProducerConsumer.LISTEN_INVALID_STATE);


    public OlcbStringIO(String prefix, String address, CanSystemConnectionMemo memo) {
        super(prefix + "C" + address);
        log.trace("ctor with {} and {}", prefix, address);
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

        iface.registerMessageListener(new EWPListener());

    }

    /**
     * Helper function that will be invoked after construction once the properties have been
     * loaded. Used specifically for preventing double initialization when loading StringIO from
     * XML.
     */
    void finishLoad() {
        log.trace("finishLoad runs");
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
     * @param isActive left over from interface for Turnout and Sensor, this is ignored
     * @return user-visible string to represent this event.
     */
    public String getEventName(boolean isActive) {
        String name = getUserName();
        if (name == null) name = mSystemName;
        String msgName = "StringIOEventName";
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
        // Does not set the known value immediately.  Instead, it waits
        // for the OpenLCB message to be received on the network, and reacts then.
        // This is JMRI's standard MONITORING feedback.

        // Send the message to the network
        iface.getOutputConnection().put(
            new ProducerConsumerEventReportMessage(iface.getNodeId(), 
                    getEventID(true), 
                    value.getBytes(java.nio.charset.StandardCharsets.UTF_8)), 
            null);

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

    class EWPListener extends MessageDecoder {
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender){
            if (!msg.getEventID().equals(getEventID(true))) {
                return;
            }
            // found contents, set the string on Swing thread
            jmri.util.ThreadingUtil.runOnGUI( () -> {
                try {
                    setString(new String(msg.getPayloadArray(), java.nio.charset.StandardCharsets.UTF_8));
                } catch (Exception e) {
                    log.warn("EWP processing got exception", e);
                }
            });
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbStringIO.class);

}
