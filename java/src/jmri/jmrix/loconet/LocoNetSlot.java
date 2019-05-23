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
 */
public class LocoNetSlot {

    // create a specific slot
    /**
     * Create a slot based solely on a slot number.  The remainder of the slot is
     * left un-initialized.
     *
     * @param slotNum  slot number to be assigned to the new LocoNetSlot object
     */
    public LocoNetSlot(int slotNum) {
        slot = slotNum;
    }

    /**
     * Creates a slot object based on the contents of a LocoNet message.
     * The slot number is assumed to be found in byte 2 of the message
     *
     * @param l  a LocoNet message
     * @throws LocoNetException if the slot does not have an easily-found
     * slot number
     */
    public LocoNetSlot(LocoNetMessage l) throws LocoNetException {
        // TODO: Consider limiting the types of LocoNet message which can be
        // used to construct the object to only LocoNet slot write or slot
        // report messages, since a LocoNetSlot object constructed from a LocoNet
        // "speed" message or "dir/func" message does not give any other useful
        // information for object initialization.
        slot = l.getElement(2);
        setSlot(l);
    }

    // accessors to specific information
    /**
     * Returns the slot number which was either specified or inferred at object
     * creation time.
     *
     * @return the slot number
     */
    public int getSlot() {
        return slot;
    }  // cannot modify the slot number once created


    /**
     * Get decoder mode.
     *
     * The decoder (operating) mode is taken from those bits in the slot's STAT
     * byte which reflect the "speed steps" and "consisting" mode.  Note that
     * the other bits from the STAT byte are not visible via this method.
     * this
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     * <p>
     * Possible values are
     * {@link LnConstants#DEC_MODE_128A},
     * {@link LnConstants#DEC_MODE_28A},
     * {@link LnConstants#DEC_MODE_128},
     * {@link LnConstants#DEC_MODE_14},
     * {@link LnConstants#DEC_MODE_28TRI},
     * {@link LnConstants#DEC_MODE_28}
     *
     * @return the encoded decoder operating mode.
     */
    public int decoderType() {
        return stat & LnConstants.DEC_MODE_MASK;
    }

    /**
     * Get slot status.
     * <p>
     * These bits are set based on the STAT byte as seen in LocoNet slot write and
     * slot read messages.  These bits determine whether the command station is
     * actively "refreshing" the loco's speed and direction information on the
     * DCC track signal, and whether the slot is able to be re-assigned for use
     * by another locomotive.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     * <p>
     * This returns only those bits of the slot's STAT byte which are related to
     * the slot's "status".
     * <p>
     * Possible values are
     * {@link LnConstants#LOCO_IN_USE},
     * {@link LnConstants#LOCO_IDLE},
     * {@link LnConstants#LOCO_COMMON},
     * {@link LnConstants#LOCO_FREE}
     * @return the slot status bits associated with the slot
     */
    public int slotStatus() {
        return stat & LnConstants.LOCOSTAT_MASK;
    }

    /**
     * Get secondary slot status.
     * <p>
     * These bits are set based on the STAT2 byte as seen in LocoNet slot write and
     * slot read messages.  These bits determine how the command station interprets
     * the "address" field of the slot.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     * <p>
     * This returns only those bits of the slot's STAT2 byte which are related to
     * the slot's "secondary status".
     *
     * @return the slot secondary status bits associated with the slot
     */

    public int ss2() {
        return ss2;
    }

    /**
     * Get consist status.
     * <p>
     * This returns only those bits of the slot's STAT byte which are related to
     * the slot's "consisting status".
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     * <p>
     * Possible values are
     * {@link LnConstants#CONSIST_NO},
     * {@link LnConstants#CONSIST_TOP},
     * {@link LnConstants#CONSIST_MID},
     * {@link LnConstants#CONSIST_SUB}
     * @return the slot "consist status", with unrelated bits zeroed
     */
    public int consistStatus() {
        return stat & LnConstants.CONSIST_MASK;
    }

