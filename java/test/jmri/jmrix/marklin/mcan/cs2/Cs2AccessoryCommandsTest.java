package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2AccessoryCommands}.
 */
public class Cs2AccessoryCommandsTest {

    private static Cs2ClassifiedFrame roundtrip(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return Cs2FrameParser.parse(cf);
    }

    // --- buildSetAccessory ---

    @Test
    public void testBuildSetAccessoryProduces13Bytes() {
        assertEquals(13, Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true).length);
    }

    @Test
    public void testBuildSetAccessoryClassifiesAsCs2Native() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true));
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    @Test
    public void testBuildSetAccessoryHasCommandCode0x0B() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true));
        assertEquals(0x0B, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildSetAccessoryIsNotResponse() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true));
        assertFalse(cf.cs2Header().response());
    }

    @Test
    public void testBuildSetAccessoryHasDlc6() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true);
        assertEquals(6, frame[4]);
    }

    @Test
    public void testBuildSetAccessoryEncodesAddress() {
        long addr = 0x00003800L;
        int[] frame = Cs2AccessoryCommands.buildSetAccessory(addr, 1, false);
        assertEquals(0x00, frame[5]);
        assertEquals(0x00, frame[6]);
        assertEquals(0x38, frame[7]);
        assertEquals(0x00, frame[8]);
    }

    @Test
    public void testBuildSetAccessoryEncodesAspect() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessory(0x3000L, 2, false);
        assertEquals(2, frame[9]);
    }

    @Test
    public void testBuildSetAccessoryEncodesPowerOn() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true);
        assertEquals(1, frame[10]);
    }

    @Test
    public void testBuildSetAccessoryEncodesPowerOff() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, false);
        assertEquals(0, frame[10]);
    }

    // --- buildQueryAccessory ---

    @Test
    public void testBuildQueryAccessoryHasDlc4() {
        int[] frame = Cs2AccessoryCommands.buildQueryAccessory(0x3000L);
        assertEquals(4, frame[4]);
    }

    @Test
    public void testBuildQueryAccessoryHasCommandCode0x0B() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildQueryAccessory(0x3000L));
        assertEquals(0x0B, cf.cs2Header().commandCode());
    }

    // --- reply parsing (AccessoryReplyFrame) ---

    private static int[] buildReplyFrame(long address, int aspect, boolean power) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF),
            aspect & 0xFF,
            power ? 1 : 0
        };
        return Cs2FrameBuilder.buildFrame(0, 0x0B, true, 0x0300, 6, data);
    }

    @Test
    public void testAccessoryReplyRoundtripsToAccessoryReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 1, true));
        assertInstanceOf(Cs2AccessoryCommands.AccessoryReplyFrame.class, parsed);
    }

    @Test
    public void testAccessoryReplyFrameAddress() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3800L, 0, false));
        Cs2AccessoryCommands.AccessoryReplyFrame reply =
                (Cs2AccessoryCommands.AccessoryReplyFrame) parsed;
        assertEquals(0x3800L, reply.address());
    }

    @Test
    public void testAccessoryReplyFrameAspect() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 2, false));
        Cs2AccessoryCommands.AccessoryReplyFrame reply =
                (Cs2AccessoryCommands.AccessoryReplyFrame) parsed;
        assertEquals(2, reply.aspect());
    }

    @Test
    public void testAccessoryReplyFramePowerTrue() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 0, true));
        Cs2AccessoryCommands.AccessoryReplyFrame reply =
                (Cs2AccessoryCommands.AccessoryReplyFrame) parsed;
        assertTrue(reply.power());
    }

    @Test
    public void testAccessoryReplyFramePowerFalse() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 0, false));
        Cs2AccessoryCommands.AccessoryReplyFrame reply =
                (Cs2AccessoryCommands.AccessoryReplyFrame) parsed;
        assertFalse(reply.power());
    }

    @Test
    public void testAccessoryReplyIsResponse() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 0, true));
        assertTrue(parsed.header().response());
    }

    // --- buildSetAccessoryTimed (DLC=8) ---

    @Test
    public void testBuildSetAccessoryTimedHasDlc8() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessoryTimed(0x3000L, 1, true, 50);
        assertEquals(8, frame[4]);
    }

    @Test
    public void testBuildSetAccessoryTimedHasCommandCode0x0B() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildSetAccessoryTimed(0x3000L, 1, true, 50));
        assertEquals(0x0B, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildSetAccessoryTimedEncodesTime() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessoryTimed(0x3000L, 0, true, 0x0190);
        assertEquals(0x01, frame[11]);
        assertEquals(0x90, frame[12]);
    }

    @Test
    public void testBuildSetAccessoryTimedEncodesAspectAndPower() {
        int[] frame = Cs2AccessoryCommands.buildSetAccessoryTimed(0x3000L, 3, false, 100);
        assertEquals(3, frame[9]);
        assertEquals(0, frame[10]);
    }

    // --- AccessoryReplyFrame.time10ms() ---

    private static int[] buildTimedReplyFrame(long address, int aspect, boolean power, int time10ms) {
        int[] data = new int[]{
            (int)((address >> 24) & 0xFF),
            (int)((address >> 16) & 0xFF),
            (int)((address >>  8) & 0xFF),
            (int)( address        & 0xFF),
            aspect & 0xFF,
            power ? 1 : 0,
            (time10ms >> 8) & 0xFF,
            time10ms & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, 0x0B, true, 0x0300, 8, data);
    }

    @Test
    public void testAccessoryTimedReplyRoundtripsToAccessoryReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildTimedReplyFrame(0x3000L, 1, true, 50));
        assertInstanceOf(Cs2AccessoryCommands.AccessoryReplyFrame.class, parsed);
    }

    @Test
    public void testAccessoryReplyFrameTime10ms() {
        Cs2ClassifiedFrame parsed = roundtrip(buildTimedReplyFrame(0x3000L, 0, true, 0x0190));
        assertEquals(0x0190,
                ((Cs2AccessoryCommands.AccessoryReplyFrame) parsed).time10ms());
    }

    @Test
    public void testAccessoryReplyFrameTime10msZeroForDlc6() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReplyFrame(0x3000L, 0, true));
        assertEquals(0, ((Cs2AccessoryCommands.AccessoryReplyFrame) parsed).time10ms());
    }

    // --- Cs2Command resolution ---

    @Test
    public void testBuildSetAccessoryResolvesToAccessoryEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2AccessoryCommands.buildSetAccessory(0x3000L, 0, true));
        assertEquals(Cs2Command.ACCESSORY, cf.cs2Header().resolvedCommand());
    }

    @Test
    public void testAccessoryReplyResolvesToAccessoryReplyEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildReplyFrame(0x3000L, 0, true));
        assertEquals(Cs2Command.ACCESSORY_REPLY, cf.cs2Header().resolvedCommand());
    }

    // --- MfxBindExReplyFrame ---

    private static int[] buildMfxBindExReply(long uid, int sid) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            (sid >> 8) & 0xFF, sid & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2AccessoryCommands.COMMAND_MFX_BIND_EX,
                true, 0x0300, 6, data);
    }

    @Test
    public void testMfxBindExReplyRoundtripsToMfxBindExReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildMfxBindExReply(0x11223344L, 0x0007));
        assertInstanceOf(Cs2AccessoryCommands.MfxBindExReplyFrame.class, parsed);
    }

    @Test
    public void testMfxBindExReplyFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildMfxBindExReply(0x11223344L, 0x0007));
        assertEquals(0x11223344L, ((Cs2AccessoryCommands.MfxBindExReplyFrame) parsed).mfxUid());
    }

    @Test
    public void testMfxBindExReplyFrameSid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildMfxBindExReply(0x11223344L, 0x0042));
        assertEquals(0x0042, ((Cs2AccessoryCommands.MfxBindExReplyFrame) parsed).mfxSid());
    }
}
