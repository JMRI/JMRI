package jmri.jmrix.loconet;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the contents of a single slot in the LocoNet command station.
 * <p>
 * A SlotListener can be registered to hear of changes in this slot. All changes
 * in values will result in notification.
 * <p>
 * Strictly speaking, functions 9 through 28 are not in the actual slot, but
 * it's convenient to imagine there's an "extended slot" and keep track of them
 * here. This is a partial implementation, though, because setting is still done
 * directly in {@link LocoNetThrottle}. In particular, if this slot has not been
 * read from the command station, the first message directly setting F9 through
 * F28 will not have a place to store information. Instead, it will trigger a
 * slot read, so the following messages will be properly handled.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Stephen Williams Copyright (C) 2008
 * @author B. Milhaupt, Copyright (C) 2018
 * @author S. Gigiel, Copyright (C) 2018
 */
public class LocoNetSlot {

    // accessors to specific information
    public int getSlot() {
        return slot;
    }  // cannot modify the slot number once created


    /** Get decoder mode.
     * Possible values are  
     * {@link LnConstants#DEC_MODE_128A},
     * {@link LnConstants#DEC_MODE_28A},
     * {@link LnConstants#DEC_MODE_128},
     * {@link LnConstants#DEC_MODE_14},
     * {@link LnConstants#DEC_MODE_28TRI},
     * {@link LnConstants#DEC_MODE_28}
     */
    public int decoderType() {
        return stat & LnConstants.DEC_MODE_MASK;
    }

    /** Get slot status.
     * Possible values are 
     * {@link LnConstants#LOCO_IN_USE},
     * {@link LnConstants#LOCO_IDLE},
     * {@link LnConstants#LOCO_COMMON},
     * {@link LnConstants#LOCO_FREE}
     */
    public int slotStatus() {
        return stat & LnConstants.LOCOSTAT_MASK;
    }

    public int ss2() {
        return ss2;
    }

    /** Get consist status.
     * Possible values are 
     * {@link LnConstants#CONSIST_NO},
     * {@link LnConstants#CONSIST_TOP},
     * {@link LnConstants#CONSIST_MID},
     * {@link LnConstants#CONSIST_SUB}
     */
    public int consistStatus() {
        return stat & LnConstants.CONSIST_MASK;
    }

    // direction and functions
    public boolean isForward() {
        return 0 == (dirf & LnConstants.DIRF_DIR);
    }

    public boolean isF0() {
        return 0 != (dirf & LnConstants.DIRF_F0);
    }

    public boolean isF1() {
        return 0 != (dirf & LnConstants.DIRF_F1);
    }

    public boolean isF2() {
        return 0 != (dirf & LnConstants.DIRF_F2);
    }

    public boolean isF3() {
        return 0 != (dirf & LnConstants.DIRF_F3);
    }

    public boolean isF4() {
        return 0 != (dirf & LnConstants.DIRF_F4);
    }

    public boolean isF5() {
        return 0 != (snd & LnConstants.SND_F5);
    }

    public boolean isF6() {
        return 0 != (snd & LnConstants.SND_F6);
    }

    public boolean isF7() {
        return 0 != (snd & LnConstants.SND_F7);
    }

    public boolean isF8() {
        return 0 != (snd & LnConstants.SND_F8);
    }

    public boolean isF9() {
        return localF9;
    }

    public boolean isF10() {
        return localF10;
    }

    public boolean isF11() {
        return localF11;
    }

    public boolean isF12() {
        return localF12;
    }

    public boolean isF13() {
        return localF13;
    }

    public boolean isF14() {
        return localF14;
    }

    public boolean isF15() {
        return localF15;
    }

    public boolean isF16() {
        return localF16;
    }

    public boolean isF17() {
        return localF17;
    }

    public boolean isF18() {
        return localF18;
    }

    public boolean isF19() {
        return localF19;
    }

    public boolean isF20() {
        return localF20;
    }

    public boolean isF21() {
        return localF21;
    }

    public boolean isF22() {
        return localF22;
    }

    public boolean isF23() {
        return localF23;
    }