    // direction and functions
    /**
     * Returns the direction of loco movement which applies when the slot's speed
     * is set for movement.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if slot is set for forward movement, else false
     */
    public boolean isForward() {
        return 0 == (dirf & LnConstants.DIRF_DIR);
    }

    /**
     * Returns the slot's F0 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F0 is "on", else false
     */
    public boolean isF0() {
        // TODO: Consider throwing an exception (here and in similar methods)
        // if the slot is one of the "special" slots where the slot is not
        // storing mobile decoder funciton state in the associated bit.
        return 0 != (dirf & LnConstants.DIRF_F0);
    }

    /**
     * Returns the slot's F1 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F1 is "on", else false
     */
    public boolean isF1() {
        return 0 != (dirf & LnConstants.DIRF_F1);
    }

    /**
     * Returns the slot's F2 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F2 is "on", else false
     */
    public boolean isF2() {
        return 0 != (dirf & LnConstants.DIRF_F2);
    }

    /**
     * Returns the slot's F3 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F3 is "on", else false
     */
    public boolean isF3() {
        return 0 != (dirf & LnConstants.DIRF_F3);
    }

    /**
     * Returns the slot's F4 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F4 is "on", else false
     */
    public boolean isF4() {
        return 0 != (dirf & LnConstants.DIRF_F4);
    }

    /**
     * Returns the slot's F5 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F5 is "on", else false
     */
    public boolean isF5() {
        return 0 != (snd & LnConstants.SND_F5);
    }

    /**
     * Returns the slot's F6 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F6 is "on", else false
     */
    public boolean isF6() {
        return 0 != (snd & LnConstants.SND_F6);
    }

    /**
     * Returns the slot's F7 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F7 is "on", else false
     */
    public boolean isF7() {
        return 0 != (snd & LnConstants.SND_F7);
    }

    /**
     * Returns the slot's F8 state
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F8 is "on", else false
     */
    public boolean isF8() {
        return 0 != (snd & LnConstants.SND_F8);
    }

    /**
     * Returns the slot's F9 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F9 is "on", else false
     */
    public boolean isF9() {
        return localF9;
    }

    /**
     * Returns the slot's F10 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F10 is "on", else false
     */
    public boolean isF10() {
        return localF10;
    }

    /**
     * Returns the slot's F11 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F11 is "on", else false
     */
    public boolean isF11() {
        return localF11;
    }

    /**
     * Returns the slot's F12 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F12 is "on", else false
     */
    public boolean isF12() {
        return localF12;
    }

    /**
     * Returns the slot's F13 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F13 is "on", else false
     */
    public boolean isF13() {
        return localF13;
    }

    /**
     * Returns the slot's F14 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F14 is "on", else false
     */
    public boolean isF14() {
        return localF14;
    }

    /**
     * Returns the slot's F15 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F15 is "on", else false
     */
    public boolean isF15() {
        return localF15;
    }

    /**
     * Returns the slot's F16 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F16 is "on", else false
     */
    public boolean isF16() {
        return localF16;
    }

    /**
     * Returns the slot's F17 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F17 is "on", else false
     */
    public boolean isF17() {
        return localF17;
    }

    /**
     * Returns the slot's F1 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F1 is "on", else false
     */
    public boolean isF18() {
        return localF18;
    }

    /**
     * Returns the slot's F19 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F19 is "on", else false
     */
    public boolean isF19() {
        return localF19;
    }

    /**
     * Returns the slot's F20 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F20 is "on", else false
     */
    public boolean isF20() {
        return localF20;
    }

    /**
     * Returns the slot's F21 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F21 is "on", else false
     */
    public boolean isF21() {
        return localF21;
    }

    /**
     * Returns the slot's F22 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F22 is "on", else false
     */
    public boolean isF22() {
        return localF22;
    }

    /**
     * Returns the slot's F23 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F23 is "on", else false
     */
    public boolean isF23() {
        return localF23;
    }

