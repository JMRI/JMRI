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
import jmri.jmrix.loconet.SlotMapEntry.SlotType;

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
        notifyConsistListChanged();
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
        for (int i = 0; i < sm.getNumSlots(); i++) {
            if (sm.slot(i).getSlotType() == SlotType.LOCO) {
                LocoNetSlot s = sm.slot(i);
                DccLocoAddress address = new DccLocoAddress(s.locoAddr(), LnThrottleManager.isLongAddress(s.locoAddr()));
                if (log.isDebugEnabled()) {
                    log.debug(" Slot {} Address {} consist status {}", i, address, LnConstants.CONSIST_STAT(s.consistStatus()));
                }
                // FIELD REPORT: this used to also match CONSIST_MID here.
                // CONSIST_MID means a locomotive that has a lead above it
                // AND another member pointing to it below -- normal for
                // any chain of 3+ members. It is a MEMBER, never its own
                // top -- the second pass below already correctly handles
                // CONSIST_MID by following its pointer to its real lead
                // (line ~126). Treating it as a top here as well created
                // a phantom duplicate top-level consist for every
                // mid-chain member: confirmed in the field with a
                // 5-engine consist where the middle engine ended up
                // registered as BOTH a member of the real consist AND its
                // own separate one-engine "consist", corrupting JMRI's
                // consist list and the Consist Tool display.
                if (s.consistStatus() == LnConstants.CONSIST_TOP) {
                    // this is a consist top, add it to the list, if it is not there
                    // already.
                    //
                    // FIELD REPORT: also guard against this address
                    // already being tracked as a MEMBER of some other
                    // consist. A locomotive mid-way through being linked
                    // into a consist can briefly still report CONSIST_TOP
                    // on the wire (it hasn't been linked yet) -- if a
                    // layout rescan lands in that exact window, it
                    // registers a phantom standalone consist for an
                    // address our own in-progress build is about to claim
                    // as a member, corrupting the consist list (confirmed
                    // in the field: an engine ended up listed under two
                    // different consists at once). Skip if some other
                    // already-tracked consist already claims it.
                    if (!consistTable.containsKey(address) && !isAlreadyConsistMember(address)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Adding Consist with Address {} due to command station read", address);
                        }
                        addConsist(address);
                        getConsist(address).add(address, true); // add the address to the consist.
                    }
                }
            }
        }

        // make a second pass, this time looking for locomotives in a consist.
        for (int i = 0; i < sm.getNumSlots(); i++) {
            if (sm.slot(i).getSlotType() == SlotType.LOCO) {
                LocoNetSlot s = sm.slot(i);
                DccLocoAddress address = new DccLocoAddress(s.locoAddr(), LnThrottleManager.isLongAddress(s.locoAddr()));
                if (log.isDebugEnabled()) {
                    log.debug(" Slot {} Address {} consist status {}", i, address, LnConstants.CONSIST_STAT(s.consistStatus()));
                }
                if (s.consistStatus() == LnConstants.CONSIST_SUB || s.consistStatus() == LnConstants.CONSIST_MID) {
                    // this is a consist member, add it to the consist in the
                    // slot which it has a pointer to (the slot pointer is stored in
                    // the slot's speed).
                    //
                    // FIELD REPORT: this used to trust that pointer blindly.
                    // On a command station that's had a lot of addresses
                    // reused across many separate consist builds (confirmed
                    // in the field after a long test session), a slot's
                    // "speed" pointer can still reference a slot number that
                    // USED to be some earlier, now-defunct consist's lead --
                    // the pointer itself doesn't get cleared just because
                    // that old lead relationship ended. Blindly following it
                    // created a phantom consist keyed to whatever address now
                    // happens to occupy that stale slot number, with this
                    // member wrongly attached to it (observed: a phantom
                    // consist "8002" whose only member was "8006", neither
                    // of which had anything to do with each other -- 8002
                    // just happened to be sitting in the slot that some
                    // earlier test's pointer still referenced). Now verifies
                    // the pointed-to slot is ACTUALLY a live top/mid right
                    // now before trusting it as this member's real lead.
                    LocoNetSlot leadSlot = sm.slot(s.speed());
                    if (leadSlot.consistStatus() == LnConstants.CONSIST_TOP || leadSlot.consistStatus() == LnConstants.CONSIST_MID) {
                        DccLocoAddress lead = new DccLocoAddress(leadSlot.locoAddr(), LnThrottleManager.isLongAddress(leadSlot.locoAddr()));
                        getConsist(lead).add(address, s.isForward() == leadSlot.isForward());
                    } else if (log.isDebugEnabled()) {
                        log.debug("Slot {} (address {}) claims consist member status but its lead pointer (slot {}) isn't currently a top/mid -- stale pointer, ignoring",
                                i, address, s.speed());
                    }
                }
            }
        }
        requestingUpdate = false;
    }

    @Override
    protected boolean shouldRequestUpdateFromLayout() {
        return !requestingUpdate;
    }

    /**
     * Is address already tracked as a MEMBER of some other already-known
     * consist? See the field report on the CONSIST_TOP check above --
     * used to avoid registering a phantom standalone consist for an
     * address that's really just mid-link into an existing one.
     */
    private boolean isAlreadyConsistMember(DccLocoAddress address) {
        for (Consist c : consistTable.values()) {
            if (!address.equals(c.getConsistAddress()) && c.contains(address)) {
                return true;
            }
        }
        return false;
    }

    private static final Logger log = LoggerFactory.getLogger(LocoNetConsistManager.class);
}
