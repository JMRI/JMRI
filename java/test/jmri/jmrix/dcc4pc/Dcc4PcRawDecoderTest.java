package jmri.jmrix.dcc4pc;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Dcc4PcRawDecoder, mostly against the worked example in section 9.3
 * of the DCC4PC Omnibus specification.
 *
 * @author swalton00 Copyright (C) 2026
 */
public class Dcc4PcRawDecoderTest {

    /** The worked example in Omnibus spec section 9.3: 16 inputs, all enabled. */
    private static final int[] SPEC_EXAMPLE = {
        0x42, 0x01, 0x3F, 0xA8, 0x90, 0x90, 0xC0, 0x00, 0x00,
        0x42, 0x02, 0xA3, 0xAC, 0x8B, 0x69, 0x4E, 0xA5, 0x99,
        0x9A, 0x01, 0x01, 0x91, 0x04, 0x22, 0x99, 0xA5, 0xA3,
        0xAC, 0x81, 0x42, 0x03, 0x3F, 0xBC, 0x80, 0x90, 0xC0,
        0x00, 0x20, 0x24, 0x8E, 0xD4, 0x4E, 0x59, 0xA3, 0xAC};

    private static Dcc4PcRawDecoder sixteenInputs() {
        int[] inputs = new int[16];
        for (int i = 0; i < 16; i++) {
            inputs[i] = i + 1;
        }
        return new Dcc4PcRawDecoder(inputs);
    }

    private static void assertData(Dcc4PcRawDecoder.InputData d, int input, int duplicateOf, int... data) {
        assertEquals(input, d.input, "input");
        assertEquals(duplicateOf, d.duplicateOf, "duplicateOf");
        assertArrayEquals(data, d.data, "data");
    }

    @Test
    public void testDecodesSpecExample() {
        List<Dcc4PcRawDecoder.Packet> packets = sixteenInputs().decode(SPEC_EXAMPLE);
        assertEquals(4, packets.size());

        // packet 1 (type 2): inputs 3 and 7 occupied, 4 and 8 orientation A, 12 orientation B
        Dcc4PcRawDecoder.Packet p1 = packets.get(0);
        assertEquals(Dcc4PcRawDecoder.Kind.DCC, p1.kind);
        assertTrue(p1.explicitStates);
        assertArrayEquals(new int[]{0x01, 0x3F, 0xA8}, p1.dcc);
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 0, 1, 2, 0, 0, 0, 3, 0, 0, 0, 0}, p1.states);
        assertEquals(3, p1.railCom.size());
        assertData(p1.railCom.get(0), 4, 0, 0xA3, 0xAC);
        assertData(p1.railCom.get(1), 8, 0, 0x8B, 0x69, 0x4E, 0xA5);
        assertData(p1.railCom.get(2), 12, 0, 0x99, 0x9A);

        // packet 2 (type 1): states inherited, input 8 repeats its previous data
        Dcc4PcRawDecoder.Packet p2 = packets.get(1);
        assertFalse(p2.explicitStates);
        assertArrayEquals(p1.states, p2.states);
        assertData(p2.railCom.get(0), 4, 0, 0x99, 0xA5);
        assertData(p2.railCom.get(1), 8, 1, 0x8B, 0x69, 0x4E, 0xA5);
        assertArrayEquals(p1.dcc, p2.railCom.get(1).sourceDcc);
        assertData(p2.railCom.get(2), 12, 0, 0xA3, 0xAC);

        assertEquals(Dcc4PcRawDecoder.Kind.OVERFLOW, packets.get(2).kind);

        // packet 4 (type 2): input 3 now unoccupied. Input 12 repeats its second
        // previous data; the overflow doesn't count, so that is packet 1's.
        Dcc4PcRawDecoder.Packet p4 = packets.get(3);
        assertArrayEquals(new int[]{0, 0, 0, 2, 0, 0, 1, 2, 0, 0, 0, 3, 0, 0, 0, 0}, p4.states);
        // the spec prints 0x8E here but decodes 0x8B in step 23
        assertData(p4.railCom.get(0), 4, 0, 0x8E, 0xD4, 0x4E, 0x59);
        assertData(p4.railCom.get(1), 8, 0, 0xA3, 0xAC);
        assertData(p4.railCom.get(2), 12, 2, 0x99, 0x9A);
        assertArrayEquals(p1.dcc, p4.railCom.get(2).sourceDcc);
    }

    @Test
    public void testJoinsPacketSplitAcrossReplies() {
        Dcc4PcRawDecoder decoder = sixteenInputs();
        assertEquals(0, decoder.decode(Arrays.copyOfRange(SPEC_EXAMPLE, 0, 12)).size());
        assertEquals(4, decoder.decode(Arrays.copyOfRange(SPEC_EXAMPLE, 12, SPEC_EXAMPLE.length)).size());
    }

    @Test
    public void testSixInputsUseTwoStateBytes() {
        // inputs 1-6 enabled: J3 occupied without RailCom, J4 orientation A with
        // ADR_HIGH 0xA4 (long 9328); then a type 1 packet with J4's ADR_LOW 0x70
        Dcc4PcRawDecoder decoder = new Dcc4PcRawDecoder(new int[]{1, 2, 3, 4, 5, 6});
        List<Dcc4PcRawDecoder.Packet> packets = decoder.decode(new int[]{
            0x42, 0xE4, 0x70, 0x90, 0x90, 0x00, 0x00, 0x02, 0x9C, 0x47,
            0x02, 0xE4, 0x70, 0xB8, 0x00, 0x02, 0x95, 0xC6});
        assertEquals(2, packets.size());
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 0}, packets.get(0).states);
        assertData(packets.get(0).railCom.get(0), 4, 0, 0x9C, 0x47);
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 0}, packets.get(1).states);
        assertData(packets.get(1).railCom.get(0), 4, 0, 0x95, 0xC6);
    }

    @Test
    public void testUnknownCommandEndsTheChunk() {
        List<Dcc4PcRawDecoder.Packet> packets = sixteenInputs().decode(new int[]{0x90, 0x01, 0x02});
        assertEquals(1, packets.size());
        assertEquals(Dcc4PcRawDecoder.Kind.UNKNOWN_COMMAND, packets.get(0).kind);
    }

    @Test
    public void testResetClearsStates() {
        Dcc4PcRawDecoder decoder = sixteenInputs();
        decoder.decode(Arrays.copyOfRange(SPEC_EXAMPLE, 0, 19));
        decoder.reset();
        // a type 1 packet with no inputs carrying data is now complete with no further bytes
        Dcc4PcRawDecoder.Packet p = decoder.decode(new int[]{0x01, 0xFF, 0x00}).get(0);
        assertArrayEquals(new int[16], p.states);
        assertTrue(p.railCom.isEmpty());
    }

}
