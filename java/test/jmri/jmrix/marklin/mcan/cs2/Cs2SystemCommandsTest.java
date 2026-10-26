package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2SystemCommands}.
 * Verifies buildSystemStop/buildSystemGo encode correctly and
 * that Cs2FrameParser dispatches to the right typed frame.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class Cs2SystemCommandsTest {

    private static Cs2ClassifiedFrame roundtrip(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return Cs2FrameParser.parse(cf);
    }

    // --- buildSystemStop ---

    @Test
    public void testBuildSystemStopProduces13Bytes() {
        assertEquals(13, Cs2SystemCommands.buildSystemStop().length);
    }

    @Test
    public void testBuildSystemStopClassifiesAsCs2Native() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemStop());
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    @Test
    public void testBuildSystemStopHasCommandCode0x00() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemStop());
        assertEquals(0x00, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildSystemStopIsNotResponse() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemStop());
        assertFalse(cf.cs2Header().response());
    }

    @Test
    public void testBuildSystemStopRoundtripsToSystemStopFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildSystemStop());
        assertInstanceOf(Cs2SystemCommands.SystemStopFrame.class, parsed);
    }

    // --- buildSystemGo ---

    @Test
    public void testBuildSystemGoProduces13Bytes() {
        assertEquals(13, Cs2SystemCommands.buildSystemGo().length);
    }

    @Test
    public void testBuildSystemGoHasCommandCode0x00() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemGo());
        assertEquals(0x00, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildSystemGoRoundtripsToSystemGoFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildSystemGo());
        assertInstanceOf(Cs2SystemCommands.SystemGoFrame.class, parsed);
    }

    @Test
    public void testSystemStopAndGoHaveDifferentSubCommands() {
        int[] stop = Cs2SystemCommands.buildSystemStop();
        int[] go   = Cs2SystemCommands.buildSystemGo();
        // sub-command is in data byte 4 (frame byte 9)
        assertNotEquals(stop[9] & 0xFF, go[9] & 0xFF);
    }

    // --- reply frames ---

    @Test
    public void testSystemStopReplyRoundtrips() {
        int[] frame = Cs2SystemCommands.buildSystemStopReply();
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertTrue(cf.cs2Header().response());
        Cs2ClassifiedFrame parsed = Cs2FrameParser.parse(cf);
        assertInstanceOf(Cs2SystemCommands.SystemStopFrame.class, parsed);
        assertTrue(parsed.header().response());
    }

    @Test
    public void testSystemGoReplyRoundtrips() {
        int[] frame = Cs2SystemCommands.buildSystemGoReply();
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertTrue(cf.cs2Header().response());
        Cs2ClassifiedFrame parsed = Cs2FrameParser.parse(cf);
        assertInstanceOf(Cs2SystemCommands.SystemGoFrame.class, parsed);
    }

    // --- Cs2Command resolution ---

    @Test
    public void testBuildSystemStopResolvesToSystemCommandEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemStop());
        assertEquals(Cs2Command.SYSTEM_COMMAND, cf.cs2Header().resolvedCommand());
    }

    @Test
    public void testBuildSystemStopReplyResolvesToSystemCommandReplyEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2SystemCommands.buildSystemStopReply());
        assertEquals(Cs2Command.SYSTEM_COMMAND_REPLY, cf.cs2Header().resolvedCommand());
    }

    // --- new sub-commands 0x03–0x0C, 0x80 ---

    @Test
    public void testBuildLocoEmergencyStopSubCommand() {
        int[] frame = Cs2SystemCommands.buildLocoEmergencyStop(0x4000L);
        assertEquals(Cs2SystemCommands.SUB_LOCO_EMERGENCY_STOP, frame[9]);
    }

    @Test
    public void testBuildLocoEmergencyStopHasDlc5() {
        assertEquals(5, Cs2SystemCommands.buildLocoEmergencyStop(0x4000L)[4]);
    }

    @Test
    public void testBuildLocoCycleStopSubCommand() {
        int[] frame = Cs2SystemCommands.buildLocoCycleStop(0x4000L);
        assertEquals(Cs2SystemCommands.SUB_LOCO_CYCLE_STOP, frame[9]);
    }

    @Test
    public void testBuildLocoDataProtocolSubCommand() {
        int[] frame = Cs2SystemCommands.buildLocoDataProtocol(0x4000L, Cs2SystemCommands.PROTOCOL_DCC_LONG_126);
        assertEquals(Cs2SystemCommands.SUB_LOCO_DATA_PROTOCOL, frame[9]);
    }

    @Test
    public void testBuildLocoDataProtocolHasDlc6() {
        assertEquals(6, Cs2SystemCommands.buildLocoDataProtocol(0x4000L, 0)[4]);
    }

    @Test
    public void testBuildLocoDataProtocolEncodesProtocol() {
        int[] frame = Cs2SystemCommands.buildLocoDataProtocol(0x4000L, Cs2SystemCommands.PROTOCOL_DCC_LONG_126);
        assertEquals(Cs2SystemCommands.PROTOCOL_DCC_LONG_126, frame[10]);
    }

    @Test
    public void testBuildAccessorySwitchTimeSubCommand() {
        int[] frame = Cs2SystemCommands.buildAccessorySwitchTime(Cs2SystemCommands.UID_BROADCAST, 100);
        assertEquals(Cs2SystemCommands.SUB_ACCESSORY_SWITCH_TIME, frame[9]);
    }

    @Test
    public void testBuildAccessorySwitchTimeHasDlc7() {
        assertEquals(7, Cs2SystemCommands.buildAccessorySwitchTime(0L, 100)[4]);
    }

    @Test
    public void testBuildFastReadMfxSubCommand() {
        int[] frame = Cs2SystemCommands.buildFastReadMfx(0x11223344L, 0x0001);
        assertEquals(Cs2SystemCommands.SUB_FAST_READ_MFX, frame[9]);
    }

    @Test
    public void testBuildEnableTrackProtocolSubCommand() {
        int packetNumber = Cs2SystemCommands.TRACK_PROTOCOL_MM2 | Cs2SystemCommands.TRACK_PROTOCOL_MFX;
        int[] frame = Cs2SystemCommands.buildEnableTrackProtocol(0L, packetNumber);
        assertEquals(Cs2SystemCommands.SUB_ENABLE_TRACK_PROTOCOL, frame[9]);
    }

    @Test
    public void testBuildEnableTrackProtocolEncodesMask() {
        int mask = Cs2SystemCommands.TRACK_PROTOCOL_MFX | Cs2SystemCommands.TRACK_PROTOCOL_DCC;
        int[] frame = Cs2SystemCommands.buildEnableTrackProtocol(0L, mask);
        assertEquals(mask, frame[10]);
    }

    @Test
    public void testBuildMfxReregistrationCounterSubCommand() {
        int[] frame = Cs2SystemCommands.buildMfxReregistrationCounter(0L, 42);
        assertEquals(Cs2SystemCommands.SUB_MFX_REREGISTRATION, frame[9]);
    }

    @Test
    public void testBuildSystemStatusQuerySubCommand() {
        int[] frame = Cs2SystemCommands.buildSystemStatusQuery(0L, 1);
        assertEquals(Cs2SystemCommands.SUB_SYSTEM_STATUS, frame[9]);
    }

    @Test
    public void testBuildSystemOverloadSubCommand() {
        int[] frame = Cs2SystemCommands.buildSystemOverload(0x11223344L, 1);
        assertEquals(Cs2SystemCommands.SUB_SYSTEM_OVERLOAD, frame[9]);
    }

    @Test
    public void testBuildDeviceIdentifierQuerySubCommand() {
        int[] frame = Cs2SystemCommands.buildDeviceIdentifierQuery(0x11223344L);
        assertEquals(Cs2SystemCommands.SUB_DEVICE_IDENTIFIER, frame[9]);
    }

    @Test
    public void testBuildDeviceIdentifierQueryHasDlc5() {
        assertEquals(5, Cs2SystemCommands.buildDeviceIdentifierQuery(0L)[4]);
    }

    @Test
    public void testBuildDeviceIdentifierSetHasDlc7() {
        assertEquals(7, Cs2SystemCommands.buildDeviceIdentifierSet(0L, 0x0042)[4]);
    }

    @Test
    public void testBuildDeviceIdentifierSetEncodesIdentifier() {
        int[] frame = Cs2SystemCommands.buildDeviceIdentifierSet(0L, 0x0042);
        assertEquals(0x00, frame[10]);
        assertEquals(0x42, frame[11]);
    }

    @Test
    public void testBuildSystemResetSubCommand() {
        int[] frame = Cs2SystemCommands.buildSystemReset(0L, 0);
        assertEquals(Cs2SystemCommands.SUB_SYSTEM_RESET, frame[9]);
    }

    @Test
    public void testBuildSystemResetHasDlc6() {
        assertEquals(6, Cs2SystemCommands.buildSystemReset(0L, 0)[4]);
    }

    // --- typed frame roundtrips for new sub-commands ---

    @Test
    public void testLocoEmergencyStopRoundtripsToLocoEmergencyStopFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildLocoEmergencyStop(0x4000L));
        assertInstanceOf(Cs2SystemCommands.LocoEmergencyStopFrame.class, parsed);
    }

    @Test
    public void testLocoEmergencyStopFrameLocId() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildLocoEmergencyStop(0x4001L));
        assertEquals(0x4001L, ((Cs2SystemCommands.LocoEmergencyStopFrame) parsed).locId());
    }

    @Test
    public void testLocoCycleStopRoundtripsToLocoCycleStopFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildLocoCycleStop(0x4000L));
        assertInstanceOf(Cs2SystemCommands.LocoCycleStopFrame.class, parsed);
    }

    @Test
    public void testLocoDataProtocolRoundtripsToLocoDataProtocolFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2SystemCommands.buildLocoDataProtocol(0x4000L, Cs2SystemCommands.PROTOCOL_DCC_SHORT_28));
        assertInstanceOf(Cs2SystemCommands.LocoDataProtocolFrame.class, parsed);
    }

    @Test
    public void testLocoDataProtocolFrameProtocol() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2SystemCommands.buildLocoDataProtocol(0x4000L, Cs2SystemCommands.PROTOCOL_DCC_LONG_126));
        assertEquals(Cs2SystemCommands.PROTOCOL_DCC_LONG_126,
                ((Cs2SystemCommands.LocoDataProtocolFrame) parsed).protocol());
    }

    @Test
    public void testEnableTrackProtocolRoundtripsToEnableTrackProtocolFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2SystemCommands.buildEnableTrackProtocol(0L, Cs2SystemCommands.TRACK_PROTOCOL_DCC));
        assertInstanceOf(Cs2SystemCommands.EnableTrackProtocolFrame.class, parsed);
    }

    @Test
    public void testEnableTrackProtocolFrameMask() {
        int mask = Cs2SystemCommands.TRACK_PROTOCOL_MM2 | Cs2SystemCommands.TRACK_PROTOCOL_DCC;
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2SystemCommands.buildEnableTrackProtocol(0L, mask));
        assertEquals(mask, ((Cs2SystemCommands.EnableTrackProtocolFrame) parsed).protocolMask());
    }

    @Test
    public void testSystemResetRoundtripsToSystemResetFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildSystemReset(0L, 0));
        assertInstanceOf(Cs2SystemCommands.SystemResetFrame.class, parsed);
    }

    // --- MFX Seek (sub-cmd 0x30) ---

    @Test
    public void testBuildMfxSeekSubCommand() {
        int[] frame = Cs2SystemCommands.buildMfxSeek(0x11223344L, 0);
        assertEquals(Cs2SystemCommands.SUB_MFX_SEEK, frame[9]);
    }

    @Test
    public void testBuildMfxSeekHasDlc6() {
        assertEquals(6, Cs2SystemCommands.buildMfxSeek(0L, 0)[4]);
    }

    @Test
    public void testBuildMfxSeekEncodesRange() {
        int[] frame = Cs2SystemCommands.buildMfxSeek(0L, 42);
        assertEquals(42, frame[10]);
    }

    @Test
    public void testBuildMfxSeekRoundtripsToMfxSeekFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildMfxSeek(0x11223344L, 5));
        assertInstanceOf(Cs2SystemCommands.MfxSeekFrame.class, parsed);
    }

    @Test
    public void testMfxSeekFrameRange() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildMfxSeek(0x11223344L, 7));
        assertEquals(7, ((Cs2SystemCommands.MfxSeekFrame) parsed).range());
    }

    @Test
    public void testMfxSeekFrameSenderUid() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2SystemCommands.buildMfxSeek(0x11223344L, 0));
        assertEquals(0x11223344L, ((Cs2SystemCommands.MfxSeekFrame) parsed).senderUid());
    }}
