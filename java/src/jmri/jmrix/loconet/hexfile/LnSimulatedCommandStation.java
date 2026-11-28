package jmri.jmrix.loconet.hexfile;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nonnull;

import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;

/**
 * A simulated command station on the LocoNet Simulator: it keeps loco slots
 * and answers the slot messages a throttle sends, as the LocoNet Personal
 * Edition gives them, so the LocoNet throttles work in simulation as on a
 * layout.
 * <p>
 * Answered: a request for a loco address (OPC_LOCO_ADR, answered with the
 * slot already holding it or a free one, or "no free slot"), a slot read
 * (OPC_RQ_SL_DATA), a null move that takes a slot into use and a move to
 * slot 0 that dispatches it (OPC_MOVE_SLOTS), a slot write (OPC_WR_SL_DATA,
 * acknowledged), and the status, speed, direction and function messages
 * that change a slot (OPC_SLOT_STAT1, OPC_LOCO_SPD, OPC_LOCO_DIRF,
 * OPC_LOCO_SND). Slots 1 to the number of slots set for the connection hold
 * locos; other slots (the fast clock, the programming track) are not
 * answered, as before.
 * <p>
 * Replies are sent through the connection, and, like every message on the
 * simulated LocoNet, come back to it as if heard from the command station.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
@javax.annotation.concurrent.ThreadSafe
public class LnSimulatedCommandStation implements LocoNetListener {

    /**
     * Told when a slot's loco, speed, direction or speed step mode changes,
     * as the command station would then send the loco.
     */
    @FunctionalInterface
    public interface SlotListener {

        /**
         * @param slot      the slot
         * @param timeNanos when it changed, on the {@link System#nanoTime()}
         *                  scale
         */
        void slotChanged(@Nonnull Slot slot, long timeNanos);
    }

    /** A loco slot's contents. */
    public static final class Slot {

        private final int number;
        int stat1 = 0x03; // free, 128 speed steps
        int address = 0;
        int speed = 0;
        int dirf = 0;
        int snd = 0;
        int ss2 = 0;
        int id1 = 0;
        int id2 = 0;

        Slot(int number) {
            this.number = number;
        }

        /**
         * @return the slot's number
         */
        public int getNumber() {
            return number;
        }

        /**
         * @return the loco address, 0 for none; addresses 1 to 127 are
         *         short, as LocoNet sends them
         */
        public int getAddress() {
            return address;
        }

        /**
         * @return the slot's speed: 0 stop, 1 emergency stop, 2 to 127
         *         moving
         */
        public int getSpeed() {
            return speed;
        }

        /**
         * @return true for forward
         */
        public boolean isForward() {
            return (dirf & 0x20) == 0;
        }

        /**
         * @return the decoder type bits of STAT1: 3 for 128 steps, 2 for 14,
         *         0 for 28
         */
        public int getDecoderType() {
            return stat1 & 0x07;
        }

        /**
         * @return the status bits of STAT1: 0x30 in use, 0x20 idle, 0x10
         *         common, 0 free
         */
        public int getStatus() {
            return stat1 & 0x30;
        }
    }

    private final LnTrafficController tc;
    private final Slot[] slots;
    private final List<SlotListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * @param tc       the LocoNet Simulator connection's traffic controller,
     *                 to send replies
     * @param numSlots the number of loco slots, 1 to 119
     */
    public LnSimulatedCommandStation(@Nonnull LnTrafficController tc, int numSlots) {
        this.tc = tc;
        int n = Math.max(1, Math.min(119, numSlots));
        slots = new Slot[n + 1];
        for (int i = 1; i <= n; i++) {
            slots[i] = new Slot(i);
        }
    }

    /**
     * @param listener to be told when a slot changes
     */
    public void addSlotListener(@Nonnull SlotListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener to be told no more
     */
    public void removeSlotListener(@Nonnull SlotListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param number a slot number
     * @return the slot, or null if it is not a loco slot of this command
     *         station
     */
    public synchronized Slot getSlot(int number) {
        return number >= 1 && number < slots.length ? slots[number] : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void message(LocoNetMessage m) {
        LocoNetMessage reply = null;
        Slot changed = null;
        long now = System.nanoTime();
        synchronized (this) {
            switch (m.getOpCode()) {
                case 0xBF: { // OPC_LOCO_ADR <adr hi> <adr lo>
                    int address = (m.getElement(1) << 7) | m.getElement(2);
                    Slot s = find(address);
                    if (s == null) {
                        s = free();
                        if (s != null) {
                            s.address = address;
                            s.stat1 = (s.stat1 & ~0x30) | 0x10; // common
                            s.speed = 0;
                            s.dirf = 0;
                            s.snd = 0;
                            s.ss2 = 0;
                            s.id1 = 0;
                            s.id2 = 0;
                            changed = s;
                        }
                    }
                    reply = s == null ? longAck(0xBF, 0) : slotData(s);
                    break;
                }
                case 0xBB: { // OPC_RQ_SL_DATA <slot> 0
                    Slot s = getSlot(m.getElement(1));
                    if (s != null) {
                        reply = slotData(s);
                    }
                    break;
                }
                case 0xBA: { // OPC_MOVE_SLOTS <src> <dst>
                    Slot s = getSlot(m.getElement(1));
                    int dst = m.getElement(2);
                    if (s != null && dst == s.number) { // null move: into use
                        s.stat1 = (s.stat1 & ~0x30) | 0x30;
                        reply = slotData(s);
                    } else if (s != null && dst == 0) { // dispatch put
                        s.stat1 = (s.stat1 & ~0x30) | 0x10;
                        reply = slotData(s);
                    } else if (s != null) {
                        reply = longAck(0xBA, 0);
                    }
                    break;
                }
                case 0xEF: { // OPC_WR_SL_DATA 0E <slot> stat1 adr spd dirf trk ss2 adr2 snd id1 id2 chk
                    Slot s = m.getNumDataElements() >= 14 ? getSlot(m.getElement(2)) : null;
                    if (s != null) {
                        s.stat1 = m.getElement(3);
                        s.address = (m.getElement(9) << 7) | m.getElement(4);
                        s.speed = m.getElement(5);
                        s.dirf = m.getElement(6);
                        s.ss2 = m.getElement(8);
                        s.snd = m.getElement(10);
                        s.id1 = m.getElement(11);
                        s.id2 = m.getElement(12);
                        changed = s;
                        reply = longAck(0xEF, 0x7F);
                    }
                    break;
                }
                case 0xB5: { // OPC_SLOT_STAT1 <slot> <stat1>
                    Slot s = getSlot(m.getElement(1));
                    if (s != null) {
                        s.stat1 = m.getElement(2);
                        changed = s;
                    }
                    break;
                }
                case 0xA0: { // OPC_LOCO_SPD <slot> <spd>
                    Slot s = getSlot(m.getElement(1));
                    if (s != null) {
                        s.speed = m.getElement(2) & 0x7F;
                        changed = s;
                    }
                    break;
                }
                case 0xA1: { // OPC_LOCO_DIRF <slot> <dirf>
                    Slot s = getSlot(m.getElement(1));
                    if (s != null) {
                        s.dirf = m.getElement(2) & 0x7F;
                        changed = s;
                    }
                    break;
                }
                case 0xA2: { // OPC_LOCO_SND <slot> <snd>
                    Slot s = getSlot(m.getElement(1));
                    if (s != null) {
                        s.snd = m.getElement(2) & 0x7F;
                    }
                    break;
                }
                default:
                    break; // not a slot message, or a reply
            }
        }
        if (reply != null) {
            tc.sendLocoNetMessage(reply);
        }
        if (changed != null) {
            for (SlotListener l : listeners) {
                l.slotChanged(changed, now);
            }
        }
    }

    private Slot find(int address) {
        for (int i = 1; i < slots.length; i++) {
            if (slots[i].address == address && (slots[i].stat1 & 0x30) != 0) {
                return slots[i];
            }
        }
        return null;
    }

    private Slot free() {
        for (int i = 1; i < slots.length; i++) {
            if ((slots[i].stat1 & 0x30) == 0) {
                return slots[i];
            }
        }
        return null;
    }

    /** OPC_SL_RD_DATA for a slot, track power on. */
    private static LocoNetMessage slotData(Slot s) {
        LocoNetMessage r = new LocoNetMessage(14);
        r.setElement(0, 0xE7);
        r.setElement(1, 0x0E);
        r.setElement(2, s.number);
        r.setElement(3, s.stat1 & 0x7F);
        r.setElement(4, s.address & 0x7F);
        r.setElement(5, s.speed & 0x7F);
        r.setElement(6, s.dirf & 0x7F);
        r.setElement(7, 0x07); // TRK: power on, not idle, LocoNet 1.1 master
        r.setElement(8, s.ss2 & 0x7F);
        r.setElement(9, (s.address >> 7) & 0x7F);
        r.setElement(10, s.snd & 0x7F);
        r.setElement(11, s.id1 & 0x7F);
        r.setElement(12, s.id2 & 0x7F);
        r.setParity();
        return r;
    }

    /** OPC_LONG_ACK for an opcode. */
    private static LocoNetMessage longAck(int opcode, int ack) {
        LocoNetMessage r = new LocoNetMessage(4);
        r.setElement(0, 0xB4);
        r.setElement(1, opcode & 0x7F);
        r.setElement(2, ack & 0x7F);
        r.setParity();
        return r;
    }
}