    public boolean isF24() {
        return localF24;
    }

    public boolean isF25() {
        return localF25;
    }

    public boolean isF26() {
        return localF26;
    }

    public boolean isF27() {
        return localF27;
    }

    public boolean isF28() {
        return localF28;
    }

    // loco address, speed
    public int locoAddr() {
        return addr;
    }

    public int speed() {
        return spd;
    }

    public int dirf() {
        return dirf;
    }

    public int snd() {
        return snd;
    }

    public int id() {
        return id;
    }

    // programmer track special case accessors
    public int pcmd() {
        return _pcmd;
    }

    public int cvval() {
        return snd + (ss2 & 2) * 64;
    }

    // global track status should be reference through SlotManager
    // create a specific slot
    public LocoNetSlot(int slotNum) {
        slot = slotNum;
    }

    public LocoNetSlot(LocoNetMessage l) throws LocoNetException {
        slot = l.getElement(2);
        setSlot(l);
    }

    boolean localF9 = false;
    boolean localF10 = false;
    boolean localF11 = false;
    boolean localF12 = false;
    boolean localF13 = false;
    boolean localF14 = false;
    boolean localF15 = false;
    boolean localF16 = false;
    boolean localF17 = false;
    boolean localF18 = false;
    boolean localF19 = false;
    boolean localF20 = false;
    boolean localF21 = false;
    boolean localF22 = false;
    boolean localF23 = false;
    boolean localF24 = false;
    boolean localF25 = false;
    boolean localF26 = false;
    boolean localF27 = false;
    boolean localF28 = false;

