package jmri.jmrix.marklin.mcan.classify;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ProtocolClassifier}.
 * <p>
 * Classification contract:
 * <ol>
 *   <li>CS2 fingerprint present → CS2_NATIVE</li>
 *   <li>CdB hash pattern present → CDB_CONFIG</li>
 *   <li>Neither → UNKNOWN</li>
 *   <li>Null or wrong length → UNKNOWN (never throws)</li>
 * </ol>
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class ProtocolClassifierTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // --- helpers ---

    /** Builds a 13-byte frame with the given CAN ID (29-bit, big-endian) and DLC. */
    private static int[] frameWith(long canId, int dlc) {
        int[] f = new int[13];
        f[0] = (int)((canId >> 24) & 0xFF);
        f[1] = (int)((canId >> 16) & 0xFF);
        f[2] = (int)((canId >>  8) & 0xFF);
        f[3] = (int)( canId        & 0xFF);
        f[4] = dlc & 0x0F;
        return f;
    }

    /** A 29-bit CAN ID whose hash carries the CS2 fingerprint (bits 9:7 = 110b). */
    private static long cs2CanId(int priority, int command, boolean response) {
        return ((long)(priority & 0x0F) << 25)
             | ((long)(command  & 0xFF) << 17)
             | ((response ? 1L : 0L)    << 16)
             | 0x0300L;  // hash = 0x0300 → fingerprint bits 9:7 = 110b
    }

    /** A 29-bit CAN ID whose hash carries the CdB config pattern (hash & 0x0F80 == 0x0A80). */
    private static long cdbCanId() {
        // hash = 0x0A80 satisfies (hash & 0x0F80) == 0x0A80
        return 0x0A80L;
    }

    // --- CS2_NATIVE ---

    @Test
    public void testClassifiesCs2NativeFromFingerprintInHash() {
        int[] frame = frameWith(cs2CanId(0, 0x05, false), 3);
        ClassifiedFrame result = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, result.protocol());
    }

    @Test
    public void testCs2NativeResultHasCs2Header() {
        int[] frame = frameWith(cs2CanId(2, 0x30, true), 5);
        ClassifiedFrame result = ProtocolClassifier.classify(frame);
        assertNotNull(result.cs2Header());
        assertEquals(2,     result.cs2Header().priority());
        assertEquals(0x30,  result.cs2Header().commandCode());
        assertTrue(result.cs2Header().response());
    }

    @Test
    public void testCs2NativeIsKnownProtocol() {
        int[] frame = frameWith(cs2CanId(0, 0, false), 0);
        assertTrue(ProtocolClassifier.classify(frame).isKnownProtocol());
    }

    // --- CDB_CONFIG ---

    @Test
    public void testClassifiesCdBConfigFromHashPattern() {
        int[] frame = frameWith(cdbCanId(), 0);
        ClassifiedFrame result = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CDB_CONFIG, result.protocol());
    }

    @Test
    public void testCdBConfigIsKnownProtocol() {
        int[] frame = frameWith(cdbCanId(), 0);
        assertTrue(ProtocolClassifier.classify(frame).isKnownProtocol());
    }

    @Test
    public void testCdBConfigCs2HeaderThrows() {
        int[] frame = frameWith(cdbCanId(), 0);
        ClassifiedFrame result = ProtocolClassifier.classify(frame);
        assertThrows(IllegalStateException.class, result::cs2Header);
    }

    // --- UNKNOWN ---

    @Test
    public void testClassifiesUnknownForUnrecognisedFrame() {
        // Hash 0x0000 has no CS2 fingerprint and no CdB pattern
        int[] frame = frameWith(0x0000_0000L, 0);
        ClassifiedFrame result = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.UNKNOWN, result.protocol());
    }

    @Test
    public void testClassifyNullReturnsUnknownWithoutThrowing() {
        ClassifiedFrame result = ProtocolClassifier.classify(null);
        assertNotNull(result);
        assertEquals(FrameProtocol.UNKNOWN, result.protocol());
    }

    @Test
    public void testClassifyWrongLengthReturnsUnknownWithoutThrowing() {
        ClassifiedFrame result12 = ProtocolClassifier.classify(new int[12]);
        ClassifiedFrame result14 = ProtocolClassifier.classify(new int[14]);
        assertEquals(FrameProtocol.UNKNOWN, result12.protocol());
        assertEquals(FrameProtocol.UNKNOWN, result14.protocol());
    }

    @Test
    public void testUnknownIsNotKnownProtocol() {
        int[] frame = frameWith(0x0000_0000L, 0);
        assertFalse(ProtocolClassifier.classify(frame).isKnownProtocol());
    }

    // --- mutual exclusivity ---

    @Test
    public void testCs2FingerprintAndCdBPatternAreMutuallyExclusive() {
        // Verify that no 16-bit hash value satisfies both patterns simultaneously.
        // CS2 fingerprint: (hash >> 7) & 0b111 == 0b110 → hash & 0x0380 = 0x0300
        // CdB pattern:     (hash & 0x0F80) == 0x0A80   → hash & 0x0F80 = 0x0A80
        // If both held: hash & 0x0380 = 0x0300 AND hash & 0x0F80 = 0x0A80
        //   From CdB: bit 11 = 1, bit 10 = 0, bit 9 = 1, bits 8:7 = 0b00
        //   From CS2: bits 9:7 = 0b110  → bit 9 = 1, bit 8 = 1 (contradiction with bit 8 = 0)
        for (int hash = 0; hash <= 0xFFFF; hash++) {
            boolean isCs2 = Cs2Header.hasCs2Fingerprint(hash);
            boolean isCdB = ProtocolClassifier.isCdBConfigPattern(
                    new int[]{0, 0, (hash >> 8) & 0xFF, hash & 0xFF, 0,
                              0, 0, 0, 0, 0, 0, 0, 0});
            assertFalse(isCs2 && isCdB,
                "Hash 0x" + Integer.toHexString(hash) + " matches both CS2 and CdB patterns");
        }
    }
}
