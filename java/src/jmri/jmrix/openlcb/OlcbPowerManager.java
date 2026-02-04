package jmri.jmrix.openlcb;

import jmri.PowerManager;
import jmri.managers.AbstractPowerManager;
import jmri.jmrix.can.CanSystemConnectionMemo;

import org.openlcb.EventID;
import org.openlcb.OlcbInterface;
import org.openlcb.implementations.BitProducerConsumer;
import org.openlcb.implementations.EventTable;
import org.openlcb.implementations.VersionedValueListener;

/**
 * Extend jmri.managers.AbstractPowerManager for OpenLCB controls.
 *
 * @author Bob Jacobsen Copyright (C) 2025
 */
public final class OlcbPowerManager extends AbstractPowerManager<CanSystemConnectionMemo> {

    // The following are the Well Known power events defined by the Standard
    OlcbAddress addrOn  = new OlcbAddress(new EventID("01.00.00.00.00.00.FF.FE"));
    OlcbAddress addrOff = new OlcbAddress(new EventID("01.00.00.00.00.00.FF.FF"));

    private final OlcbInterface iface;

    VersionedValueListener<Boolean> powerListener;
    BitProducerConsumer pc;
    EventTable.EventTableEntryHolder onEventTableEntryHolder = null;
    EventTable.EventTableEntryHolder offEventTableEntryHolder = null;
    private static final int PC_DEFAULT_FLAGS = BitProducerConsumer.DEFAULT_FLAGS &
            (~BitProducerConsumer.LISTEN_INVALID_STATE);

    public OlcbPowerManager(CanSystemConnectionMemo memo) {
        super(memo);
        this.iface = memo.get(OlcbInterface.class);

        int flags = PC_DEFAULT_FLAGS;
        log.debug("Power Manager Flags: default {} overridden {} listen bit {}", PC_DEFAULT_FLAGS, flags,
                    BitProducerConsumer.LISTEN_EVENT_IDENTIFIED);

        pc = new BitProducerConsumer(iface, addrOn.toEventID(),
                addrOff.toEventID(), flags);
        powerListener = new VersionedValueListener<Boolean>(pc.getValue()) {
            @Override
            public void update(Boolean value) {
                try {
                    OlcbPowerManager.super.setPower(value ? PowerManager.ON : PowerManager.OFF);
                } catch (jmri.JmriException e) {
                    log.error("Unexpected exception", e);
                }
            }
        };

        onEventTableEntryHolder = iface.getEventTable().addEvent(addrOn.toEventID(), "PowerManager:on");
        offEventTableEntryHolder = iface.getEventTable().addEvent(addrOff.toEventID(), "PowerManager:off");
    }

    
    @Override
    public void setPower(int s) {
        if (s == PowerManager.ON) {
            powerListener.setFromOwnerWithForceNotify(true);
        } else if (s == PowerManager.OFF) {
            powerListener.setFromOwnerWithForceNotify(false);
        }
        try {
            super.setPower(s);
        } catch (jmri.JmriException e) {
            log.error("Unexpected exception", e);
        }
    }

    @Override
    public void dispose() {
        if (powerListener != null) {
            powerListener.release();
            powerListener = null;
        }
        if (pc != null) {
            pc.release();
            pc = null;
        }
 
        if (onEventTableEntryHolder != null) {
            onEventTableEntryHolder.release();
            onEventTableEntryHolder = null;
        }
        
        if (offEventTableEntryHolder != null) {
            offEventTableEntryHolder.release();
            offEventTableEntryHolder = null;
        }
        
   }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbPowerManager.class);

}