    // methods to interact with LocoNet
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public void setSlot(LocoNetMessage l) throws LocoNetException { // exception if message can't be parsed
        // sort out valid messages, handle
        switch (l.getOpCode()) {
            case 0xd5:
                if ((l.getElement(1) & 0b11110000) == 0) {
                    // speed and direction
                    spd = l.getElement(4);
                    dirf = dirf & 0b11011111;
                    if ((l.getElement(1) & 0b00001000) != 0) {
                        dirf = dirf | 0b00100000;
                    }
                } else if ((l.getElement(1) & 0b11111000) == 0b00010000) {
                    // function grp 1
                    dirf = dirf & 0b11100000;
                    dirf = dirf | (l.getElement(4) & 0b00011111);
                    snd = snd & 0b11111100;
                    snd = snd | ((l.getElement(4) & 0b01100000) >> 5);
                } else if ((l.getElement(1) & 0b11111000) == 0b00011000) {
                    // function grp 2
                    snd = snd & 0b11110011;
                    snd = snd | ((l.getElement(4) & 0b00000011) << 2);
                    localF9 = ((l.getElement(4) & 0b00000100) != 0);
                    localF10 = ((l.getElement(4) & 0b00001000) != 0);
                    localF11 = ((l.getElement(4) & 0b00010000) != 0);
                    localF12 = ((l.getElement(4) & 0b00100000) != 0);
                    localF13 = ((l.getElement(4) & 0b01000000) != 0);
                } else if ((l.getElement(1) & 0b11111000) == 0b00100000) {
                    localF14 = ((l.getElement(4) & 0b00000001) != 0);
                    localF15 = ((l.getElement(4) & 0b00000010) != 0);
                    localF16 = ((l.getElement(4) & 0b00000100) != 0);
                    localF17 = ((l.getElement(4) & 0b00001000) != 0);
                    localF18 = ((l.getElement(4) & 0b00010000) != 0);
                    localF19 = ((l.getElement(4) & 0b00100000) != 0);
                    localF20 = ((l.getElement(4) & 0b01000000) != 0);
                } else if ((l.getElement(1) & 0b11111000) == 0b00101000 || (l.getElement(1) & 0b11111000) == 0b00110000) {
                    localF21 = ((l.getElement(4) & 0b00000001) != 0);
                    localF22 = ((l.getElement(4) & 0b00000010) != 0);
                    localF23 = ((l.getElement(4) & 0b00000100) != 0);
                    localF24 = ((l.getElement(4) & 0b00001000) != 0);
                    localF25 = ((l.getElement(4) & 0b00010000) != 0);
                    localF26 = ((l.getElement(4) & 0b00100000) != 0);
                    localF27 = ((l.getElement(4) & 0b01000000) != 0);
                    localF28 = ((l.getElement(1) & 0b00010000) != 0);
                }
                notifySlotListeners();
                break;
            case 0xe6:
            case 0xee:
                lastUpdateTime = System.currentTimeMillis();
                stat = l.getElement(4);
                addr = l.getElement(5) + 128 * l.getElement(6);
                spd = l.getElement(8);
                //dirf = dirf & 0b11011111;
                //if ((l.getElement(2) & 0b00001000) != 0) {
                //    dirf = dirf | 0b00100000;
                //}
                dirf = l.getElement(10) & 0b00111111;
                id = l.getElement(18) + 256 * l.getElement(19);
                //dirf = dirf & 0b11100000;
                //dirf = dirf | (l.getElement(10) & 0b00011111);
                snd = snd & 0b11111100;
                snd = snd |  ( (l.getElement(11) & 0b01100000) >> 5) ;
                snd = l.getElement(11) & 0b00001111;
                localF9  = ((l.getElement(11) & 0b00010000 ) != 0);
                localF10 = ((l.getElement(11) & 0b00100000 ) != 0);
                localF11 = ((l.getElement(11) & 0b01000000 ) != 0);
                localF12 = ((l.getElement(9)  & 0b00010000 ) != 0);
                localF13 = ((l.getElement(12) & 0b00000001 ) != 0);
                localF14 = ((l.getElement(12) & 0b00000010 ) != 0);
                localF15 = ((l.getElement(12) & 0b00000100 ) != 0);
                localF16 = ((l.getElement(12) & 0b00001000 ) != 0);
                localF17 = ((l.getElement(12) & 0b00010000 ) != 0);
                localF18 = ((l.getElement(12) & 0b00100000 ) != 0);
                localF19 = ((l.getElement(12) & 0b01000000 ) != 0);
                localF20 = ((l.getElement(9)  & 0b00100000 ) != 0);
                localF21 = ((l.getElement(13) & 0b00000001 ) != 0);
                localF22 = ((l.getElement(13) & 0b00000010 ) != 0);
                localF23 = ((l.getElement(13) & 0b00000100 ) != 0);
                localF24 = ((l.getElement(13) & 0b00001000 ) != 0);
                localF25 = ((l.getElement(13) & 0b00010000 ) != 0);
                localF26 = ((l.getElement(13) & 0b00100000 ) != 0);
                localF27 = ((l.getElement(13) & 0b01000000 ) != 0);
                localF28 = ((l.getElement(9)  & 0b01000000 ) != 0);

                notifySlotListeners();
                break;
            case 0xd4:
                // null move or change status if byte 1 bits 5-3 on
                if ((l.getElement(1) & 0b11111000 ) == 0b00111000 ) {
                    if (( l.getElement(3) & 0b01100010) == 0b01100000) {
                        // new status in byte 4 update status
                        stat = l.getElement(4);
                    } else if (( l.getElement(3) & 0b0110010) == 0b00000010) {
                        // add loco in slot 1 to top slot 2
                        // there will be a status response.
                        log.info("Prep for consisting");
                    } else {
                        // slot move slot zero = dispatch same slot is null move
                        if (l.getElement(3) == 0 && l.getElement(4) == 0) {
                            stat = stat & ~LnConstants.LOCO_IN_USE;
                            log.info("set idle");
                        }
                    }
                }
                notifySlotListeners();
                break;
            case LnConstants.OPC_SL_RD_DATA:
                lastUpdateTime = System.currentTimeMillis();
            //fall through
            case LnConstants.OPC_WR_SL_DATA: {
                if (l.getElement(1) != 0x0E) {
                    return;  // not an appropriate reply
                }            // valid, so fill contents
                if (slot != l.getElement(2)) {
                    log.error("Asked to handle message not for this slot ("
                            + slot + ") " + l);
                }
                stat = l.getElement(3);
                _pcmd = l.getElement(4);
                addr = l.getElement(4) + 128 * l.getElement(9);
                spd = l.getElement(5);
                dirf = l.getElement(6);
                trk = l.getElement(7);
                ss2 = l.getElement(8);
                // item 9 is in add2
                snd = l.getElement(10);
                id = l.getElement(11) + 128 * l.getElement(12);

                notifySlotListeners();
                return;
            }
            case LnConstants.OPC_SLOT_STAT1:
                if (slot != l.getElement(1)) {
                    log.error("Asked to handle message not for this slot " + l);
                }
                stat = l.getElement(2);
                notifySlotListeners();
                lastUpdateTime = System.currentTimeMillis();
                return;
            case LnConstants.OPC_LOCO_SND: {
                // set sound functions in slot - first, clear bits
                snd &= ~(LnConstants.SND_F5 | LnConstants.SND_F6
                        | LnConstants.SND_F7 | LnConstants.SND_F8);
                // and set them as masked
                snd |= ((LnConstants.SND_F5 | LnConstants.SND_F6
                        | LnConstants.SND_F7 | LnConstants.SND_F8) & l.getElement(2));
                notifySlotListeners();
                lastUpdateTime = System.currentTimeMillis();
                return;
            }
            case LnConstants.OPC_LOCO_DIRF: {
                // When slot is consist-mid or consist-sub, this LocoNet Opcode 
                // can only change the functions; direction cannot be changed.
                if (((stat & LnConstants.CONSIST_MASK) == LnConstants.CONSIST_MID) ||
                        ((stat & LnConstants.CONSIST_MASK) == LnConstants.CONSIST_SUB)) {
                    // set functions in slot - first, clear bits, preserving DIRF_DIR bit
                    dirf &= LnConstants.DIRF_DIR | (~(LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4));
                    // and set the function bits from the LocoNet message
                    dirf += ((LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4) & l.getElement(2));
                } else {
                    // set direction, functions in slot - first, clear bits
                    dirf &= ~(LnConstants.DIRF_DIR | LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4);
                    // and set them as masked
                    dirf += ((LnConstants.DIRF_DIR | LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4) & l.getElement(2));
                    
                }
                notifySlotListeners();
                lastUpdateTime = System.currentTimeMillis();
                return;
            }
            case LnConstants.OPC_MOVE_SLOTS: {
                // change in slot status will be reported by the reply,
                // so don't need to do anything here (but could)
                lastUpdateTime = System.currentTimeMillis();
                notifySlotListeners();
                return;
            }
            case LnConstants.OPC_LOCO_SPD: {
                // This opcode has no effect on the slot's speed setting if the
                // slot is mid-consist or sub-consist.
                if (((stat & LnConstants.CONSIST_MASK) != LnConstants.CONSIST_MID) &&
                        ((stat & LnConstants.CONSIST_MASK) != LnConstants.CONSIST_SUB)) {
                    
                    spd = l.getElement(2);
                    notifySlotListeners();
                    lastUpdateTime = System.currentTimeMillis();
                } else {
                    log.info("Ignoring speed change for slot {} marked as consist-mid or consist-sub.", slot);
                }
                return;
            }
            case LnConstants.OPC_CONSIST_FUNC: {
                // This opcode can be sent to a slot which is marked as mid-consist 
                // or sub-consist.  Do not pay attention to this message if the
                // slot is not mid-consist or sub-consist.
                if (((stat & LnConstants.CONSIST_MASK) == LnConstants.CONSIST_MID) ||
                        ((stat & LnConstants.CONSIST_MASK) == LnConstants.CONSIST_SUB)) {
                    // set functions in slot - first, clear bits, preserving DIRF_DIR bit
                    dirf &= LnConstants.DIRF_DIR | (~(LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4));
                    // and set the function bits from the LocoNet message
                    dirf += ((LnConstants.DIRF_F0
                            | LnConstants.DIRF_F1 | LnConstants.DIRF_F2
                            | LnConstants.DIRF_F3 | LnConstants.DIRF_F4) & l.getElement(2));
                    notifySlotListeners();
                    lastUpdateTime = System.currentTimeMillis();
                }
                return;
            }
            default: {
                throw new LocoNetException("message can't be parsed"); // NOI18N
            }
        }
    }

