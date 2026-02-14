package jmri.jmrix.marklin.mcan.protocol;

import jmri.jmrix.marklin.mcan.classify.CdBClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link CdBCommissioning}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBCommissioningTest {

    private static final int CAN_CLASS = 0x1A;

    /**
     * Builds a synthetic CdB ping reply frame for a given class, module ID and DLC.
     * CAN ID = pingId + 0x10000 (response bit set).
     */
    private static int[] buildPingReplyFrame(int canClass, int moduleId, int dlc) {
        int pingId = CdBFrameEncoder.buildPingCanId(canClass, moduleId);
        int replyId = pingId + 0x10000;
        int[] frame = new int[13];
        frame[0] = (replyId >> 24) & 0x1F;
        frame[1] = (replyId >> 16) & 0xFF;
        frame[2] = (replyId >>  8) & 0xFF;
        frame[3] =  replyId        & 0xFF;
        frame[4] = dlc & 0xFF;
        return frame;
    }

    private static CdBClassifiedFrame decode(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol(), "Frame not classified as CDB_CONFIG");
        return CdBProtocolDecoder.decode(cf);
    }

    // --- isFactoryFreshPingReply ---

    @Test
    public void testPingReplyFrameClassifiesAsCdBConfig() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 6);
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol());
    }

    @Test
    public void testPingReplyFrameDecodesAsPingReply() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 6);
        CdBClassifiedFrame cdf = decode(frame);
        assertEquals(CdBFrameType.PING_REPLY, cdf.frameType());
    }

    @Test
    public void testIsFactoryFreshPingReplyTrueForModuleZeroDlc6() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 6);
        CdBClassifiedFrame cdf = decode(frame);
        assertTrue(CdBCommissioning.isFactoryFreshPingReply(cdf));
    }

    @Test
    public void testIsFactoryFreshPingReplyFalseForNonZeroModuleId() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 3, 6);
        CdBClassifiedFrame cdf = decode(frame);
        assertFalse(CdBCommissioning.isFactoryFreshPingReply(cdf));
    }

    @Test
    public void testIsFactoryFreshPingReplyFalseForDlc3() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 3);
        CdBClassifiedFrame cdf = decode(frame);
        assertFalse(CdBCommissioning.isFactoryFreshPingReply(cdf));
    }

    @Test
    public void testIsFactoryFreshPingReplyFalseForWriteFrame() {
        int[] frame = CdBModuleSelect.buildSelect(CAN_CLASS, 2);
        CdBClassifiedFrame cdf = decode(frame);
        // A WRITE frame is not PING_REPLY
        assertFalse(CdBCommissioning.isFactoryFreshPingReply(cdf));
    }

    // --- extractChallengeBytes ---

    @Test
    public void testExtractChallengeBytesReturnsTwoElements() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 6);
        frame[6] = 0xAB;
        frame[7] = 0xCD;
        CdBClassifiedFrame cdf = decode(frame);
        Optional<int[]> result = CdBCommissioning.extractChallengeBytes(cdf);
        assertTrue(result.isPresent());
        assertEquals(2, result.get().length);
    }

    @Test
    public void testExtractChallengeBytesExtractsCorrectValues() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 0, 6);
        frame[6] = 0xAB;
        frame[7] = 0xCD;
        CdBClassifiedFrame cdf = decode(frame);
        int[] bytes = CdBCommissioning.extractChallengeBytes(cdf).get();
        assertEquals(0xAB, bytes[0]);
        assertEquals(0xCD, bytes[1]);
    }

    @Test
    public void testExtractChallengeBytesEmptyForCommissionedModule() {
        int[] frame = buildPingReplyFrame(CAN_CLASS, 5, 3);
        CdBClassifiedFrame cdf = decode(frame);
        assertFalse(CdBCommissioning.extractChallengeBytes(cdf).isPresent());
    }

    @Test
    public void testExtractChallengeBytesEmptyForNonReplyFrame() {
        int[] frame = CdBFrameEncoder.buildPing(CAN_CLASS);
        CdBClassifiedFrame cdf = decode(frame);
        assertFalse(CdBCommissioning.extractChallengeBytes(cdf).isPresent());
    }

    // --- buildWriteNewModuleId ---

    @Test
    public void testBuildWriteNewModuleIdProduces13Bytes() {
        assertEquals(13, CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD).length);
    }

    @Test
    public void testBuildWriteNewModuleIdIsWriteFrame() {
        int[] frame = CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD);
        CdBClassifiedFrame cdf = decode(frame);
        assertEquals(CdBFrameType.WRITE, cdf.frameType());
    }

    @Test
    public void testBuildWriteNewModuleIdHasDlc6() {
        int[] frame = CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD);
        assertEquals(6, frame[4] & 0x0F);
    }

    @Test
    public void testBuildWriteNewModuleIdEncodesNewModuleIdBytes() {
        // newId=7 → hi=0, lo=7
        int[] frame = CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD);
        assertEquals(0x00, frame[7] & 0xFF);   // newId high byte
        assertEquals(0x07, frame[8] & 0xFF);   // newId low byte
    }

    @Test
    public void testBuildWriteNewModuleIdEncodesChallengeBytes() {
        int[] frame = CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD);
        assertEquals(0xAB, frame[9]  & 0xFF);
        assertEquals(0xCD, frame[10] & 0xFF);
    }

    @Test
    public void testBuildWriteNewModuleIdTargetsBroadcast() {
        // Must address module 0 (broadcast) since the module has no ID yet
        int[] frame = CdBCommissioning.buildWriteNewModuleId(CAN_CLASS, 7, 0xAB, 0xCD);
        CdBClassifiedFrame cdf = decode(frame);
        assertEquals(0, cdf.moduleId());
    }
}
