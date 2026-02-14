package jmri.jmrix.marklin.mcan.protocol;

import jmri.jmrix.marklin.mcan.classify.CdBClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBProtocolDecoder}.
 * <p>
 * Frames are constructed by {@link CdBFrameEncoder} and first-classified by
 * {@link ProtocolClassifier}, then decoded.  This makes the tests insensitive
 * to the exact byte layout and verifies the encoder-decoder round-trip.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBProtocolDecoderTest {

    private static final int CLASS_LAMPEN_CHEF = 0x1A;
    private static final int MODULE_3          = 3;

    private static ClassifiedFrame classify(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol(),
                "Encoder produced a frame that is not CDB_CONFIG");
        return cf;
    }

    // --- ping ---

    @Test
    public void testDecodePingFrameTypeIsPing() {
        int[] frame = CdBFrameEncoder.buildPing(CLASS_LAMPEN_CHEF);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CdBFrameType.PING, result.frameType());
    }

    @Test
    public void testDecodePingExtractsClass() {
        int[] frame = CdBFrameEncoder.buildPing(CLASS_LAMPEN_CHEF);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CLASS_LAMPEN_CHEF, result.canClass());
    }

    @Test
    public void testDecodePingModuleIdIsBroadcast() {
        int[] frame = CdBFrameEncoder.buildPing(CLASS_LAMPEN_CHEF);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(0, result.moduleId());
    }

    // --- read ---

    @Test
    public void testDecodeReadFrameTypeIsRead() {
        int[] frame = CdBFrameEncoder.buildReadRegister(CLASS_LAMPEN_CHEF, MODULE_3, 0, 5);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CdBFrameType.READ, result.frameType());
    }

    @Test
    public void testDecodeReadExtractsClassAndModule() {
        int[] frame = CdBFrameEncoder.buildReadRegister(CLASS_LAMPEN_CHEF, MODULE_3, 0, 5);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CLASS_LAMPEN_CHEF, result.canClass());
        assertEquals(MODULE_3,          result.moduleId());
    }

    // --- write ---

    @Test
    public void testDecodeWriteFrameTypeIsWrite() {
        int[] frame = CdBFrameEncoder.buildWriteRegister(CLASS_LAMPEN_CHEF, MODULE_3, 0, 5, 0x1234);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CdBFrameType.WRITE, result.frameType());
    }

    @Test
    public void testDecodeWriteExtractsClassAndModule() {
        int[] frame = CdBFrameEncoder.buildWriteRegister(CLASS_LAMPEN_CHEF, MODULE_3, 1, 4, 0xABCD);
        CdBClassifiedFrame result = CdBProtocolDecoder.decode(classify(frame));
        assertEquals(CLASS_LAMPEN_CHEF, result.canClass());
        assertEquals(MODULE_3,          result.moduleId());
    }

    // --- error handling ---

    @Test
    public void testDecodeThrowsForNonCdBFrame() {
        // Build a CS2-native frame and classify it
        int[] cs2Frame = new int[13];
        cs2Frame[2] = 0x03;  // hash = 0x0300 → CS2 fingerprint
        ClassifiedFrame cf = ProtocolClassifier.classify(cs2Frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
        assertThrows(IllegalArgumentException.class, () -> CdBProtocolDecoder.decode(cf));
    }

    @Test
    public void testDecodeIsKnownTypeForPingReadWrite() {
        int[] ping  = CdBFrameEncoder.buildPing(CLASS_LAMPEN_CHEF);
        int[] read  = CdBFrameEncoder.buildReadRegister(CLASS_LAMPEN_CHEF, MODULE_3, 0, 0);
        int[] write = CdBFrameEncoder.buildWriteRegister(CLASS_LAMPEN_CHEF, MODULE_3, 0, 0, 0);

        assertTrue(CdBProtocolDecoder.decode(classify(ping)).isKnownType());
        assertTrue(CdBProtocolDecoder.decode(classify(read)).isKnownType());
        assertTrue(CdBProtocolDecoder.decode(classify(write)).isKnownType());
    }
}