    /**
     * Load functions 9 through 28 from LocoNet "Set Direct" message.
     */
    public void functionMessage(long pkt) {
        // parse for which set of functions
        if ((pkt & 0xFFFFFF0) == 0xA0) {
            // F9-12
            localF9 = ((pkt & 0x01) != 0);
            localF10 = ((pkt & 0x02) != 0);
            localF11 = ((pkt & 0x04) != 0);
            localF12 = ((pkt & 0x08) != 0);
            notifySlotListeners();
        } else if ((pkt & 0xFFFFFF00) == 0xDE00) {
            // check F13-20
            localF13 = ((pkt & 0x01) != 0);
            localF14 = ((pkt & 0x02) != 0);
            localF15 = ((pkt & 0x04) != 0);
            localF16 = ((pkt & 0x08) != 0);
            localF17 = ((pkt & 0x10) != 0);
            localF18 = ((pkt & 0x20) != 0);
            localF19 = ((pkt & 0x40) != 0);
            localF20 = ((pkt & 0x80) != 0);
            notifySlotListeners();
        } else if ((pkt & 0xFFFFFF00) == 0xDF00) {
            // check F21-28
            localF21 = ((pkt & 0x01) != 0);
            localF22 = ((pkt & 0x02) != 0);
            localF23 = ((pkt & 0x04) != 0);
            localF24 = ((pkt & 0x08) != 0);
            localF25 = ((pkt & 0x10) != 0);
            localF26 = ((pkt & 0x20) != 0);
            localF27 = ((pkt & 0x40) != 0);
            localF28 = ((pkt & 0x80) != 0);
            notifySlotListeners();
        }
    }

