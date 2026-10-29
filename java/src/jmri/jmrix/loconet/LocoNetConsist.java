package jmri.jmrix.loconet;

import java.util.ArrayList;

import jmri.Consist;
import jmri.ConsistListener;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.ThrottleListener;

/**
 * LocoNetConsist.java
 * This is the Consist definition for a consist on a LocoNet system.
 * It uses the LocoNet specific commands to build a consist.
 * @author Paul Bender Copyright (C) 2011
 */
public class LocoNetConsist extends jmri.implementation.DccConsist implements SlotListener, ThrottleListener {

    private SlotManager slotManager = null;
    private LnTrafficController trafficController = null;
    private jmri.jmrix.AbstractThrottleManager throttleManager = null;
    private LocoNetSlot leadSlot = null;

    private ArrayList<DccLocoAddress> needToWrite = null;
    // Mirrors needToWrite, but for pending removals -- see the field
    // report on removeFromCSConsist() below for why this exists.
    private ArrayList<DccLocoAddress> needToRemove = null;

    // Address the current pending slot request is for. notifyChangedSlot()
    // checks a response against this before acting on it -- a single
    // throttle can hold more than one address on a live LocoNet bus, so a
    // mismatched response must never be linked in as the requested engine.
    private DccLocoAddress pendingSlotAddress = null;
    // Bounds the mismatch-triggered retry in unexpectedSlotAddress()
    // below so a genuinely dead/unreachable address can't retry
    // forever. Reset to 0 on any correctly-matched response.
    private int consecutiveSlotMismatches = 0;
    private static final int MAX_CONSECUTIVE_SLOT_MISMATCHES = 5;

    // State Machine states
    static final int IDLESTATE = 0;
    static final int LEADREQUESTSTATE = 1;
    static final int LINKSTAGEONESTATE = 2;
    static final int LINKSTAGETWOSTATE = 4;
    static final int LINKSTAGETHREESTATE = 8;
    static final int UNLINKSTAGEONESTATE = 16;
    static final int FREESLOTSTATE = 32;

    private int consistRequestState = IDLESTATE;

    // Initialize a consist for the specific address
    // the Default consist type for LocoNet is a Command
    // Station Consist.
    public LocoNetConsist(int address, LocoNetSystemConnectionMemo lm) {
        super(address);
        this.slotManager = lm.getSlotManager();
        this.trafficController = lm.getLnTrafficController();
        this.throttleManager = (jmri.jmrix.AbstractThrottleManager) lm.getThrottleManager();
        consistRequestState = LEADREQUESTSTATE;
        consistType = Consist.CS_CONSIST;
        needToWrite = new ArrayList<>();
        needToRemove = new ArrayList<>();
        throttleManager.requestThrottle(consistAddress, LocoNetConsist.this, false);
    }

    // Initialize a consist for the specific address
    // the Default consist type for LocoNet is a Command
    // Station Consist.
    public LocoNetConsist(DccLocoAddress address, LocoNetSystemConnectionMemo lm) {
        super(address);
        this.slotManager = lm.getSlotManager();
        this.trafficController = lm.getLnTrafficController();
        this.throttleManager = (jmri.jmrix.AbstractThrottleManager) lm.getThrottleManager();
        consistRequestState = LEADREQUESTSTATE;
        consistType = Consist.CS_CONSIST;
        needToWrite = new ArrayList<>();
        needToRemove = new ArrayList<>();
        throttleManager.requestThrottle(consistAddress, LocoNetConsist.this, false);
    }

