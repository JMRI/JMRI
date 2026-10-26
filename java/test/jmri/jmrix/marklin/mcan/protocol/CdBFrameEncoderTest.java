package jmri.jmrix.marklin.mcan.protocol;

import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBFrameEncoder}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBFrameEncoderTest {

    // --- buildPingCanId ---

    @Test
    public void testBuildPingCanIdForLampenChefBroadcast() {
        // LAMPEN_CHEF classId = 0x1A, broadcast = module 0
        // pingId = ((0x1A >> 4) << 21) | ((0x1A & 0xF) << 12) | 0x0A80
        //        = (0x01 << 21) | (0x0A << 12) | 0x0A80
        //        = 0x00200000  | 0x0000A000   | 0x0A80
        //        = 0x0020AA80  (module 0 → low byte = 0x80)
        int canId = CdBFrameEncoder.buildPingCanId(0x1A, 0);
        assertEquals(0x0020AA80, canId);
    }

    @Test
    public void testBuildPingCanIdEncodesModuleAsOffset0x80() {
        int pingForModule1  = CdBFrameEncoder.buildPingCanId(0x1A, 1);
        int pingForModule0  = CdBFrameEncoder.buildPingCanId(0x1A, 0);
        // module 1 → low byte = 0x81; module 0 → low byte = 0x80
        assertEquals(pingForModule0 + 1, pingForModule1);
    }

    // --- buildPing ---

    @Test
    public void testBuildPingProduces13Bytes() {
        int[] frame = CdBFrameEncoder.buildPing(0x1A);
        assertEquals(13, frame.length);
    }

    @Test
    public void testBuildPingHasDlcZero() {
        int[] frame = CdBFrameEncoder.buildPing(0x1A);
        assertEquals(0, frame[4] & 0x0F);
    }

    @Test
    public void testBuildPingCanIdIsEncodedBigEndian() {
        int[] frame  = CdBFrameEncoder.buildPing(0x1A);
        int decoded  = ((frame[0] & 0x1F) << 24)
                     | ((frame[1] & 0xFF) << 16)
                     | ((frame[2] & 0xFF) <<  8)
                     |  (frame[3] & 0xFF);
        assertEquals(CdBFrameEncoder.buildPingCanId(0x1A, 0), decoded);
    }

    // --- buildReadRegister ---

    @Test
    public void testBuildReadRegisterHasDlc2() {
        int[] frame = CdBFrameEncoder.buildReadRegister(0x1A, 3, 0, 5);
        assertEquals(2, frame[4] & 0x0F);
    }

    @Test
    public void testBuildReadRegisterEncodesSlotAndReg() {
        int[] frame = CdBFrameEncoder.buildReadRegister(0x1A, 3, 2, 7);
        assertEquals(2, frame[5]);  // slot
        assertEquals(7, frame[6]);  // reg
    }

    @Test
    public void testBuildReadRegisterCanIdIsReadOffset() {
        int pingId = CdBFrameEncoder.buildPingCanId(0x1A, 3);
        int[] frame = CdBFrameEncoder.buildReadRegister(0x1A, 3, 0, 0);
        int decodedId = ((frame[0] & 0x1F) << 24)
                      | ((frame[1] & 0xFF) << 16)
                      | ((frame[2] & 0xFF) <<  8)
                      |  (frame[3] & 0xFF);
        assertEquals(pingId + 0x40000, decodedId);
    }

    // --- buildWriteRegister ---

    @Test
    public void testBuildWriteRegisterHasDlc4() {
        int[] frame = CdBFrameEncoder.buildWriteRegister(0x1A, 3, 0, 5, 0x1234);
        assertEquals(4, frame[4] & 0x0F);
    }

    @Test
    public void testBuildWriteRegisterEncodesSlotRegAndValue() {
        int[] frame = CdBFrameEncoder.buildWriteRegister(0x1A, 3, 1, 4, 0xABCD);
        assertEquals(1,    frame[5]);         // slot
        assertEquals(4,    frame[6]);         // reg
        assertEquals(0xAB, frame[7] & 0xFF);  // value hi
        assertEquals(0xCD, frame[8] & 0xFF);  // value lo
    }

    @Test
    public void testBuildWriteRegisterCanIdIsWriteOffset() {
        int pingId = CdBFrameEncoder.buildPingCanId(0x1A, 3);
        int[] frame = CdBFrameEncoder.buildWriteRegister(0x1A, 3, 0, 0, 0);
        int decodedId = ((frame[0] & 0x1F) << 24)
                      | ((frame[1] & 0xFF) << 16)
                      | ((frame[2] & 0xFF) <<  8)
                      |  (frame[3] & 0xFF);
        assertEquals(pingId + 0x60000, decodedId);
    }

    // --- extractReadReplyValue ---

    @Test
    public void testExtractReadReplyValueFromDlc4Frame() {
        int[] frame = new int[13];
        frame[4] = 4;
        frame[7] = 0x12;
        frame[8] = 0x34;
        Optional<Integer> val = CdBFrameEncoder.extractReadReplyValue(frame);
        assertTrue(val.isPresent());
        assertEquals(0x1234, val.get());
    }

    @Test
    public void testExtractReadReplyValueEmptyForDlcUnder4() {
        int[] frame = new int[13];
        frame[4] = 2;
        assertFalse(CdBFrameEncoder.extractReadReplyValue(frame).isPresent());
    }

    @Test
    public void testExtractReadReplyValueEmptyForNull() {
        assertFalse(CdBFrameEncoder.extractReadReplyValue(null).isPresent());
    }
}