    /**
     * Returns the slot's F24 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F24 is "on", else false
     */
    public boolean isF24() {
        return localF24;
    }

    /**
     * Returns the slot's F25 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F25 is "on", else false
     */
    public boolean isF25() {
        return localF25;
    }

    /**
     * Returns the slot's F26 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F26 is "on", else false
     */
    public boolean isF26() {
        return localF26;
    }

    /**
     * Returns the slot's F27 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F27 is "on", else false
     */
    public boolean isF27() {
        return localF27;
    }

    /**
     * Returns the slot's F28 state
     * <p>
     * Some command stations do not actively remember the state of this function.
     * JMRI attempts to track the messages which control this function, but may not
     * reliably do so in some cases.
     * <p>
     * For slot numbers not normally associated with mobile decoders, this bit
     * may have other meanings.
     *
     * @return true if F28 is "on", else false
     */
    public boolean isF28() {
        return localF28;
    }

    // loco address, speed
    /**
     * Returns the mobile decoder address associated with the slot.
     * <p>
     * Note that the returned address can encode a "short" address, a "long"
     * address or an "alias".
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     *
     * @return the mobile decoder address
     */
    public int locoAddr() {
        return addr;
    }

    /**
     * Returns the mobile decoder speed associated with the slot
     * <p>
     * If this slot object is consisted to another slot and is not the "top" of
     * the consist, then the return value is the slot number to which this slot
     * is consisted.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     *
     * @return the current speed step associated with the slot.
     */
    public int speed() {
        return spd;
    }

    /**
     * Returns the mobile decoder direction and F0-F4 bits, as used in the DIRF bits
     * of various LocoNet messages.
     * <p>
     * If this slot object is consisted to another slot and is not the "top" of
     * the consist, then the "direction" bit reflects the relative direction of this
     * loco with respect to the loco it is consisted to, where "Reverse" means it
     * travels in the "reverse" direction with respect to the loco to which it is
     * consisted.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     *
     * @return the &lt;DIRF&gt; byte value
     */
    public int dirf() {
        return dirf;
    }

    /**
     * Returns the mobile decoder F5-F8 bits, as used in the SND bits
     * of various LocoNet messages.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     *
     * @return the &lt;SND&gt; byte value
     */
    public int snd() {
        return snd;
    }

    /**
     * Returns the "Throttle ID" associated with the slot.
     * <p>
     * The returned value is a 14-bit integer comprised of ID1 as the least-significant
     * bits and ID2 as the most-significant bits.
     * <p>
     * For slot numbers not normally associated with mobile decoders, these bits
     * may have other meanings.
     *
     * @return an integer representing the throttle ID number
     */
    public int id() {
        return id;
    }

    // programmer track special case accessors
    /**
     * Returns the programmer command associated with the slot.
     * <p>
     * The returned value is taken from the &lt;PCMD&gt; byte of programmer slot read
     * and write LocoNet messages.
     * <p>
     * For slot numbers other than the programmer slot, these bits
     * may have other meanings.
     *
     * @return the &lt;PCMD&gt; byte
     */
    public int pcmd() {
        return _pcmd;
    }

    public int cvval() {
        return snd + (ss2 & 2) * 64;
    }

    // global track status should be reference through SlotManager
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