    // Set the Consist Type
    @Override
    public void setConsistType(int type) {
        switch (type) {
            case Consist.ADVANCED_CONSIST:
            case Consist.CS_CONSIST:
                consistType = type;
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(new DccLocoAddress(0, false), ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     * Is this address allowed?
     * On LocoNet systems, All addresses can be used in a Universal Consist
     * and only 0 is not allowed in Advanced Consists.
     * {@inheritDoc}
     */
    @Override
    public boolean isAddressAllowed(DccLocoAddress address) {
        return consistType == Consist.CS_CONSIST || (address.getNumber() != 0);
    }

    /**
     * Is there a size limit for this consist?
     * @return -1 (no limit) for
     * both CS and Advanced Consists,
     * 0 for any other consist type.
     */
    @Override
    public int sizeLimit() {
        switch (consistType) {
            case ADVANCED_CONSIST:
            case CS_CONSIST:
                return -1;
            default:
                return 0;
        }
    }

    // does the consist contain the specified address?
    @Override
    public boolean contains(DccLocoAddress address) {
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            return consistList.contains(address);
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    // get the relative direction setting for a specific
    // locomotive in the consist
    @Override
    public boolean getLocoDirection(DccLocoAddress address) {
        log.debug("consist {} obtaining direction for {} Consist List Size {}",
            consistAddress, address, consistList.size());
        if (consistType == ADVANCED_CONSIST || consistType == CS_CONSIST) {
            if (address == consistAddress) {
                return true;
            }
            if (consistList.contains(address)) {
                return consistDir.getOrDefault(address, false);
            } else {
                return (true);
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(address, ConsistListener.NotImplemented);
        }
        return false;
    }

    /**
     * Add an Address to the internal Consist list object.
     */
    private synchronized void addToConsistList(DccLocoAddress locoAddress, boolean directionNormal) {
        if (!(consistList.contains(locoAddress))) {
            consistList.add(locoAddress);
        }
        if (consistDir.containsKey(locoAddress)) {
            consistDir.remove(locoAddress);
        }
        consistDir.put(locoAddress, directionNormal);
    }

    /**
     * Remove an address from the internal Consist list object.
     */
    private synchronized void removeFromConsistList(DccLocoAddress locoAddress) {
        // Can already be null if dispose() ran first -- this is also
        // reached asynchronously from LnThrottleManager's retry thread.
        if (consistDir == null || consistList == null) {
            return;
        }
        consistDir.remove(locoAddress);
        consistList.remove(locoAddress);
    }

    /**
     * Add a Locomotive to a Consist.
     *
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal if the locomotive is traveling
     *        the same direction as the consist, false otherwise
     */
    @Override
    public synchronized void add(DccLocoAddress locoAddress, boolean directionNormal) {
        if (locoAddress == consistAddress) {
            // this is required for command station consists on LocoNet.
            addToConsistList(locoAddress, directionNormal);
            notifyConsistListeners(locoAddress, ConsistListener.OPERATION_SUCCESS);
        } else if (consistType == ADVANCED_CONSIST) {
            if (consistList.contains(locoAddress)) {
                // we are changing the direction, so remove first,
                // then add
                removeFromAdvancedConsist(locoAddress);
            }
            addToConsistList(locoAddress, directionNormal);
            if (leadSlot == null || consistRequestState != IDLESTATE) {
                needToWrite.add(locoAddress);
            } else {
                addToAdvancedConsist(locoAddress, directionNormal);
            }
        } else if (consistType == CS_CONSIST) {
            if (consistList.contains(locoAddress)) {
                // we are changing the direction, so remove first,
                // then add
                removeFromCSConsist(locoAddress);
            }
            addToConsistList(locoAddress, directionNormal);
            if (leadSlot == null || consistRequestState != IDLESTATE) {
                needToWrite.add(locoAddress);
            } else {
                addToCSConsist(locoAddress, directionNormal);
            }
        } else {
            log.error("Consist Type Not Supported");
            notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
        }
    }

    private synchronized void delayedAdd() {
        if (consistList == null) {
            // This consist was dispose()'d (deleted) while this add was
            // still queued behind an in-flight slot request -- nothing
            // left to add it to. See the field report on the
            // "missing lead slot" branch in unlinkSlots().
            return;
        }
        DccLocoAddress locoAddress = needToWrite.get(0);
        if (consistType == ADVANCED_CONSIST) {
            addToAdvancedConsist(locoAddress, getLocoDirection(locoAddress));
        } else if (consistType == CS_CONSIST) {
            addToCSConsist(locoAddress, getLocoDirection(locoAddress));
        }
        needToWrite.remove(locoAddress);
    }

    /**
     * Process one queued removal. See the field report on
     * removeFromCSConsist() for why removals need to be queued rather
     * than fired immediately -- this drains that queue the same way
     * delayedAdd() drains needToWrite.
     */
    private synchronized void delayedRemove() {
        if (needToRemove.isEmpty()) {
            return;
        }
        // The consist's own address, if queued, must always drain LAST --
        // freeing its slot before every other queued follower is actually
        // unlinked (not just requested) would orphan their real LocoNet
        // link. Prefer any non-lead entry first.
        DccLocoAddress locoAddress = needToRemove.get(0);
        if (locoAddress.equals(consistAddress) && needToRemove.size() > 1) {
            for (DccLocoAddress candidate : needToRemove) {
                if (!candidate.equals(consistAddress)) {
                    locoAddress = candidate;
                    break;
                }
            }
        }
        needToRemove.remove(locoAddress);
        if (consistType == ADVANCED_CONSIST) {
            removeFromAdvancedConsist(locoAddress);
        } else if (consistType == CS_CONSIST) {
            removeFromCSConsist(locoAddress);
        }
    }

    /**
     * Called at every point where a slot operation has just finished and
     * consistRequestState is (or is about to become) IDLESTATE. Drains
     * needToRemove ahead of needToWrite -- queued deletes should not sit
     * behind queued adds -- and only leaves the state machine idle once
     * both queues are empty.
     */
    private synchronized void processQueuedWork() {
        if (needToRemove.isEmpty() && needToWrite.isEmpty()) {
            consistRequestState = IDLESTATE;
            return;
        }
        // Deferred via invokeLater() -- draining synchronously here can
        // recurse indefinitely and stack-overflow, since requestThrottle()
        // calls back synchronously when a throttle is already cached, with
        // no base case to stop the chain.
        javax.swing.SwingUtilities.invokeLater(() -> {
            synchronized (LocoNetConsist.this) {
                // Must reset to IDLESTATE before draining the next item --
                // otherwise it sees "still busy" and re-queues instead of
                // processing, looping forever without ever completing.
                consistRequestState = IDLESTATE;
                if (!needToRemove.isEmpty()) {
                    delayedRemove();
                } else if (!needToWrite.isEmpty()) {
                    delayedAdd();
                }
            }
        });
    }

    /**
     * Restore a Locomotive to a Consist, but don't write to
     * the command station.
     * This is used for restoring the consist
     * from a file or adding a consist read from the command station.
     *
     * @param locoAddress     the Locomotive address to add to the locomotive
     * @param directionNormal True if the locomotive is traveling
     *        the same direction as the consist, false otherwise
     */
    @Override
    public synchronized void restore(DccLocoAddress locoAddress, boolean directionNormal) {
        switch (consistType) {
            case ADVANCED_CONSIST:
            case CS_CONSIST:
                addToConsistList(locoAddress, directionNormal);
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     *  Remove a Locomotive from this Consist.
     *
     *  @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    public synchronized void remove(DccLocoAddress locoAddress) {
        switch (consistType) {
            case ADVANCED_CONSIST:
                removeFromAdvancedConsist(locoAddress);
                removeFromConsistList(locoAddress);
                break;
            case CS_CONSIST:
                removeFromCSConsist(locoAddress);
                removeFromConsistList(locoAddress);
                break;
            default:
                log.error("Consist Type Not Supported");
                notifyConsistListeners(locoAddress, ConsistListener.NotImplemented);
                break;
        }
    }

    /**
     *  Add a Locomotive to an Advanced Consist.
     *
     *  @param locoAddress     the Locomotive address to add to the locomotive
     *  @param directionNormal True if the locomotive is traveling
     *        the same direction as the consist, false otherwise
     */
    @Override
    protected synchronized void addToAdvancedConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add Locomotive {} to advanced consist {} With Direction Normal {}.",
            locoAddress, consistAddress, directionNormal);
        //set the value in the roster entry for CV19
        setRosterEntryCVValue(locoAddress);
        consistRequestState = LINKSTAGEONESTATE;
        throttleManager.requestThrottle(locoAddress, this, false);
    }

    /**
     *  Remove a Locomotive from an Advanced Consist
     *  @param locoAddress is the Locomotive address to add to the locomotive
     */
    @Override
    protected synchronized void removeFromAdvancedConsist(DccLocoAddress locoAddress) {
        log.debug(" Remove Locomotive {} from advanced consist {}", locoAddress, consistAddress);
        //reset the value in the roster entry for CV19
        resetRosterEntryCVValue(locoAddress);
        slotManager.slotFromLocoAddress(locoAddress.getNumber(), this);
        consistRequestState = UNLINKSTAGEONESTATE;
    }

    /**
     *  Add a Locomotive to a LocoNet Universal Consist.
     *  @param locoAddress is the Locomotive address to add to the locomotive
     *  @param directionNormal is True if the locomotive is traveling
     *        the same direction as the consist, or false otherwise.
     */
    private synchronized void addToCSConsist(DccLocoAddress locoAddress, boolean directionNormal) {
        log.debug("Add Locomotive {} to Standard Consist {} With Direction Normal {}.",
            locoAddress, consistAddress, directionNormal);
        if(consistList.size()<=1 && locoAddress.equals(consistAddress)){
          // there is only one address in this consist, no reason to link.
          // Must still call processQueuedWork() here -- add()'s reference
          // equality check means the lead's own address reaches this
          // no-op branch almost every time, and without this the queue
          // chain dead-ends before any real follower is ever processed.
          notifyConsistListeners(locoAddress,ConsistListener.OPERATION_SUCCESS);
          processQueuedWork();
          return;
        }
        pendingSlotAddress = locoAddress;
        throttleManager.requestThrottle(locoAddress, this, false);
        // skip right to stage 2, we do not need to status edit.
        consistRequestState = LINKSTAGETWOSTATE;
    }

    /**
     *  Remove a Locomotive from a LocoNet Universal Consist.
     *  @param locoAddress is the Locomotive address to add to the locomotive.
     */
    public synchronized void removeFromCSConsist(DccLocoAddress locoAddress) {
        log.debug("Remove Locomotive {} from Standard Consist {}.", locoAddress, consistAddress);
        // dispose() removes every member synchronously with no pacing, all
        // sharing this one consistRequestState field -- without queuing
        // here (like add() already does), concurrent slot requests
        // overwrite each other and most removals are silently lost.
        if (consistRequestState != IDLESTATE) {
            needToRemove.add(locoAddress);
            return;
        }
        if(consistList == null || (locoAddress.equals(consistAddress) && (consistList.isEmpty() || consistList.size()==1))){
          // there is only one address in this consist, no reason to
          // unlink -- but the slot is still marked In Use/Common from
          // when the consist was built, so fetch it and free it
          // directly rather than leaving it stuck (see FREESLOTSTATE
          // in notifyChangedSlot()).
          //
          // consistList == null covers the lead's own deferred removal
          // arriving after dispose()'s loop has already finished and
          // nulled it out; consistList.isEmpty() covers the same "nothing
          // left to unlink against" case but before the null reset --
          // removeFromConsistList() strips an address synchronously the
          // instant remove() is called, regardless of whether the real
          // async unlink completed, so this can read back empty before
          // the lead's own deferred removal is retried. Both need the
          // same "just free the slot" treatment as the sole-member case.
          pendingSlotAddress = locoAddress;
          slotManager.slotFromLocoAddress(locoAddress.getNumber(), this);
          consistRequestState = FREESLOTSTATE;
          return;
        }
        if (locoAddress.equals(consistAddress)) {
          // leadSlot is cached once at construction and never updated, so
          // removing the lead's own address while other members are still
          // tracked would fall through to the generic unlink path below
          // and resolve BOTH lead and follow to that same slot object --
          // tripping unlinkSlots()'s lead==follow guard, which sends no
          // LocoNet message and never retries, permanently stranding the
          // slot as "In Use". Deferring here until the lead is genuinely
          // the last member left (the branch above) is the only safe
          // order: freeing its slot while followers are still linked to
          // it by slot number on the real hardware would corrupt the
          // consist there even though JMRI's own bookkeeping looks fine.
          needToRemove.add(locoAddress);
          return;
        }
        pendingSlotAddress = locoAddress;
        slotManager.slotFromLocoAddress(locoAddress.getNumber(), this);
        consistRequestState = UNLINKSTAGEONESTATE;
    }

    /**
     * create and send a message to link two slots
     * @param lead is the slot which is the leader
     * @param follow is the slot which will follow the leader
     */
    private void linkSlots(LocoNetSlot lead, LocoNetSlot follow) {
        LocoNetMessage msg;
        if (lead != follow) {
            if (slotManager.getLoconetProtocol() == LnConstants.LOCONETPROTOCOL_TWO) {
                msg = new LocoNetMessage(6);
                int dest1 = follow.getSlot() / 128;
                int dest2 = follow.getSlot() % 128;
                int src1 = lead.getSlot() / 128;
                int src2 = lead.getSlot() % 128;
                msg.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
                msg.setElement(1, dest1 | 0b00111000);
                msg.setElement(2, dest2 & 0x7F);
                msg.setElement(3, src1  | 0b01000000);
                msg.setElement(4, src2 & 0x7F);
            } else {
                msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_LINK_SLOTS);
                msg.setElement(1, follow.getSlot());
                msg.setElement(2, lead.getSlot());
            }
            trafficController.sendLocoNetMessage(msg);
        } else {
          // lead == follow
          // this is an error, notify the consist listeners.
          follow.removeSlotListener(this);
          notifyConsistListeners(new DccLocoAddress(follow.locoAddr(),
                throttleManager.canBeLongAddress(follow.locoAddr())),
                ConsistListener.CONSIST_ERROR);
        }
        processQueuedWork();
    }

    /**
     * create and send a message to unlink two slots
     * @param lead is the slot which is the leader
     * @param follow is the slot which was following the leader
     */
    private void unlinkSlots(LocoNetSlot lead, LocoNetSlot follow) {
        if (lead == null || follow == null) {
            // Can happen for a consist JMRI rediscovered from the layout
            // (requestUpdateFromLayout()) whose lead slot never resolved
            // -- e.g. the command station never answered the lead's slot
            // request. There's nothing to build a valid unlink message
            // with, but still free whatever slot IS known rather than
            // crashing with an NPE and leaving it stuck In Use.
            log.warn("unlinkSlots(): missing {} slot for consist {} -- cannot send unlink, freeing what's known",
                    lead == null ? "lead" : "follow", consistAddress);
            if (follow != null) {
                trafficController.sendLocoNetMessage(follow.writeStatus(LnConstants.LOCO_FREE));
                follow.removeSlotListener(this);
            }
            processQueuedWork();
            return;
        }
        LocoNetMessage msg;
        if (lead != follow) {
            if (slotManager.getLoconetProtocol() == LnConstants.LOCONETPROTOCOL_TWO) {
                msg = new LocoNetMessage(6);
                int src1 = lead.getSlot() / 128;
                int src2 = lead.getSlot() % 128;
                int dest1 = follow.getSlot() / 128;
                int dest2 = follow.getSlot() % 128;
                msg.setOpCode(LnConstants.OPC_EXP_SLOT_MOVE_RE_OPC_IB2_SPECIAL);
                msg.setElement(3, src1 | 0b01010000);
                msg.setElement(4, src2 & 0x7F);
                msg.setElement(1, dest1 | 0b00111000);
                msg.setElement(2, dest2 & 0x7F);
            } else {
                msg = new LocoNetMessage(4);
                msg.setOpCode(LnConstants.OPC_UNLINK_SLOTS);
                msg.setElement(1, follow.getSlot());
                msg.setElement(2, lead.getSlot());
            }
            trafficController.sendLocoNetMessage(msg);
            // Unlinking only breaks the LocoNet slot link -- it does not
            // touch the follower's own slot status, which otherwise stays
            // In Use/Common forever after being removed from a consist.
            // Match what the Slot Monitor's own manual Free button sends
            // (see SlotMonDataModel.setValueAt(), DISPCOLUMN case).
            trafficController.sendLocoNetMessage(follow.writeStatus(LnConstants.LOCO_FREE));
        } else {
          // lead == follow
          // this is an error, notify the consist listeners.
          follow.removeSlotListener(this);
          notifyConsistListeners(new DccLocoAddress(follow.locoAddr(),
                throttleManager.canBeLongAddress(follow.locoAddr())),
                ConsistListener.CONSIST_ERROR | ConsistListener.DELETE_ERROR );
        }
        processQueuedWork();
    }

    /**
     * Free a slot directly, with no unlink -- used when deleting a
     * command-station consist down to its last (lead) engine, where
     * there's nothing left linked to unlink but the slot itself is
     * still marked In Use/Common from when the consist was built.
     * @param s the slot to free
     */
    private void freeSlot(LocoNetSlot s) {
        trafficController.sendLocoNetMessage(s.writeStatus(LnConstants.LOCO_FREE));
        s.removeSlotListener(this);
        notifyConsistListeners(new DccLocoAddress(s.locoAddr(),
                throttleManager.canBeLongAddress(s.locoAddr())),
                ConsistListener.OPERATION_SUCCESS);
        processQueuedWork();
    }

    private void setDirection(LocoNetThrottle t) {
        log.debug("consist {} set direction for {}", consistAddress, t.getLocoAddress());
        // send a command to set the direction
        // of the locomotive in the slot.
        boolean directionNormal = getLocoDirection((DccLocoAddress) t.getLocoAddress());
        if (directionNormal) {
            t.setIsForward(leadSlot.isForward());
        } else {
            t.setIsForward(!leadSlot.isForward());
        }

        consistRequestState = LINKSTAGETWOSTATE;
    }

    private void setSlotModeAdvanced(LocoNetSlot s) {
        // set the slot so that it can be an advanced consist
        int oldstatus = s.slotStatus();
        int newstatus = oldstatus | LnConstants.STAT1_SL_SPDEX;
        trafficController.sendLocoNetMessage(s.writeStatus(newstatus));
    }

    /**
     * Verify a slot response's actual address matches what was
     * requested (pendingSlotAddress) before acting on it as though it
     * were the follower/lead engine we asked for.
     *
     * A single physical throttle can hold more than one address on a live
     * LocoNet bus, so a slot response isn't guaranteed to match what was
     * requested -- acting on it unchecked can link the wrong engine in.
     *
     * @return true if the response was unexpected and should NOT be
     *         acted on (already cleaned up and queue continued)
     */
    private boolean unexpectedSlotAddress(LocoNetSlot s) {
        if (pendingSlotAddress == null) {
            return false;
        }
        if (s.locoAddr() != pendingSlotAddress.getNumber()) {
            // Re-queue rather than drop on a mismatch -- for removals
            // specifically, dropping left JMRI's bookkeeping (already
            // updated synchronously in remove()) out of sync with the
            // real hardware, reporting an engine freed while it stayed
            // linked. Bounded by consecutiveSlotMismatches so a dead
            // address can't retry forever.
            DccLocoAddress missed = pendingSlotAddress;
            boolean wasRemoval = (consistRequestState == UNLINKSTAGEONESTATE || consistRequestState == FREESLOTSTATE);
            log.warn("notifyChangedSlot(): requested slot for {} but got slot {} for address {} instead -- ignoring rather than linking/freeing the wrong engine",
                    missed, s.getSlot(), s.locoAddr());
            s.removeSlotListener(this);
            pendingSlotAddress = null;
            if (consecutiveSlotMismatches < MAX_CONSECUTIVE_SLOT_MISMATCHES) {
                consecutiveSlotMismatches++;
                if (wasRemoval) {
                    needToRemove.add(0, missed);
                } else {
                    needToWrite.add(0, missed);
                }
            } else {
                log.error("notifyChangedSlot(): giving up on {} after {} consecutive mismatched slot responses",
                        missed, consecutiveSlotMismatches);
                notifyConsistListeners(missed, ConsistListener.CONSIST_ERROR);
            }
            processQueuedWork();
            return true;
        }
        pendingSlotAddress = null;
        consecutiveSlotMismatches = 0;
        return false;
    }

    // slot listener interface functions
    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        log.debug("Notified slot {} changed with mode {} slot consist state: {}",
            s.getSlot(), consistRequestState, LnConstants.CONSIST_STAT(s.consistStatus()));
        switch (consistRequestState) {
            case LEADREQUESTSTATE:
                leadSlot = s;
                consistRequestState = IDLESTATE;
                break;
            case LINKSTAGEONESTATE:
                s.addSlotListener(this);
                setSlotModeAdvanced(s);
                consistRequestState = LINKSTAGETWOSTATE;
                break;
            case LINKSTAGETWOSTATE:
                if (!unexpectedSlotAddress(s)) {
                    linkSlots(leadSlot, s);
                }
                break;
            case UNLINKSTAGEONESTATE:
                if (!unexpectedSlotAddress(s)) {
                    unlinkSlots(leadSlot, s);
                }
                break;
            case FREESLOTSTATE:
                if (!unexpectedSlotAddress(s)) {
                    freeSlot(s);
                }
                break;
            default:
                s.removeSlotListener(this);
                notifyConsistListeners(new DccLocoAddress(s.locoAddr(),
                        throttleManager.canBeLongAddress(s.locoAddr())),
                        ConsistListener.OPERATION_SUCCESS);
                processQueuedWork();
        }
    }

    // Throttle listener interface functions
    @Override
    public void notifyThrottleFound(jmri.DccThrottle t) {
        log.debug("notified Throttle {} found with mode {}", t.getLocoAddress(), consistRequestState);
        try {
            if (consistRequestState == LEADREQUESTSTATE) {
                ((LocoNetThrottle) t).setIsForward(true);
                leadSlot = ((LocoNetThrottle) t).getLocoNetSlot();
                processQueuedWork();
            } else {
                LocoNetSlot tempSlot = ((LocoNetThrottle) t).getLocoNetSlot();
                if (tempSlot != null) {
                    tempSlot.addSlotListener(this);
                    if (consistRequestState == LINKSTAGEONESTATE) {
                        notifyChangedSlot(tempSlot);
                        setDirection(((LocoNetThrottle) t));
                        consistRequestState = LINKSTAGETWOSTATE;
                    } else {
                        setDirection(((LocoNetThrottle) t));
                    }
                } else {
                    log.error("Cannot notify a throttle's slot if the slot is null!");
                }
            }
        } catch (java.lang.ClassCastException cce) {
            // if the simulator is in use, we will
            // get a ClassCastException.
            if (consistRequestState == LEADREQUESTSTATE) {
                t.setIsForward(true);
                processQueuedWork();
            } else {
                if (t instanceof LocoNetThrottle) {
                    LocoNetThrottle lt = (LocoNetThrottle)t;
                    setDirection(lt);
                }
            }
        }
    }

    @Override
    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
        if (! (address instanceof DccLocoAddress)) {
            throw new IllegalArgumentException("address is not a DccLocoAddress object");
        }
        notifyConsistListeners((DccLocoAddress) address,
                ConsistListener.CONSIST_ERROR);
        removeFromConsistList((DccLocoAddress) address);
        pendingSlotAddress = null;
        processQueuedWork();
    }

    /**
     * No steal or share decisions made locally
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void notifyDecisionRequired(jmri.LocoAddress address, DecisionType question) {
        log.debug("notifydecisionrequired {} {}", address, question);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LocoNetConsist.class);

}
