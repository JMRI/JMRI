package jmri.jmrix.marklin.mcan.classify;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Cs2Header}.
 * <p>
 * A CS2 frame carries hash fingerprint {@code (hash >> 7) & 0b111 == 0b110},
 * meaning bits 9:7 of the 16-bit hash must be {@code 110b} (0x300 when isolated).
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class Cs2HeaderTest {

    // Build a minimal valid CS2 packet: hash=0x0300 (fingerprint bits set), DLC=0
    private static int[] minimalCs2Packet(int priority, int command, boolean response,
                                          int hash, int dlc) {
        long canId = ((long)(priority & 0x0F) << 25)
                   | ((long)(command  & 0xFF) << 17)
                   | ((response ? 1L : 0L)    << 16)
                   | (hash & 0xFFFFL);
        int[] pkt = new int[13];
        pkt[0] = (int)((canId >> 24) & 0xFF);
        pkt[1] = (int)((canId >> 16) & 0xFF);
        pkt[2] = (int)((canId >>  8) & 0xFF);
        pkt[3] = (int)( canId        & 0xFF);
        pkt[4] = dlc & 0x0F;
        return pkt;
    }

    @Test
    public void testHasCs2FingerprintReturnsTrueForValidFingerprint() {
        // bits 9:7 = 110b → hash & 0x0380 = 0x0300
        assertTrue(Cs2Header.hasCs2Fingerprint(0x0300));
        assertTrue(Cs2Header.hasCs2Fingerprint(0x037F));  // with lower bits set
        assertTrue(Cs2Header.hasCs2Fingerprint(0xFF00 | 0x0300));  // upper bits irrelevant
    }

    @Test
    public void testHasCs2FingerprintReturnsFalseForCdBPattern() {
        // CdB hash pattern 0x0A80 → bits 9:7 = 101b → fingerprint absent
        assertFalse(Cs2Header.hasCs2Fingerprint(0x0A80));
        assertFalse(Cs2Header.hasCs2Fingerprint(0x0000));
        assertFalse(Cs2Header.hasCs2Fingerprint(0x0180));  // bits 9:7 = 011b
    }

    @Test
    public void testIsValidDlcAcceptsRange0To8() {
        for (int dlc = 0; dlc <= 8; dlc++) {
            assertTrue(Cs2Header.isValidDlc(dlc), "DLC " + dlc + " should be valid");
        }
    }

    @Test
    public void testIsValidDlcRejectsOutOfRange() {
        assertFalse(Cs2Header.isValidDlc(-1));
        assertFalse(Cs2Header.isValidDlc(9));
        assertFalse(Cs2Header.isValidDlc(15));
    }

    @Test
    public void testTryParseExtractsPriorityCommandResponseHashDlc() {
        int[] pkt = minimalCs2Packet(2, 0x30, true, 0x0300, 5);
        Cs2Header h = Cs2Header.tryParse(pkt);
        assertNotNull(h);
        assertEquals(2,     h.priority());
        assertEquals(0x30,  h.commandCode());
        assertTrue(h.response());
        assertEquals(0x0300, h.hash());
        assertEquals(5,     h.dlc());
    }

    @Test
    public void testTryParseReturnsNullForCdBHashPattern() {
        // CdB config pattern  -  fingerprint absent
        int[] pkt = minimalCs2Packet(0, 0, false, 0x0A80, 0);
        assertNull(Cs2Header.tryParse(pkt));
    }

    @Test
    public void testTryParseReturnsNullForNull() {
        assertNull(Cs2Header.tryParse(null));
    }

    @Test
    public void testTryParseReturnsNullForShortPacket() {
        assertNull(Cs2Header.tryParse(new int[4]));
    }

    @Test
    public void testTryParseReturnsNullForInvalidDlc() {
        // Build a packet where DLC nibble is 9 (out of range)
        int[] pkt = minimalCs2Packet(0, 0, false, 0x0300, 0);
        pkt[4] = 0x09;  // DLC=9 is out of valid range
        assertNull(Cs2Header.tryParse(pkt));
    }

    // --- priority sub-fields (2+2 bit split) ---

    @Test
    public void testBusPriorityIsUpperTwoBitsOfPriorityField() {
        // priority = 0b1011 → busPriority = 0b10 = 2, messagePriority = 0b11 = 3
        int[] pkt = minimalCs2Packet(0b1011, 0, false, 0x0300, 0);
        Cs2Header h = Cs2Header.tryParse(pkt);
        assertNotNull(h);
        assertEquals(0b10, h.busPriority());
    }

    @Test
    public void testMessagePriorityIsLowerTwoBitsOfPriorityField() {
        // priority = 0b1011 → messagePriority = 0b11 = 3
        int[] pkt = minimalCs2Packet(0b1011, 0, false, 0x0300, 0);
        Cs2Header h = Cs2Header.tryParse(pkt);
        assertNotNull(h);
        assertEquals(0b11, h.messagePriority());
    }

    @Test
    public void testBusPriorityAndMessagePriorityReconstitutePriorityField() {
        for (int p = 0; p < 16; p++) {
            int[] pkt = minimalCs2Packet(p, 0, false, 0x0300, 0);
            Cs2Header h = Cs2Header.tryParse(pkt);
            assertNotNull(h);
            int reconstituted = (h.busPriority() << 2) | h.messagePriority();
            assertEquals(p, reconstituted,
                    "busPriority<<2 | messagePriority must equal priority() for priority=" + p);
        }
    }

    @Test
    public void testMessagePriorityTier3IsLocoAccessoryCommand() {
        // Tier 3 (messagePriority=3) is the loco/accessory command tier per the spec.
        // priority = 0b0011 → busPriority=0, messagePriority=3
        int[] pkt = minimalCs2Packet(0b0011, 0x0B, false, 0x0300, 4);
        Cs2Header h = Cs2Header.tryParse(pkt);
        assertNotNull(h);
        assertEquals(3, h.messagePriority());
        assertEquals(0, h.busPriority());
    }
}