    /**
     * Update the slot object to reflect the specific contents of a
     * LocoNet message.
     * <p>Note that the object's "slot" field
     * is not updated by this method.
     *
     * @param l  a LocoNet message
     * @throws LocoNetException if the message is not one which
     *      contains slot-related data
     */
    @SuppressWarnings("fallthrough")
    @SuppressFBWarnings(value = "SF_SWITCH_FALLTHROUGH")
    public void setSlot(LocoNetMessage l) throws LocoNetException { // exception if message can't be parsed
        // sort out valid messages, handle
        switch (l.getOpCode()) {
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
                int toSlot = l.getElement(2);
                if ( toSlot == 0 ) {
                    //dispatched implies common
                    stat = (stat & ~LnConstants.LOCOSTAT_MASK) | LnConstants.LOCO_COMMON;
                }
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
     * Sets F9 through F28 (as appropriate) from data extracted from LocoNet
     * "OPC_IMM_PACKET" message.
     * <p>If the pkt parameter does not contain data from an appropriate
     * OPC_IMM_PACKET message, the pkt is ignored and the slot object remains
     * unchanged.
     *
     * @param pkt is a "long" consisting of four bytes extracted from a LocoNet
     * "OPC_IMM_PACKET" message.
     * <p>
     * {@link jmri.jmrix.loconet.SlotManager#getDirectDccPacket(LocoNetMessage m)}
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
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_SLOT_STAT1);
        l.setElement(1, slot);
        l.setElement(2, (stat & ~LnConstants.DEC_MODE_MASK) | status);
        return l;
    }

    /**
     * Sets the object's ID value and returns a LocoNet message to inform the
     * command station that the throttle ID has been changed.
     * @param newID  the new ID number to set into the slot object
     * @return a LocoNet message containing a "Slot Write" message to inform the
     * command station that a specific throttle is controlling the slot.
     */
    public LocoNetMessage writeThrottleID(int newID) {
        id = (newID & 0x17F);
        return writeSlot();
    }

    /**
     * Update the status mode bits in STAT1 (D5, D4)
     *
     * @param status New values for STAT1 (D5, D4)
     * @return Formatted LocoNet message to change value.
     */
    public LocoNetMessage writeStatus(int status) {
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_SLOT_STAT1);
        l.setElement(1, slot);
        l.setElement(2, (stat & ~LnConstants.LOCOSTAT_MASK) | status);
        return l;
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
        LocoNetMessage l = new LocoNetMessage(4);
        l.setOpCode(LnConstants.OPC_MOVE_SLOTS);
        l.setElement(1, slot);
        l.setElement(2, 0);
        return l;
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

    /**
     * Creates a LocoNet "OPC_WR_SL_DATA" message containing the current state of
     * the LocoNetSlot object.
     *
     * @return a LocoNet message which can be used to inform the command station
     * of a change in the slot contents.
     */
    public LocoNetMessage writeSlot() {
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
        l.setElement(11, id & 0x7F);
        l.setElement(12, (id / 128) & 0x7F);
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
    final private List<SlotListener> slotListeners = new ArrayList<>();

    /**
     * Registers a slot listener if it is not already registered.
     *
     * @param l  a slot listener
     */
    public synchronized void addSlotListener(SlotListener l) {
        // add only if not already registered
        if (!slotListeners.contains(l)) {
            slotListeners.add(l);
        }
    }

    /**
     * Un-registers a slot listener.
     *
     * @param l  a slot listener
     */
    public synchronized void removeSlotListener(SlotListener l) {
        if (slotListeners.contains(l)) {
            slotListeners.remove(l);
        }
    }

    /**
     * Returns the timestamp when this LocoNetSlot was updated by some LocoNet
     * message.
     *
     * @return last time the slot info was updated
     */
    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Notifies all listeners that this slot has been changed in some way.
     */
    public void notifySlotListeners() {
        // make a copy of the listener list to synchronized not needed for transmit
        List<SlotListener> v;
        synchronized (this) {
            v = new ArrayList<>(slotListeners);
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
     * <p>
     * Note that the &lt;TRK&gt; byte is not accurate on some command stations.
     *
     * @return the effective &lt;TRK&gt; byte
     */
    public int getTrackStatus() { return trk; }

    /**
     * Set the track status byte (location 7)
     * <p>
     * Note that setting the LocoNetSlot object's track status may result in a
     * change to the command station's actual track status if the slot's status
     * is communicated to the command station via an OPC_WR_DL_DATA LocoNet message.
     *
     * @param status is the new track status value.
     */
    public void setTrackStatus(int status) { trk = status; }

    /**
     * Return the days value from the slot.  Only valid for fast-clock slot.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @return "Days" value currently in fast-clock slot.
     */
    public int getFcDays() {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcDays invalid for slot " + getSlot());
        }
        return (addr & 0x3f80) / 0x80;
    }

    /**
     * For fast-clock slot, set "days" value.
     * <p>
     * Note that the new days value is not effective until a LocoNet
     * message is sent which writes the fast-clock slot data.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @param val is the new fast-clock "days" value
     */
    public void setFcDays(int val) {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcDays invalid for slot " + getSlot());
        }
        addr = val * 128 + (addr & 0x7f);
    }

    /**
     * Return the hours value from the slot.  Only valid for fast-clock slot.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @return "Hours" value currently stored in fast clock slot.
     */
    public int getFcHours() {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcHours invalid for slot " + getSlot());
        }
        int temp = ((256 - ss2) & 0x7F) % 24;
        return (24 - temp) % 24;
    }

    /**
     * For fast-clock slot, set "hours" value.
     * <p>
     * Note that the new hours value is not effective until a LocoNet
     * message is sent which writes the fast-clock slot data.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @param val is the new fast-clock "hours" value
     */
    public void setFcHours(int val) {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcHours invalid for slot " + getSlot());
        }
        ss2 = (256 - (24 - val)) & 0x7F;
    }

