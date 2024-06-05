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

    // State Machine states
    static final int IDLESTATE = 0;
    static final int LEADREQUESTSTATE = 1;
    static final int LINKSTAGEONESTATE = 2;
    static final int LINKSTAGETWOSTATE = 4;
    static final int LINKSTAGETHREESTATE = 8;
    static final int UNLINKSTAGEONESTATE = 16;

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
        DccLocoAddress locoAddress = needToWrite.get(0);
        if (consistType == ADVANCED_CONSIST) {
            addToAdvancedConsist(locoAddress, getLocoDirection(locoAddress));
        } else if (consistType == CS_CONSIST) {
            addToCSConsist(locoAddress, getLocoDirection(locoAddress));
        }
        needToWrite.remove(locoAddress);
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
          notifyConsistListeners(locoAddress,ConsistListener.OPERATION_SUCCESS);
          return;
        }
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
        if(consistList.size()==1 && locoAddress.equals(consistAddress)){
          // there is only one address in this consist, no reason to link.
          notifyConsistListeners(locoAddress,ConsistListener.OPERATION_SUCCESS);
          return;
        }
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
        consistRequestState = IDLESTATE;
        if (!needToWrite.isEmpty()) {
            delayedAdd();
        }
    }

    /**
     * create and send a message to unlink two slots
     * @param lead is the slot which is the leader
     * @param follow is the slot which was following the leader
     */
    private void unlinkSlots(LocoNetSlot lead, LocoNetSlot follow) {
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
        } else {
          // lead == follow
          // this is an error, notify the consist listeners.
          follow.removeSlotListener(this);
          notifyConsistListeners(new DccLocoAddress(follow.locoAddr(),
                throttleManager.canBeLongAddress(follow.locoAddr())),
                ConsistListener.CONSIST_ERROR | ConsistListener.DELETE_ERROR );
        }
        consistRequestState = IDLESTATE;
        if (!needToWrite.isEmpty()) {
            delayedAdd();
        }
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
                linkSlots(leadSlot, s);
                break;
            case UNLINKSTAGEONESTATE:
                unlinkSlots(leadSlot, s);
                break;
            default:
                s.removeSlotListener(this);
                notifyConsistListeners(new DccLocoAddress(s.locoAddr(),
                        throttleManager.canBeLongAddress(s.locoAddr())),
                        ConsistListener.OPERATION_SUCCESS);
                if (!needToWrite.isEmpty()) {
                    delayedAdd();
                } else {
                    consistRequestState = IDLESTATE;
                }
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
                consistRequestState = IDLESTATE;
                if (!needToWrite.isEmpty()) {
                    delayedAdd();
                }
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
                consistRequestState = IDLESTATE;
                if (!needToWrite.isEmpty()) {
                    delayedAdd();
                }
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
        consistRequestState = IDLESTATE;
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
