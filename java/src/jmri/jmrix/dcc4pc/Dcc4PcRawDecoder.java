package jmri.jmrix.dcc4pc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * Decodes the Raw RailCom encoding of a DCC4PC RailCom reader (Omnibus
 * specification section 9.3). Each packet gives one DCC packet the reader saw,
 * the state of every enabled input and the RailCom bytes each input with
 * RailCom data received in the cutout that followed.
 * <p>
 * The encoding is stateful: type 1 packets carry no input states and inherit
 * them from the packet before, and the number of duplicate-flag and length
 * bytes that follow depends on those states. RailCom data may also be sent as
 * "duplicate of the 1st, 2nd or 3rd previous" for an input. The decoder
 * therefore only works on an unbroken stream that starts right after a Reset
 * Encoding State, which sets every input to unoccupied. A Get Data reply may
 * end part way through a packet; the leftover bytes are kept and joined to the
 * next reply.
 *
 * @author swalton00 Copyright (C) 2026
 */
public class Dcc4PcRawDecoder {

    public static final int STATE_UNOCCUPIED = 0;
    public static final int STATE_OCCUPIED = 1;
    public static final int STATE_ORIENTATION_A = 2;
    public static final int STATE_ORIENTATION_B = 3;

    /** The reader's output buffer overflowed and packets were lost here. */
    public static final int OVERFLOW = 0x81;

    private static final int HISTORY = 3;

    public enum Kind {
        DCC, OVERFLOW, UNKNOWN_COMMAND
    }

    /**
     * RailCom bytes for one input. {@code duplicateOf} is 0 for new data, else
     * 1-3 for a repeat of that earlier packet; {@code data} is null if the
     * repeated packet is older than the history kept. {@code sourceDcc} is the
     * DCC packet the bytes followed.
     */
    public static class InputData {

        public final int input;
        public final int duplicateOf;
        public final int[] data;
        public final int[] sourceDcc;

        InputData(int input, int duplicateOf, int[] data, int[] sourceDcc) {
            this.input = input;
            this.duplicateOf = duplicateOf;
            this.data = data;
            this.sourceDcc = sourceDcc;
        }
    }

    /**
     * One decoded packet. For DCC packets {@code states[i]} is the state of
     * {@code getInputs()[i]}; {@code explicitStates} says whether this packet
     * carried them (type 2) or inherited them (type 1).
     */
    public static class Packet {

        public final Kind kind;
        public final int[] dcc;
        public final boolean explicitStates;
        public final int[] states;
        public final List<InputData> railCom;

        Packet(Kind kind, int[] dcc, boolean explicitStates, int[] states, List<InputData> railCom) {
            this.kind = kind;
            this.dcc = dcc;
            this.explicitStates = explicitStates;
            this.states = states;
            this.railCom = railCom;
        }
    }

    private static class Entry {

        final int[] data;
        final int[] dcc;

        Entry(int[] data, int[] dcc) {
            this.data = data;
            this.dcc = dcc;
        }
    }

    private final int[] inputs;
    private final int[] states;
    private final List<Deque<Entry>> history = new ArrayList<>();
    private int[] pending = new int[0];

    /**
     * @param enabledInputs the inputs enabled on the reader, numbered from 1,
     *                      in ascending order
     */
    public Dcc4PcRawDecoder(int[] enabledInputs) {
        inputs = enabledInputs.clone();
        Arrays.sort(inputs);
        states = new int[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            history.add(new ArrayDeque<>());
        }
    }

    /**
     * @return the enabled inputs, numbered from 1, in the order their states
     *         are reported
     */
    public int[] getInputs() {
        return inputs.clone();
    }

    /**
     * Start again as the reader does after a Reset Encoding State: every input
     * unoccupied, no partial packet and no RailCom history.
     */
    public void reset() {
        pending = new int[0];
        Arrays.fill(states, STATE_UNOCCUPIED);
        for (Deque<Entry> h : history) {
            h.clear();
        }
    }

