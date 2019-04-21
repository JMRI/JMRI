/**
 * Consist Manager for use with the LocoNetConsist class for the
 * consists it builds.
 *
 * @author Paul Bender Copyright (C) 2011
 */
package jmri.jmrix.loconet;

import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.implementation.AbstractConsistManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocoNetConsistManager extends AbstractConsistManager {

    private LocoNetSystemConnectionMemo memo = null;
    private boolean requestingUpdate = false;

    /**
     * Constructor - call the constructor for the superclass, and initialize the
     * consist reader thread, which retrieves consist information from the
     * command station
     *
     * @param lm the LocoNetSystemConnectionMemo to which this object is related
     */
    public LocoNetConsistManager(LocoNetSystemConnectionMemo lm) {
        super();
        this.memo = lm;
    }

    /**
     * This implementation does support command station assisted consists, so
     * return true.
     *
     */
    @Override
    public boolean isCommandStationConsistPossible() {
        return true;
    }

    /**
     * Does a CS consist require a separate consist address?
     *
     */
    @Override
    public boolean csConsistNeedsSeperateAddress() {
        return false;
    }

    /**
     * Add a new LocoNetConsist with the given address to
     * consistTable/consistList
     */
    @Override
    public Consist addConsist(LocoAddress address) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        if (consistTable.containsKey(address)) // no duplicates allowed.
        {
            return consistTable.get(address);
        }
        LocoNetConsist consist;
        consist = new LocoNetConsist((DccLocoAddress) address, memo);
        consistTable.put(address, consist);
        return consist;
    }

    /* request an update from the layout, loading
     * Consists from the command station.
     *
     * On a LocoNet command station, the consists are stored in the
     * slots in an array based tree.  Each node in a consist contains
     * a pointer to the "top" slot in the consist.  A top slot is
     * allowed to be a member of another consist.  When this occurs,
     * it is labeled as a "mid" locomotive.
     *
     * This function updates the list of consists by scanning the
     * slots and adding new "top" slot addresses and removing address
     * that are no longer "top" locomotives.
     */
    @Override
    public void requestUpdateFromLayout() {
        if (!shouldRequestUpdateFromLayout()) {
            return;
        }
        requestingUpdate = true;
        SlotManager sm = memo.getSlotManager();

        // in the first pass, check for consists top addresses in the
        // command station slots.
        for (int i = 0; i < 128; i++) {
            LocoNetSlot s = sm.slot(i);
            DccLocoAddress address = new DccLocoAddress(s.locoAddr(), LnThrottleManager.isLongAddress(s.locoAddr()));
            if (log.isDebugEnabled()) {
                log.debug(" Slot " + i + " Address " + address + " consist status " + LnConstants.CONSIST_STAT(s.consistStatus()));
            }
            if (s.consistStatus() == LnConstants.CONSIST_TOP || s.consistStatus() == LnConstants.CONSIST_MID) {
                // this is a consist top, add it to the list, if it is not there
                // already.
                if (!consistTable.containsKey(address)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Adding Consist with Address " + address + " due to command station read");
                    }
                    addConsist(address);
                    getConsist(address).add(address, true); // add the address to the consist.
                }
            }
        }

        // make a second pass, this time looking for locomotives in a consist.
        for (int i = 0; i < 128; i++) {
            LocoNetSlot s = sm.slot(i);
            DccLocoAddress address = new DccLocoAddress(s.locoAddr(), LnThrottleManager.isLongAddress(s.locoAddr()));
            if (log.isDebugEnabled()) {
                log.debug(" Slot " + i + " Address " + address + " consist status " + LnConstants.CONSIST_STAT(s.consistStatus()));
            }
            if (s.consistStatus() == LnConstants.CONSIST_SUB || s.consistStatus() == LnConstants.CONSIST_MID) {
                // this is a consist member, add it to the consist in the
                // slot which it has a pointer to (the slot pointer is stored in
                // the slot's speed).
                DccLocoAddress lead = new DccLocoAddress(sm.slot(s.speed()).locoAddr(), LnThrottleManager.isLongAddress(sm.slot(s.speed()).locoAddr()));
                getConsist(lead).add(address, s.isForward() == sm.slot(s.speed()).isForward());
            }
        }
        requestingUpdate = false;
    }

    @Override
    protected boolean shouldRequestUpdateFromLayout() {
        return !requestingUpdate;
    }
    private final static Logger log = LoggerFactory.getLogger(LocoNetConsistManager.class);
}
