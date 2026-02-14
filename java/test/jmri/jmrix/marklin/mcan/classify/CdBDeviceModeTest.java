package jmri.jmrix.marklin.mcan.classify;

import jmri.jmrix.marklin.mcan.protocol.CdBFrameEncoder;
import jmri.jmrix.marklin.mcan.protocol.CdBProtocolDecoder;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBDeviceMode}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBDeviceModeTest {

    private static final int CAN_CLASS = 0x1A;

    /** Builds a synthetic CdB ping reply frame. */
    private static CdBClassifiedFrame pingReply(int moduleId, int dlc) {
        int pingId  = CdBFrameEncoder.buildPingCanId(CAN_CLASS, moduleId);
        int replyId = pingId + 0x10000;
        int[] frame = new int[13];
        frame[0] = (replyId >> 24) & 0x1F;
        frame[1] = (replyId >> 16) & 0xFF;
        frame[2] = (replyId >>  8) & 0xFF;
        frame[3] =  replyId        & 0xFF;
        frame[4] = dlc & 0xFF;
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        return CdBProtocolDecoder.decode(cf);
    }

    /** Builds a synthetic CdB read-reply frame. */
    private static CdBClassifiedFrame readReply(int moduleId) {
        int pingId   = CdBFrameEncoder.buildPingCanId(CAN_CLASS, moduleId);
        int readReplyId = pingId + 0x50000;
        int[] frame = new int[13];
        frame[0] = (readReplyId >> 24) & 0x1F;
        frame[1] = (readReplyId >> 16) & 0xFF;
        frame[2] = (readReplyId >>  8) & 0xFF;
        frame[3] =  readReplyId        & 0xFF;
        frame[4] = 4;  // DLC=4 for read reply
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        return CdBProtocolDecoder.decode(cf);
    }

    // --- FACTORY mode ---

    @Test
    public void testFactoryModeFromPingReplyModuleZeroDlc6() {
        Optional<CdBDeviceMode> mode = CdBDeviceMode.fromFrame(pingReply(0, 6));
        assertTrue(mode.isPresent());
        assertEquals(CdBDeviceMode.FACTORY, mode.get());
    }

    @Test
    public void testFactoryModeNotFromPingReplyModuleZeroDlc3() {
        Optional<CdBDeviceMode> mode = CdBDeviceMode.fromFrame(pingReply(0, 3));
        assertFalse(mode.isPresent());
    }

    // --- OPERATIONAL mode ---

    @Test
    public void testOperationalModeFromPingReplyNonZeroModuleDlc3() {
        Optional<CdBDeviceMode> mode = CdBDeviceMode.fromFrame(pingReply(5, 3));
        assertTrue(mode.isPresent());
        assertEquals(CdBDeviceMode.OPERATIONAL, mode.get());
    }

    @Test
    public void testOperationalModeNotFromPingReplyNonZeroModuleDlc6() {
        // DLC=6 with a non-zero module is not a known shape  -  empty
        Optional<CdBDeviceMode> mode = CdBDeviceMode.fromFrame(pingReply(5, 6));
        assertFalse(mode.isPresent());
    }

    // --- SELECTED mode ---

    @Test
    public void testSelectedModeFromReadReply() {
        Optional<CdBDeviceMode> mode = CdBDeviceMode.fromFrame(readReply(3));
        assertTrue(mode.isPresent());
        assertEquals(CdBDeviceMode.SELECTED, mode.get());
    }

    // --- empty for non-reply frames ---

    @Test
    public void testEmptyForPingFrame() {
        int[] frame = CdBFrameEncoder.buildPing(CAN_CLASS);
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        CdBClassifiedFrame cdf = CdBProtocolDecoder.decode(cf);
        assertFalse(CdBDeviceMode.fromFrame(cdf).isPresent());
    }

    @Test
    public void testEmptyForReadRequestFrame() {
        int[] frame = CdBFrameEncoder.buildReadRegister(CAN_CLASS, 3, 0, 1);
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        CdBClassifiedFrame cdf = CdBProtocolDecoder.decode(cf);
        assertFalse(CdBDeviceMode.fromFrame(cdf).isPresent());
    }

    @Test
    public void testEmptyForWriteFrame() {
        int[] frame = CdBFrameEncoder.buildWriteRegister(CAN_CLASS, 3, 0, 1, 0x1234);
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        CdBClassifiedFrame cdf = CdBProtocolDecoder.decode(cf);
        assertFalse(CdBDeviceMode.fromFrame(cdf).isPresent());
    }
}