    /**
     * Return the minutes value from the slot.  Only valid for fast-clock slot.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @return Return minutes value currently stored in the fast clock slot.
     */
    public int getFcMinutes() {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcMinutes invalid for slot " + getSlot());
        }
        int temp = ((255 - dirf) & 0x7F) % 60;
        return (60 - temp) % 60;
    }

    /**
     * For fast-clock slot, set "minutes" value.
     * <p>
     * Note that the new minutes value is not effective until a LocoNet
     * message is sent which writes the fast-clock slot data.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @param val is the new fast-clock "minutes" value
     */
    public void setFcMinutes(int val) {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcMinutes invalid for slot " + getSlot());
        }
        dirf = (255 - (60 - val)) & 0x7F;
    }

    /**
     * Return the fractional minutes value from the slot.  Only valid for fast-
     * clock slot.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @return Return frac_mins field which is the number of 65ms ticks until
     *         then next minute rollover. These ticks step at the current fast
     *         clock rate
     */
    public int getFcFracMins() {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcFracMins invalid for slot " + getSlot());
        }
        return 0x3FFF - ((addr & 0x7F) | ((spd & 0x7F) << 7));
    }

    /**
     * Set the "frac_mins" value.
     * <p>
     * Note that the new fractional minutes value is not effective until a LocoNet
     * message is sent which writes the fast-clock slot data.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @param val is the new fast-clock "fractional minutes"
     */
    public void setFcFracMins(int val) {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcFracMins invalid for slot " + getSlot());
        }
        int temp = 0x3FFF - val;
        addr = addr | (temp & 0x7F);
        spd = (temp >> 7) & 0x7F;
    }

    /**
     * Get the fast-clock rate.  Only valid for fast-clock slot.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @return Rate stored in fast clock slot.
     */
    public int getFcRate() {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("getFcRate invalid for slot " + getSlot());
        }
        return stat;
    }

    /**
     * For fast-clock slot, set "rate" value.
     * <p>
     * Note that the new rate is not effective until a LocoNet message is sent
     * which writes the fast-clock slot data.
     * <p>
     * This method logs an error if invoked for a slot other than the fast-clock slot.
     *
     * @param val is the new fast-clock rate
     */
    public void setFcRate(int val) {
        // TODO: consider throwing a LocoNetException if issued for a slot other
        // than the "fast clock slot".
        if (getSlot() != LnConstants.FC_SLOT) {
            log.error("setFcRate invalid for slot " + getSlot());
        }
        stat = val & 0x7F;
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetSlot.class);
}
