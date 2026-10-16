package jmri.jmrix.dcc4pc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Works out which locomotives are in which block from Raw encoding packets.
 * <p>
 * Channel 1 is a broadcast: every decoder in a block sends its address in the
 * same time slot, so with two locos the ADR_LOW halves collide into invalid
 * codes. Channel 2 is only used by the decoder the DCC packet was addressed
 * to, so a valid channel 2 reply on an input right after a packet to address
 * X means X is in that block. A clean ADR_HIGH + ADR_LOW pair on channel 1 is
 * also accepted, which covers a lone loco the command station isn't
 * addressing.
 * <p>
 * A loco stays in a block until the block reads unoccupied, or until it has
 * been seen in another block and not in this one for {@link #MOVE_MILLIS}.
 * There is no plain timeout: a stopped decoder can go many seconds without a
 * channel 2 reply.
 * <p>
 * A loco that is still present and heard again is reported as a
 * {@link Change#refresh refresh}, at most every {@link #REFRESH_MILLIS}, so
 * its "last seen" time keeps up without flooding the GUI.
 *
 * @author swalton00 Copyright (C) 2026
 */
public class Dcc4PcBlockLocos {

    /** How long a loco seen elsewhere keeps its old block, to allow for bridging a gap. */
    public static final long MOVE_MILLIS = 2000;

    /** Shortest time between refreshes for a loco that stays in a block. */
    public static final long REFRESH_MILLIS = 1000;

    /**
     * RailCom from a loco on an input proves the input is occupied, whatever
     * the debounced state says. The debounced state lags the raw one (a block
     * becomes occupied after 2 packets, and unoccupied only after 300 ms), so
     * a loco entering a block can be heard before its block reads occupied.
     * An unoccupied reading therefore only removes locos not heard on that
     * input for this long, which matches the unoccupied hold in the sensor
     * manager: a block that has really been empty that long has had no RailCom.
     */
    public static final long HEARD_PROVES_OCCUPIED_MILLIS = 300;

    /** Added to an address to mark it as a long (14-bit) address. */
    public static final int LONG = 0x10000;

    /** A loco added to or removed from a block, or heard again in it. */
    public static class Change {

        /** Input number, from 1. */
        public final int input;
        /** Address, with {@link #LONG} added for a long address. */
        public final int address;
        public final boolean added;
        /** True if the loco was already in the block and has been heard again; {@link #added} is false. */
        public final boolean refresh;
        /** Why, for the log. */
        public final String how;

        Change(int input, int address, boolean added, String how) {
            this(input, address, added, false, how);
        }

        private Change(int input, int address, boolean added, boolean refresh, String how) {
            this.input = input;
            this.address = address;
            this.added = added;
            this.refresh = refresh;
            this.how = how;
        }

        static Change heard(int input, int address, String how) {
            return new Change(input, address, false, true, how);
        }

        @Override
        public String toString() {
            return (refresh ? "=" : added ? "+" : "-") + name(address) + " on input " + input + " (" + how + ")";
        }
    }

    private final int[] inputs;
    /** Per enabled input: address -> last time evidence was seen. */
    private final List<Map<Integer, Long>> seen = new ArrayList<>();
    /** Per enabled input: address -> when it was last added or refreshed. */
    private final List<Map<Integer, Long>> reported = new ArrayList<>();
    private final Integer[] adrHigh;

    /**
     * @param inputs the enabled inputs, numbered from 1, in the decoder's order
     */
    public Dcc4PcBlockLocos(int[] inputs) {
        this.inputs = inputs.clone();
        adrHigh = new Integer[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            seen.add(new LinkedHashMap<>());
            reported.add(new LinkedHashMap<>());
        }
    }

    /**
     * @param input input number, from 1
     * @return the locos in that block, in the order they were first seen
     */
    public List<Integer> locos(int input) {
        int i = indexOf(input);
        return i < 0 ? new ArrayList<>() : new ArrayList<>(seen.get(i).keySet());
    }

    /** Forget everything, as after a resync: the next packets rebuild it. */
    public void clear() {
        Arrays.fill(adrHigh, null);
        for (Map<Integer, Long> m : seen) {
            m.clear();
        }
        for (Map<Integer, Long> m : reported) {
            m.clear();
        }
    }

    /**
     * Apply one decoded packet.
     *
     * @param p      the packet
     * @param states the input states to use, debounced, in the decoder's order
     * @param now    the time, in milliseconds
     * @return what changed
     */
    public List<Change> apply(Dcc4PcRawDecoder.Packet p, int[] states, long now) {
        List<Change> changes = new ArrayList<>();
        if (p.kind == Dcc4PcRawDecoder.Kind.OVERFLOW) {
            // spec 9.3: don't pair an ADR_HIGH from before an overflow with an ADR_LOW after it
            Arrays.fill(adrHigh, null);
            return changes;
        }
        if (p.kind != Dcc4PcRawDecoder.Kind.DCC) {
            return changes;
        }
        for (int i = 0; i < inputs.length; i++) {
            if (states[i] == Dcc4PcRawDecoder.STATE_UNOCCUPIED) {
                adrHigh[i] = null;
                for (Map.Entry<Integer, Long> e : new ArrayList<>(seen.get(i).entrySet())) {
                    if (now - e.getValue() < HEARD_PROVES_OCCUPIED_MILLIS) {
                        continue; // heard here just now, so the block is occupied
                    }
                    seen.get(i).remove(e.getKey());
                    reported.get(i).remove(e.getKey());
                    changes.add(new Change(inputs[i], e.getKey(), false, "block unoccupied"));
                }
            }
        }
        int dest = mobileAddress(p.dcc);
        for (Dcc4PcRawDecoder.InputData d : p.railCom) {
            int i = indexOf(d.input);
            if (i < 0 || d.data == null) {
                continue;
            }
            int[] ch1 = Arrays.copyOfRange(d.data, 0, Math.min(2, d.data.length));
            int[] ch2 = Arrays.copyOfRange(d.data, ch1.length, d.data.length);
            // Only fresh data counts as evidence. A duplicate just says "the same bytes as
            // the Nth previous packet", and those bytes can be another loco's reply: on the
            // layout, packets to short 9 and long 9999 (idle throttle slots, no such locos)
            // were "answered" this way, even when the earlier packet went to the same
            // address. Fresh replies come often enough to find every loco.
            if (dest >= 0 && d.duplicateOf == 0 && hasValidReply(ch2)) {
                see(i, dest, now, "channel 2 reply " + hex(d.data) + " after DCC " + hex(p.dcc), changes);
            }
            if (ch1.length == 2) {
                int a = decode(ch1[0]);
                int b = decode(ch1[1]);
                if (a < 0 || b < 0) {
                    // a collision or noise: an ADR_HIGH held can't pair with what follows
                    adrHigh[i] = null;
                    continue;
                }
                int id = a >> 2;
                int payload = ((a & 0x03) << 6) | b;
                if (id == 1) { // ADR_HIGH
                    adrHigh[i] = payload;
                } else if (id == 2 && adrHigh[i] != null) { // ADR_LOW
                    int address = address(adrHigh[i], payload);
                    if (address > 0) {
                        see(i, address, now, "channel 1 address " + hex(ch1) + " (ADR_HIGH " + Integer.toHexString(adrHigh[i]) + ")", changes);
                    }
                }
            }
        }
        return changes;
    }

    private void see(int i, int address, long now, String how, List<Change> changes) {
        if (seen.get(i).put(address, now) == null) {
            reported.get(i).put(address, now);
            changes.add(new Change(inputs[i], address, true, how));
        } else if (now - reported.get(i).getOrDefault(address, 0L) >= REFRESH_MILLIS) {
            reported.get(i).put(address, now);
            changes.add(Change.heard(inputs[i], address, how));
        }
        for (int j = 0; j < inputs.length; j++) {
            Long last = j == i ? null : seen.get(j).get(address);
            if (last != null && now - last > MOVE_MILLIS) {
                seen.get(j).remove(address);
                reported.get(j).remove(address);
                changes.add(new Change(inputs[j], address, false, "seen on input " + inputs[i]));
            }
        }
    }

    private int indexOf(int input) {
        for (int i = 0; i < inputs.length; i++) {
            if (inputs[i] == input) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param address an address as used here
     * @return it as text, such as "9328(L)" or "3(S)"
     */
    public static String name(int address) {
        return (address & ~LONG) + ((address & LONG) != 0 ? "(L)" : "(S)");
    }

    /**
     * The locomotive address a DCC packet is sent to.
     *
     * @param dcc the packet bytes, without the error byte or with it
     * @return the address, with {@link #LONG} added for a long address, or -1
     *         for a broadcast, idle, accessory or malformed packet
     */
    public static int mobileAddress(int[] dcc) {
        if (dcc == null || dcc.length == 0) {
            return -1;
        }
        int b0 = dcc[0] & 0xFF;
        if (b0 >= 1 && b0 <= 127) {
            return b0;
        }
        if (b0 >= 192 && b0 <= 231 && dcc.length >= 2) {
            return LONG | ((b0 & 0x3F) << 8) | (dcc[1] & 0xFF);
        }
        return -1;
    }

    /**
     * Combine channel 1 ADR_HIGH and ADR_LOW payloads (RCN-217 5.2.1).
     *
     * @return the address, with {@link #LONG} added for a long address, or -1
     *         if ADR_HIGH isn't a valid address pattern
     */
    static int address(int high, int low) {
        if (high == 0) {
            return low & 0x7F;
        }
        if ((high & 0xC0) == 0x80) {
            return LONG | ((high & 0x3F) << 8) | (low & 0xFF);
        }
        if ((high & 0xF0) == 0x60) {
            return low & 0x7F; // consist address
        }
        return -1;
    }

    /**
     * True if channel 2 holds at least one well-formed datagram or an
     * ACK/NACK/BUSY.
     */
    static boolean hasValidReply(int[] ch2) {
        int pos = 0;
        while (pos < ch2.length) {
            int first = decode(ch2[pos]);
            if (first == SPECIAL) {
                return true;
            }
            if (first < 0) {
                pos++;
                continue;
            }
            int id = first >> 2;
            int symbols = (id == 3 || id == 7) ? 3 : (id >= 8 && id <= 11) ? 6 : 2;
            if (pos + symbols > ch2.length) {
                return false;
            }
            boolean valid = true;
            for (int k = 1; k < symbols; k++) {
                if (decode(ch2[pos + k]) < 0) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                return true;
            }
            pos += symbols;
        }
        return false;
    }

    static final int INVALID = -1;
    /** ACK, NACK or BUSY. */
    static final int SPECIAL = -2;

    /** ENCODE[value] is the RailCom 4-of-8 line code for a six-bit value. */
    private static final int[] ENCODE = {
        0xAC, 0xAA, 0xA9, 0xA5, 0xA3, 0xA6, 0x9C, 0x9A,
        0x99, 0x95, 0x96, 0x93, 0x8E, 0x8D, 0x8B, 0xB1,
        0xB2, 0xB4, 0xB8, 0x74, 0x72, 0x6C, 0x6A, 0x69,
        0x66, 0x65, 0x63, 0x5C, 0x5A, 0x59, 0x55, 0x56,
        0x53, 0x4E, 0x4D, 0x4B, 0x47, 0x71, 0xE8, 0xE4,
        0xE2, 0xD1, 0xC9, 0xC5, 0xD8, 0xD4, 0xD2, 0xCA,
        0xC6, 0xCC, 0x78, 0x17, 0x1B, 0x1D, 0x1E, 0x2E,
        0x36, 0x3A, 0x27, 0x2B, 0x2D, 0x35, 0x39, 0x33};

    private static final int[] DECODE = new int[256];

    static {
        Arrays.fill(DECODE, INVALID);
        for (int v = 0; v < ENCODE.length; v++) {
            DECODE[ENCODE[v]] = v;
        }
        DECODE[0x0F] = SPECIAL; // ACK
        DECODE[0xF0] = SPECIAL; // ACK
        DECODE[0x3C] = SPECIAL; // NACK
        DECODE[0xE1] = SPECIAL; // BUSY
    }

    /** @return the bytes as hex, for the log */
    static String hex(int[] bytes) {
        if (bytes == null) {
            return "?";
        }
        StringBuilder sb = new StringBuilder();
        for (int b : bytes) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(String.format("%02X", b & 0xFF));
        }
        return sb.toString();
    }

    /**
     * @return the six-bit value of a 4-of-8 code, {@link #SPECIAL} or
     *         {@link #INVALID}
     */
    static int decode(int b) {
        return DECODE[b & 0xFF];
    }
}