    /**
     * Decode a chunk of Get Data reply bytes.
     *
     * @param chunk the reply bytes, each 0-255
     * @return every packet completed by this chunk
     */
    public List<Packet> decode(int[] chunk) {
        int[] buf = new int[pending.length + chunk.length];
        System.arraycopy(pending, 0, buf, 0, pending.length);
        for (int i = 0; i < chunk.length; i++) {
            buf[pending.length + i] = chunk[i] & 0xFF;
        }
        List<Packet> packets = new ArrayList<>();
        int pos = 0;
        while (pos < buf.length) {
            int first = buf[pos];
            if ((first & 0x80) != 0) {
                if (first == OVERFLOW) {
                    // packets were lost, but the reader counts duplicates and states from
                    // what it delivered (spec 9.3 example, packet 4), so both still hold
                    packets.add(new Packet(Kind.OVERFLOW, new int[0], false, states.clone(), new ArrayList<>()));
                    pos++;
                    continue;
                }
                // an unknown command's length is unknown too, so the rest of the buffer goes with it
                packets.add(new Packet(Kind.UNKNOWN_COMMAND, new int[0], false, states.clone(), new ArrayList<>()));
                pos = buf.length;
                break;
            }
            int[] length = new int[1];
            Packet p = parseDcc(buf, pos, length);
            if (p == null) {
                break;
            }
            packets.add(p);
            pos += length[0];
        }
        pending = Arrays.copyOfRange(buf, pos, buf.length);
        return packets;
    }

    /**
     * Parse a type 1 or 2 packet at {@code start}. The decoder's state changes
     * only once the whole packet is present.
     *
     * @return the packet, or null if it is not complete yet
     */
    private Packet parseDcc(int[] buf, int start, int[] length) {
        int pos = start;
        int first = buf[pos++];
        boolean explicit = (first & 0x40) != 0;
        int dccLength = (first & 0x3F) + 1;
        if (pos + dccLength > buf.length) {
            return null;
        }
        int[] dcc = Arrays.copyOfRange(buf, pos, pos + dccLength);
        pos += dccLength;
        int[] newStates = states.clone();
        if (explicit) {
            int stateBytes = (inputs.length * 2 + 7) / 8;
            if (pos + stateBytes > buf.length) {
                return null;
            }
            for (int i = 0; i < inputs.length; i++) {
                newStates[i] = field(buf, pos, i, 2);
            }
            pos += stateBytes;
        }
        List<Integer> withData = new ArrayList<>();
        for (int i = 0; i < inputs.length; i++) {
            if (newStates[i] >= STATE_ORIENTATION_A) {
                withData.add(i);
            }
        }
        int dupBytes = (withData.size() * 2 + 7) / 8;
        if (pos + dupBytes > buf.length) {
            return null;
        }
        int[] dup = new int[withData.size()];
        int fresh = 0;
        for (int k = 0; k < dup.length; k++) {
            dup[k] = field(buf, pos, k, 2);
            if (dup[k] == 0) {
                fresh++;
            }
        }
        pos += dupBytes;
        int lengthBytes = (fresh * 4 + 7) / 8;
        if (pos + lengthBytes > buf.length) {
            return null;
        }
        int[] lengths = new int[fresh];
        int total = 0;
        for (int f = 0; f < fresh; f++) {
            lengths[f] = field(buf, pos, f, 4);
            total += lengths[f];
        }
        pos += lengthBytes;
        if (pos + total > buf.length) {
            return null;
        }
        // complete: resolve the data, then commit states and history
        List<InputData> railCom = new ArrayList<>();
        int[][] resolved = new int[dup.length][];
        int f = 0;
        for (int k = 0; k < dup.length; k++) {
            Deque<Entry> h = history.get(withData.get(k));
            int[] source;
            if (dup[k] == 0) {
                resolved[k] = Arrays.copyOfRange(buf, pos, pos + lengths[f]);
                source = dcc;
                pos += lengths[f];
                f++;
            } else {
                Entry e = dup[k] <= h.size() ? new ArrayList<>(h).get(dup[k] - 1) : null;
                resolved[k] = e == null ? null : e.data;
                source = e == null ? null : e.dcc;
            }
            railCom.add(new InputData(inputs[withData.get(k)], dup[k], resolved[k], source));
        }
        for (int k = 0; k < dup.length; k++) {
            Deque<Entry> h = history.get(withData.get(k));
            h.addFirst(new Entry(resolved[k], dcc));
            while (h.size() > HISTORY) {
                h.removeLast();
            }
        }
        System.arraycopy(newStates, 0, states, 0, states.length);
        length[0] = pos - start;
        return new Packet(Kind.DCC, dcc, explicit, newStates, railCom);
    }

    /**
     * @return the {@code index}-th {@code width}-bit field (2 or 4) starting at
     *         {@code offset}, first field in the low bits
     */
    private static int field(int[] buf, int offset, int index, int width) {
        int perByte = 8 / width;
        int b = buf[offset + index / perByte];
        return (b >> ((index % perByte) * width)) & ((1 << width) - 1);
    }
}
