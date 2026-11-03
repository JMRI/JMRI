package jmri.jmrix.marklin.mcan.cs2;
import jmri.jmrix.marklin.mcan.FrameProtocol;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2DecoderCommands}.
 */
public class Cs2DecoderCommandsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static Cs2Frame roundtrip(int[] frame) {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return cf;
    }

    // --- emergency stop ---

    @Test
    public void testBuildLocoEmergencyStopProduces13Bytes() {
        assertEquals(13, Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L).length);
    }

    @Test
    public void testBuildLocoEmergencyStopHasCommandCode() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L));
        assertEquals(Cs2DecoderCommands.COMMAND_LOCO_EMERGENCY_STOP,
                cf.header().commandCode());
    }

    @Test
    public void testBuildLocoEmergencyStopIsNotResponse() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L));
        assertFalse(cf.header().response());
    }

    @Test
    public void testBuildLocoEmergencyStopHasDlc4() {
        int[] frame = Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L);
        assertEquals(4, frame[4]);
    }

    @Test
    public void testBuildLocoEmergencyStopEncodesAddress() {
        long addr = 0x12345678L;
        int[] frame = Cs2DecoderCommands.buildLocoEmergencyStop(addr);
        assertEquals(0x12, frame[5]);
        assertEquals(0x34, frame[6]);
        assertEquals(0x56, frame[7]);
        assertEquals(0x78, frame[8]);
    }

    // --- loco speed ---

    @Test
    public void testBuildLocoSpeedHasCommandCode() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoSpeed(0x4000L, 100));
        assertEquals(Cs2DecoderCommands.COMMAND_LOCO_SPEED, cf.header().commandCode());
    }

    @Test
    public void testBuildLocoSpeedHasDlc6() {
        int[] frame = Cs2DecoderCommands.buildLocoSpeed(0x4000L, 100);
        assertEquals(6, frame[4]);
    }

    @Test
    public void testBuildLocoSpeedQueryHasDlc4() {
        int[] frame = Cs2DecoderCommands.buildLocoSpeed(0x4000L, -1);
        assertEquals(4, frame[4]);
    }

    @Test
    public void testBuildLocoSpeedEncodesSpeed() {
        int[] frame = Cs2DecoderCommands.buildLocoSpeed(0x4000L, 0x0200);
        assertEquals(0x02, frame[9]);
        assertEquals(0x00, frame[10]);
    }

    // --- loco direction ---

    @Test
    public void testBuildLocoDirectionHasCommandCode() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoDirection(0x4000L, 1));
        assertEquals(Cs2DecoderCommands.COMMAND_LOCO_DIRECTION, cf.header().commandCode());
    }

    @Test
    public void testBuildLocoDirectionHasDlc5() {
        int[] frame = Cs2DecoderCommands.buildLocoDirection(0x4000L, 1);
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildLocoDirectionQueryHasDlc4() {
        int[] frame = Cs2DecoderCommands.buildLocoDirection(0x4000L, -1);
        assertEquals(4, frame[4]);
    }

    // --- loco function ---

    @Test
    public void testBuildLocoFunctionHasCommandCode() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoFunction(0x4000L, 0, true));
        assertEquals(Cs2DecoderCommands.COMMAND_LOCO_FUNCTION, cf.header().commandCode());
    }

    @Test
    public void testBuildLocoFunctionHasDlc6() {
        int[] frame = Cs2DecoderCommands.buildLocoFunction(0x4000L, 0, true);
        assertEquals(6, frame[4]);
    }

    @Test
    public void testBuildLocoFunctionQueryHasDlc5() {
        int[] frame = Cs2DecoderCommands.buildLocoFunctionQuery(0x4000L, 0);
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildLocoFunctionEncodesOnOff() {
        int[] on  = Cs2DecoderCommands.buildLocoFunction(0x4000L, 3, true);
        int[] off = Cs2DecoderCommands.buildLocoFunction(0x4000L, 3, false);
        assertEquals(1, on[10]);
        assertEquals(0, off[10]);
    }

    // --- MFX bind ---

    @Test
    public void testBuildMfxBindHasCommandCode() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildMfxBind(0x12345678L, 0x0001));
        assertEquals(Cs2DecoderCommands.COMMAND_MFX_BIND, cf.header().commandCode());
    }

    @Test
    public void testBuildMfxBindHasDlc6() {
        int[] frame = Cs2DecoderCommands.buildMfxBind(0x12345678L, 0x0001);
        assertEquals(6, frame[4]);
    }

    // --- CS2_NATIVE classification ---

    @Test
    public void testBuildLocoEmergencyStopClassifiesAsCs2Native() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    @Test
    public void testBuildLocoSpeedClassifiesAsCs2Native() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2DecoderCommands.buildLocoSpeed(0x4000L, 500));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    // --- LocoEmergencyStopFrame roundtrip ---

    @Test
    public void testLocoEmergencyStopRoundtripsToLocoEmergencyStopFrame() {
        Cs2Frame parsed = roundtrip(Cs2DecoderCommands.buildLocoEmergencyStop(0x4000L));
        assertInstanceOf(Cs2DecoderCommands.LocoEmergencyStopFrame.class, parsed);
    }

    @Test
    public void testLocoEmergencyStopFrameAddress() {
        Cs2Frame parsed = roundtrip(Cs2DecoderCommands.buildLocoEmergencyStop(0x12345678L));
        assertEquals(0x12345678L, ((Cs2DecoderCommands.LocoEmergencyStopFrame) parsed).locoAddress());
    }

    // --- ReadConfigReplyFrame roundtrip ---

    private static int[] buildReadConfigReply(long addr, int cvIndex, int cvNum, int value) {
        // Pack: upper 6 bits = cvIndex, lower 10 bits = cvNum, split across bytes 4-5
        int packed = ((cvIndex & 0x3F) << 10) | (cvNum & 0x3FF);
        int[] data = new int[]{
            (int)((addr >> 24) & 0xFF), (int)((addr >> 16) & 0xFF),
            (int)((addr >>  8) & 0xFF), (int)( addr        & 0xFF),
            (packed >> 8) & 0xFF, packed & 0xFF,
            value & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_READ_CONFIG,
                true, Cs2DecoderCommands.BROADCAST_HASH, 7, data);
    }

    @Test
    public void testReadConfigReplyRoundtripsToReadConfigReplyFrame() {
        Cs2Frame parsed = roundtrip(buildReadConfigReply(0x4000L, 0, 1, 42));
        assertInstanceOf(Cs2DecoderCommands.ReadConfigReplyFrame.class, parsed);
    }

    @Test
    public void testReadConfigReplyFrameLocoAddress() {
        Cs2Frame parsed = roundtrip(buildReadConfigReply(0xC001L, 0, 1, 99));
        assertEquals(0xC001L, ((Cs2DecoderCommands.ReadConfigReplyFrame) parsed).locoAddress());
    }

    @Test
    public void testReadConfigReplyFrameCvNumber() {
        Cs2Frame parsed = roundtrip(buildReadConfigReply(0x4000L, 0, 29, 3));
        assertEquals(29, ((Cs2DecoderCommands.ReadConfigReplyFrame) parsed).cvNumber());
    }

    @Test
    public void testReadConfigReplyFrameValue() {
        Cs2Frame parsed = roundtrip(buildReadConfigReply(0x4000L, 0, 1, 0x7F));
        assertEquals(0x7F, ((Cs2DecoderCommands.ReadConfigReplyFrame) parsed).value());
    }

    @Test
    public void testReadConfigReplyDlc6IsNotTyped() {
        // DLC=6 = failure case  -  returns base frame, not ReadConfigReplyFrame
        int[] data = new int[]{0, 0, 0x40, 0, 0, 0};
        int[] frame = Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_READ_CONFIG,
                true, Cs2DecoderCommands.BROADCAST_HASH, 6, data);
        Cs2Frame parsed = roundtrip(frame);
        assertFalse(parsed instanceof Cs2DecoderCommands.ReadConfigReplyFrame,
                "DLC=6 failure reply must not be typed as ReadConfigReplyFrame");
    }

    // --- WriteConfigReplyFrame roundtrip ---

    private static int[] buildWriteConfigReply(long addr, int cvNum, int value, int flags) {
        int[] data = new int[]{
            (int)((addr >> 24) & 0xFF), (int)((addr >> 16) & 0xFF),
            (int)((addr >>  8) & 0xFF), (int)( addr        & 0xFF),
            (cvNum >> 8) & 0xFF, cvNum & 0xFF,
            value & 0xFF, flags & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_WRITE_CONFIG,
                true, Cs2DecoderCommands.BROADCAST_HASH, 8, data);
    }

    @Test
    public void testWriteConfigReplyRoundtripsToWriteConfigReplyFrame() {
        Cs2Frame parsed = roundtrip(buildWriteConfigReply(0x4000L, 1, 3, 0xC0));
        assertInstanceOf(Cs2DecoderCommands.WriteConfigReplyFrame.class, parsed);
    }

    @Test
    public void testWriteConfigReplyFrameWriteAcknowledged() {
        Cs2Frame parsed = roundtrip(buildWriteConfigReply(0x4000L, 1, 3, 0x80));
        assertTrue(((Cs2DecoderCommands.WriteConfigReplyFrame) parsed).writeAcknowledged());
    }

    @Test
    public void testWriteConfigReplyFrameVerifySucceeded() {
        Cs2Frame parsed = roundtrip(buildWriteConfigReply(0x4000L, 1, 3, 0x40));
        assertTrue(((Cs2DecoderCommands.WriteConfigReplyFrame) parsed).verifySucceeded());
    }

    @Test
    public void testWriteConfigReplyFrameNoAck() {
        Cs2Frame parsed = roundtrip(buildWriteConfigReply(0x4000L, 1, 3, 0x00));
        assertFalse(((Cs2DecoderCommands.WriteConfigReplyFrame) parsed).writeAcknowledged());
        assertFalse(((Cs2DecoderCommands.WriteConfigReplyFrame) parsed).verifySucceeded());
    }

    // --- MfxBindReplyFrame roundtrip ---

    private static int[] buildMfxBindReply(long uid, int sid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            (sid >> 8) & 0xFF, sid & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_MFX_BIND,
                true, Cs2DecoderCommands.BROADCAST_HASH, 6, data);
    }

    @Test
    public void testMfxBindReplyRoundtripsToMfxBindReplyFrame() {
        Cs2Frame parsed = roundtrip(buildMfxBindReply(0xDEADBEEFL, 0x0001));
        assertInstanceOf(Cs2DecoderCommands.MfxBindReplyFrame.class, parsed);
    }

    @Test
    public void testMfxBindReplyFrameUid() {
        Cs2Frame parsed = roundtrip(buildMfxBindReply(0x12345678L, 0x00FF));
        assertEquals(0x12345678L, ((Cs2DecoderCommands.MfxBindReplyFrame) parsed).mfxUid());
    }

    @Test
    public void testMfxBindReplyFrameSid() {
        Cs2Frame parsed = roundtrip(buildMfxBindReply(0x12345678L, 0x0042));
        assertEquals(0x0042, ((Cs2DecoderCommands.MfxBindReplyFrame) parsed).mfxSid());
    }

    // --- MfxVerifyReplyFrame roundtrip ---

    private static int[] buildMfxVerifyPositiveReply(long uid, int sid, int ask) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            (sid >> 8) & 0xFF, sid & 0xFF, ask & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_MFX_VERIFY,
                true, Cs2DecoderCommands.BROADCAST_HASH, 7, data);
    }

    private static int[] buildMfxVerifyNegativeReply(long uid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            0x00, 0x00
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2DecoderCommands.COMMAND_MFX_VERIFY,
                true, Cs2DecoderCommands.BROADCAST_HASH, 6, data);
    }

    @Test
    public void testMfxVerifyPositiveRoundtripsToMfxVerifyReplyFrame() {
        Cs2Frame parsed = roundtrip(buildMfxVerifyPositiveReply(0xAABBCCDDL, 5, 200));
        assertInstanceOf(Cs2DecoderCommands.MfxVerifyReplyFrame.class, parsed);
    }

    @Test
    public void testMfxVerifyPositiveFrameIsPositive() {
        Cs2Frame parsed = roundtrip(buildMfxVerifyPositiveReply(0xAABBCCDDL, 5, 200));
        assertTrue(((Cs2DecoderCommands.MfxVerifyReplyFrame) parsed).positive());
    }

    @Test
    public void testMfxVerifyPositiveFrameAskRatio() {
        Cs2Frame parsed = roundtrip(buildMfxVerifyPositiveReply(0xAABBCCDDL, 5, 200));
        assertEquals(200, ((Cs2DecoderCommands.MfxVerifyReplyFrame) parsed).askRatio());
    }

    @Test
    public void testMfxVerifyNegativeFrameIsNotPositive() {
        Cs2Frame parsed = roundtrip(buildMfxVerifyNegativeReply(0xAABBCCDDL));
        assertFalse(((Cs2DecoderCommands.MfxVerifyReplyFrame) parsed).positive());
    }

    @Test
    public void testMfxVerifyNegativeFrameAskRatioIsZero() {
        Cs2Frame parsed = roundtrip(buildMfxVerifyNegativeReply(0xAABBCCDDL));
        assertEquals(0, ((Cs2DecoderCommands.MfxVerifyReplyFrame) parsed).askRatio());
    }
}
