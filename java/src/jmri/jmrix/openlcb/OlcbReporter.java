package jmri.jmrix.openlcb;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.RailCom;
import jmri.RailComManager;
import jmri.implementation.AbstractIdTagReporter;
import org.openlcb.Connection;
import org.openlcb.ConsumerRangeIdentifiedMessage;
import org.openlcb.EventID;
import org.openlcb.EventState;
import org.openlcb.Message;
import org.openlcb.OlcbInterface;
import org.openlcb.ProducerConsumerEventReportMessage;
import org.openlcb.ProducerIdentifiedMessage;
import org.openlcb.implementations.EventTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Implement jmri.AbstractReporter for OpenLCB protocol.
 *
 * @author Bob Jacobsen Copyright (C) 2008, 2010, 2011
 * @author Balazs Racz Copyright (C) 2023
 * @since 5.3.5
 */
public class OlcbReporter extends AbstractIdTagReporter {

    /// How many bits does a reporter event range contain.
    private static final int REPORTER_BIT_COUNT = 16;
    /// Next bit in the event ID beyond the reporter event range.
    private static final long REPORTER_LSB = (1L << REPORTER_BIT_COUNT);
    /// Mask for the bits which are the actual report.
    private static final long REPORTER_EVENT_MASK = REPORTER_LSB - 1;

    /// When this bit is set, the report is an exit report.
    private static final long EXIT_BIT = (1L << 14);
    /// When this bit is set, the orientation of the locomotive is reverse, when clear it is normal.
    private static final long ORIENTATION_BIT = (1L << 15);

    /// Mask for the address bits of the reporter.
    private static final long ADDRESS_MASK = (1L << 14) - 1;
    /// The high bits of the address report for a DCC short address.
    private static final int HIBITS_SHORTADDRESS = 0x28;
    /// The high bits of the address report for a DCC consist address.
    private static final int HIBITS_CONSIST = 0x29;

    private OlcbAddress baseAddress;    // event ID for zero report
    private EventID baseEventID;
    private long baseEventNumber;
    private final OlcbInterface iface;
    private final Connection messageListener = new Receiver();

    EventTable.EventTableEntryHolder baseEventTableEntryHolder = null;

    public OlcbReporter(String prefix, String address, OlcbInterface iface) {
        super(prefix + "R" + address);
        this.iface = iface;
        init(address);
    }

    /**
     * Common initialization for both constructors.
     * <p>
     *
     */
    private void init(String address) {
        iface.registerMessageListener(messageListener);
        // build local addresses
        OlcbAddress a = new OlcbAddress(address);
        OlcbAddress[] v = a.split();
        if (v == null) {
            log.error("Did not find usable system name: {}", address);
            return;
        }
        switch (v.length) {
            case 1:
                baseAddress = v[0];
                baseEventID = baseAddress.toEventID();
                baseEventNumber = baseEventID.toLong();
                break;
            default:
                log.error("Can't parse OpenLCB Reporter system name: {}", address);
        }
    }

    /**
     * Helper function that will be invoked after construction once the properties have been
     * loaded. Used specifically for preventing double initialization when loading sensors from
     * XML.
     */
    void finishLoad() {
        if (baseEventTableEntryHolder != null) {
            baseEventTableEntryHolder.release();
            baseEventTableEntryHolder = null;
        }
        baseEventTableEntryHolder = iface.getEventTable().addEvent(baseEventID, getEventName());
        // Reports identified message.
        Message m = new ConsumerRangeIdentifiedMessage(iface.getNodeId(), getEventRangeID());
        iface.getOutputConnection().put(m, messageListener);
    }

    /**
     * Computes the 64-bit representation of the event range covered by this reporter.
     * This is defined for the Producer/Consumer Range identified messages in the OpenLCB
     * standards.
     * @return Event ID representing the event base address and the mask.
     */
    private EventID getEventRangeID() {
        long eventRange = baseEventNumber;
        if ((baseEventNumber & REPORTER_LSB) == 0) {
            eventRange |= REPORTER_EVENT_MASK;
        }
        byte[] contents = new byte[8];
        for (int i = 1; i <= 8; i++) {
            contents[8-i] = (byte)(eventRange & 0xff);
            eventRange >>= 8;
        }
        return new EventID(contents);
    }