    /**
     * Update the decoder type bits in STAT1 (D2, D1, D0)
     *
     * @param status New values for STAT1 (D2, D1, D0)
     * @return Formatted LocoNet message to change value.
     */
    public LocoNetMessage writeMode(int status) {
        if (slot < 128 ) {
            LocoNetMessage l = new LocoNetMessage(4);
            l.setOpCode(LnConstants.OPC_SLOT_STAT1);
            l.setElement(1, slot);
            l.setElement(2, (stat & ~LnConstants.DEC_MODE_MASK) | status);
            return l;
        } else {
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xd4);
            l.setElement(1, ((slot / 128) & 0x07) | 0b00111000 ) ;
            l.setElement(2, slot & 0x7f);
            l.setElement(3, 0x60);
            l.setElement(4, (stat & ~LnConstants.DEC_MODE_MASK) | status);
            return l;
        }
    }
    
    public LocoNetMessage writeThrottleID(int newID) {
        id = (newID & 0x7f7f);
        return writeSlot();
    }

    /**
     * Update the status mode bits in STAT1 (D5, D4)
     *
     * @param status New values for STAT1 (D5, D4)
     * @return Formatted LocoNet message to change value.
     */
    public LocoNetMessage writeStatus(int status) {
        if (slot < 128 ) {
            LocoNetMessage l = new LocoNetMessage(4);
            l.setOpCode(LnConstants.OPC_SLOT_STAT1);
            l.setElement(1, slot);
            l.setElement(2, (stat & ~LnConstants.LOCOSTAT_MASK) | status);
            return l;
        } else {
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xd4);
            l.setElement(1, ((slot / 128) & 0x07) | 0b00111000 ) ;
            l.setElement(2, slot & 0x7f);
            l.setElement(3, 0x60);
            l.setElement(4, (stat & ~LnConstants.LOCOSTAT_MASK) | status);
            return l;
        }
    }

    /**
     * Update Speed
     *
     * @param speed new speed
     * @return Formatted LocoNet message to change value.
     */
    public LocoNetMessage writeSpeed(int speed) {
        if (slot  < 128) {
            LocoNetMessage l = new LocoNetMessage(4);
            l.setOpCode(LnConstants.OPC_LOCO_SPD);
            l.setElement(1, slot );
            l.setElement(2, speed);
            return l;
        } else {
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xd5);
            l.setElement(1, ((slot / 128) & 0x07) | ((dirf &  LnConstants.DIRF_DIR ) >> 2) );
            l.setElement(2, slot & 0x7f);
            l.setElement(3, (id & 0x7f));
            l.setElement(4, speed);
            return l;
        }
    }

    /**
     * Create LocoNet message which dispatches this slot
     * 
     * Note that the invoking method ought to invoke the slot's NotifySlotListeners 
     * method to inform any other interested parties that the slot status has changed.
     * 
     * @return LocoNet message which "dispatches" the slot
    */
    public LocoNetMessage dispatchSlot() {
        if (slot < 128) {
            LocoNetMessage l = new LocoNetMessage(4);
            l.setOpCode(LnConstants.OPC_MOVE_SLOTS);
            l.setElement(1, slot);
            l.setElement(2, 0);
            return l;
        } else {
            LocoNetMessage l = new LocoNetMessage(6);
            l.setOpCode(0xd4);
            l.setElement(1, ((slot / 128) & 0x07) | 0b00111000 ) ;
            l.setElement(2, slot & 0x7f);
            l.setElement(3, 0);
            l.setElement(4, 0);
            return l;
        }
    }

    /**
     * Create a LocoNet OPC_SLOT_STAT1 message which releases this slot to the 
     * "Common" state
     * 
     * The invoking method must send the returned LocoNet message to LocoNet in
     * order to have a useful effect.  
     * 
     * Upon receipt of the echo of the transmitted OPC_SLOT_STAT1 message, the 
     * LocoNetSlot object will notify its listeners.
     * 
     * @return LocoNet message which "releases" the slot to the "Common" state
    */
    public LocoNetMessage releaseSlot() {
        return writeStatus(LnConstants.LOCO_COMMON);
    }

    public LocoNetMessage writeSlot() {
        if (slot < 128) {
            LocoNetMessage l = new LocoNetMessage(14);
            l.setOpCode(LnConstants.OPC_WR_SL_DATA);
            l.setElement(1, 0x0E);
            l.setElement(2, slot & 0x7F);
            l.setElement(3, stat & 0x7F);
            l.setElement(4, addr & 0x7F);
            l.setElement(9, (addr / 128) & 0x7F);
            l.setElement(5, spd & 0x7F);
            l.setElement(6, dirf & 0x7F);
            l.setElement(7, trk & 0x7F);
            l.setElement(8, ss2 & 0x7F);
            // item 9 is add2
            l.setElement(10, snd & 0x7F);
            l.setElement(12, id & 0x7F); // loco id is specified as to 2 byte hex the wrong way round.... 
            l.setElement(11, (id / 256) & 0x7F); // loco id is specified as to 2 byte hex
            return l;
        }
        LocoNetMessage l = new LocoNetMessage(21);
        l.setOpCode(0xee);
        l.setElement(1, 0x15);
        l.setElement(2, (slot / 128) & 0x07);
        l.setElement(3, slot & 0x7F);
        l.setElement(4, stat & 0x7F);
        l.setElement(6, (addr / 128) & 0x7F);
        l.setElement(5, addr & 0x7F);
        l.setElement(7, 0x47);
        l.setElement(19, (id / 256) & 0x7F); // loco id is specified as to 2 byte hex the wrong way round....
        l.setElement(18, id & 0x7F); // loco id is specified as to 2 byte hex
        return l;
    }

    // data values to echo slot contents
    final private int slot;   // <SLOT#> is the number of the slot that was read.
    private int stat; // <STAT> is the status of the slot
    private int addr; // full address of the loco, made from
    //    <ADDR> is the low 7 (0-6) bits of the Loco address
    //    <ADD2> is the high 7 bits (7-13) of the 14-bit loco address
    private int spd; // <SPD> is the current speed (0-127)
    private int dirf; // <DIRF> is the current Direction and the setting for functions F0-F4
    private int trk = 7; // <TRK> is the global track status
    private int ss2; // <SS2> is the an additional slot status
    private int snd;  // <SND> is the settings for functions F5-F8
    private int id;  // throttle id, made from
    //     <ID1> and <ID2> normally identify the throttle controlling the loco

    private int _pcmd;  // hold pcmd and pstat for programmer

    private long lastUpdateTime; // Time of last update for detecting stale slots

    // data members to hold contact with the slot listeners
    final private List<SlotListener> slotListeners = new ArrayList<SlotListener>();

    public synchronized void addSlotListener(SlotListener l) {
        // add only if not already registered
        if (!slotListeners.contains(l)) {
            slotListeners.add(l);
        }
    }

    public synchronized void removeSlotListener(SlotListener l) {
        if (slotListeners.contains(l)) {
            slotListeners.remove(l);
        }
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void notifySlotListeners() {
        // make a copy of the listener list to synchronized not needed for transmit
        List<SlotListener> v;
        synchronized (this) {
            v = new ArrayList<SlotListener>(slotListeners);
        }
        log.debug("notify {} SlotListeners",v.size()); // NOI18N
        // forward to all listeners
        int cnt = v.size();
        for (int i = 0; i < cnt; i++) {
            SlotListener client = v.get(i);
            client.notifyChangedSlot(this);
        }
    }

    /**
     * Get the track status byte (location 7)
     */
    public int getTrackStatus() { return trk; }
    /**
     * Set the track status byte (location 7)
     */
    public void setTrackStatus(int status) { trk = status; }
    
    /**
     * Only valid for fast-clock slot.
     *
     * @return "Days" value currently in fast-clock slot.
     */
    public int getFcDays() {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcDays invalid for slot " + getSlot());
        }
        return (addr & 0x3f80) / 0x80;
    }

    /**
     * For fast-clock slot, set "days" value.
     */
    public void setFcDays(int val) {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcDays invalid for slot " + getSlot());
        }
        addr = val * 128 + (addr & 0x7f);
    }

    /**
     * Only valid for fast-clock slot.
     *
     * @return "Hours" value currently stored in fast clock slot.
     */
    public int getFcHours() {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcHours invalid for slot " + getSlot());
        }
        int temp = ((256 - ss2) & 0x7F) % 24;
        return (24 - temp) % 24;
    }

    /**
     * For fast-clock slot, set "hours" value.
     */
    public void setFcHours(int val) {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcHours invalid for slot " + getSlot());
        }
        ss2 = (256 - (24 - val)) & 0x7F;
    }

    /**
     * Only valid for fast-clock slot.
     *
     * @return Return minutes value currently stored in the fast clock slot.
     */
    public int getFcMinutes() {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcMinutes invalid for slot " + getSlot());
        }
        int temp = ((255 - dirf) & 0x7F) % 60;
        return (60 - temp) % 60;
    }

    /**
     * For fast-clock slot, set "minutes" value.
     */
    public void setFcMinutes(int val) {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcMinutes invalid for slot " + getSlot());
        }
        dirf = (255 - (60 - val)) & 0x7F;
    }

    /**
     * Only valid for fast-clock slot.
     *
     * @return Return frac_mins field which is the number of 65ms ticks until
     *         then next minute rollover. These ticks step at the current fast
     *         clock rate
     */
    public int getFcFracMins() {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcMinutes invalid for slot " + getSlot());
        }
        return 0x3FFF - ((addr & 0x7F) | ((spd & 0x7F) << 7));
    }

    /**
     * For fast-clock slot, set "frac_mins" value.
     */
    public void setFcFracMins(int val) {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcMinutes invalid for slot " + getSlot());
        }
        int temp = 0x3FFF - val;
        addr = addr | (temp & 0x7F);
        spd = (temp >> 7) & 0x7F;
    }

    /**
     * Only valid for fast-clock slot.
     *
     * @return Rate stored in fast clock slot.
     */
    public int getFcRate() {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcMinutes invalid for slot " + getSlot());
        }
        return stat;
    }

    /**
     * For fast-clock slot, set "rate" value.
     */
    public void setFcRate(int val) {
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcMinutes invalid for slot " + getSlot());
        }
        stat = val & 0x7F;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetSlot.class);
}