    /**
     * Computes the display name of a given event to be entered into the Event Table.
     * @return user-visible string to represent this event.
     */
    private String getEventName() {
        String name = getUserName();
        if (name == null) name = mSystemName;
        return Bundle.getMessage("ReporterEventName", name);
    }

    /**
     * Updates event table entries when the user name changes.
     * @param s new user name
     * @throws BadUserNameException see {@link NamedBean}
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public void setUserName(String s) throws BadUserNameException {
        super.setUserName(s);
        if (baseEventTableEntryHolder != null) {
            baseEventTableEntryHolder.getEntry().updateDescription(getEventName());
        }
    }

    @Override
    public void dispose() {
        if (baseEventTableEntryHolder != null) {
            baseEventTableEntryHolder.release();
            baseEventTableEntryHolder = null;
        }
        iface.unRegisterMessageListener(messageListener);
        super.dispose();
    }

    /**
     * {@inheritDoc}
     *
     * Sorts by decoded EventID(s)
     */
    @CheckReturnValue
    @Override
    public int compareSystemNameSuffix(@Nonnull String suffix1, @Nonnull String suffix2, @Nonnull NamedBean n) {
        return OlcbAddress.compareSystemNameSuffix(suffix1, suffix2);
    }

    /**
     * State is always an integer, which is the numeric value from the last loco
     * address that we reported, or -1 if the last update was an exit.
     *
     * @return loco address number or -1 if the last message specified exiting
     */
    @Override
    public int getState() {
        return lastLoco;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int s) {
        lastLoco = s;
    }
    int lastLoco = -1;

    /**
     * Callback from the message decoder when a relevant event message arrives.
     * @param reportBits The bottom 14 bits of the event report. (THe top bits are already checked against our base event number)
     * @param isEntry true for entry, false for exit
     */
    private void handleReport(long reportBits, boolean isEntry) {
        // The extra notify with null is necessary to clear past notifications even if we have a new report.
        notify(null);
        if (!isEntry || ((reportBits & EXIT_BIT) != 0)) {
            return;
        }
        long addressBits = reportBits & ADDRESS_MASK;
        int address = 0;
        int hiBits = (int) ((addressBits >> 8) & 0x3f);
        int direction = (int) (reportBits & ORIENTATION_BIT);
        if (addressBits < 0x2800) {
            address = (int) addressBits;
        } else if (hiBits == HIBITS_SHORTADDRESS) {
            address = (int) (addressBits & 0xff);
        } else if (hiBits == HIBITS_CONSIST) {
            address = (int) (addressBits & 0x7f);
        }
        RailCom tag = (RailCom) InstanceManager.getDefault(RailComManager.class).provideIdTag("" + address);
        if (direction != 0) {
            tag.setOrientation(RailCom.ORIENTB);
        } else {
            tag.setOrientation(RailCom.ORIENTA);
        }
        notify(tag);
    }
    private class Receiver extends org.openlcb.MessageDecoder {
        @Override
        public void handleProducerConsumerEventReport(ProducerConsumerEventReportMessage msg, Connection sender) {
            long id = msg.getEventID().toLong();
            if ((id & ~REPORTER_EVENT_MASK) != baseEventNumber) {
                // Not for us.
                return;
            }
            handleReport(id & REPORTER_EVENT_MASK, true);
        }

        @Override
        public void handleProducerIdentified(ProducerIdentifiedMessage msg, Connection sender) {
            long id = msg.getEventID().toLong();
            if ((id & ~REPORTER_EVENT_MASK) != baseEventNumber) {
                // Not for us.
                return;
            }
            if (msg.getEventState() == EventState.Invalid) {
                handleReport(id & REPORTER_EVENT_MASK, false);
            } else if (msg.getEventState() == EventState.Valid) {
                handleReport(id & REPORTER_EVENT_MASK, true);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbReporter.class);

}
