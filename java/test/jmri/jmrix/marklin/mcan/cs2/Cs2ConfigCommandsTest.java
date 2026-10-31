package jmri.jmrix.marklin.mcan.cs2;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.*;
import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Cs2ConfigCommands}.
 */
public class Cs2ConfigCommandsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static Cs2ClassifiedFrame roundtrip(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return Cs2FrameParser.parse(cf);
    }

    // --- buildPing ---

    @Test
    public void testBuildPingProduces13Bytes() {
        assertEquals(13, Cs2ConfigCommands.buildPing().length);
    }

    @Test
    public void testBuildPingClassifiesAsCs2Native() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol());
    }

    @Test
    public void testBuildPingHasCommandCode0x18() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        assertEquals(0x18, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildPingIsNotResponse() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        assertFalse(cf.cs2Header().response());
    }

    @Test
    public void testBuildPingHasDlc0() {
        int[] frame = Cs2ConfigCommands.buildPing();
        assertEquals(0, frame[4]);
    }

    @Test
    public void testBuildPingResolvesToPingEnum() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        assertEquals(Cs2Command.PING, cf.cs2Header().resolvedCommand());
    }

    @Test
    public void testBuildPingParsesToPingRequestFrame() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        Cs2ClassifiedFrame typed = Cs2FrameParser.parse(cf);
        assertInstanceOf(Cs2ConfigCommands.PingRequestFrame.class, typed);
    }

    // --- parsePingReply / PingReplyFrame ---

    private static int[] buildPingReply(long uid, int version, int deviceType) {
        int hash = NativeDeviceInfo.calculateHash(uid) | 0x0300;
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF),
            (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF),
            (int)( uid        & 0xFF),
            (version >> 8) & 0xFF,
            version & 0xFF,
            (deviceType >> 8) & 0xFF,
            deviceType & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, 0x18, true, hash, 8, data);
    }

    @Test
    public void testPingReplyRoundtripsToPingReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildPingReply(0xDEADBEEFL, 0x0102, 0x0060));
        assertInstanceOf(Cs2ConfigCommands.PingReplyFrame.class, parsed);
    }

    @Test
    public void testPingReplyFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildPingReply(0x12345678L, 0x0100, 0x0060));
        Cs2ConfigCommands.PingReplyFrame reply = (Cs2ConfigCommands.PingReplyFrame) parsed;
        assertEquals(0x12345678L, reply.uid());
    }

    @Test
    public void testPingReplyFrameSoftwareVersion() {
        Cs2ClassifiedFrame parsed = roundtrip(buildPingReply(0x12345678L, 0x0305, 0x0060));
        Cs2ConfigCommands.PingReplyFrame reply = (Cs2ConfigCommands.PingReplyFrame) parsed;
        assertEquals(0x0305, reply.softwareVersion());
    }

    @Test
    public void testPingReplyFrameDeviceType() {
        Cs2ClassifiedFrame parsed = roundtrip(buildPingReply(0x12345678L, 0x0100, 0x0060));
        Cs2ConfigCommands.PingReplyFrame reply = (Cs2ConfigCommands.PingReplyFrame) parsed;
        assertEquals(0x0060, reply.deviceType());
    }

    @Test
    public void testPingReplyIsResponse() {
        Cs2ClassifiedFrame parsed = roundtrip(buildPingReply(0x12345678L, 0x0100, 0x0060));
        assertTrue(parsed.header().response());
    }

    // --- parsePingReply returning NativeDeviceInfo ---

    @Test
    public void testParsePingReplyExtractsUid() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildPingReply(0x00112233L, 0x0100, NativeDeviceInfo.TYPE_MS2));
        Optional<NativeDeviceInfo> result = Cs2ConfigCommands.parsePingReply(cf);
        assertTrue(result.isPresent());
        assertEquals(0x00112233L, result.get().uid());
    }

    @Test
    public void testParsePingReplyExtractsSoftwareVersion() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildPingReply(0x00112233L, 0x0201, NativeDeviceInfo.TYPE_MS2));
        Optional<NativeDeviceInfo> result = Cs2ConfigCommands.parsePingReply(cf);
        assertTrue(result.isPresent());
        assertEquals(0x0201, result.get().softwareVersion());
    }

    @Test
    public void testParsePingReplyExtractsDeviceType() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildPingReply(0x00112233L, 0x0100, NativeDeviceInfo.TYPE_MS2));
        Optional<NativeDeviceInfo> result = Cs2ConfigCommands.parsePingReply(cf);
        assertTrue(result.isPresent());
        assertEquals(NativeDeviceInfo.TYPE_MS2, result.get().deviceType());
    }

    @Test
    public void testParsePingReplyEmptyForWrongDlc() {
        int[] frame = buildPingReply(0x00112233L, 0x0100, NativeDeviceInfo.TYPE_MS2);
        frame[4] = 4;
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertFalse(Cs2ConfigCommands.parsePingReply(cf).isPresent());
    }

    @Test
    public void testParsePingReplyEmptyForBroadcastUid() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildPingReply(NativeDeviceInfo.UID_BROADCAST, 0, 0));
        assertFalse(Cs2ConfigCommands.parsePingReply(cf).isPresent());
    }

    @Test
    public void testParsePingReplyEmptyForUninitializedUid() {
        ClassifiedFrame cf = ProtocolClassifier.classify(buildPingReply(NativeDeviceInfo.UID_UNINITIALIZED, 0, 0));
        assertFalse(Cs2ConfigCommands.parsePingReply(cf).isPresent());
    }

    @Test
    public void testParsePingReplyEmptyForNonResponseFrame() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildPing());
        assertFalse(Cs2ConfigCommands.parsePingReply(cf).isPresent());
    }

    @Test
    public void testParsePingReplyEmptyForCdBFrame() {
        int[] cdbFrame = new int[13];
        cdbFrame[2] = 0x0A; cdbFrame[3] = 0x80;
        ClassifiedFrame cf = ProtocolClassifier.classify(cdbFrame);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol());
        assertFalse(Cs2ConfigCommands.parsePingReply(cf).isPresent());
    }

    // --- buildCanBoot ---

    @Test
    public void testBuildCanBootHasCommandCode0x1B() {
        ClassifiedFrame cf = ProtocolClassifier.classify(Cs2ConfigCommands.buildCanBoot());
        assertEquals(0x1B, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildCanBootHasDlc5() {
        int[] frame = Cs2ConfigCommands.buildCanBoot();
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildCanBootHasMagicByte() {
        int[] frame = Cs2ConfigCommands.buildCanBoot();
        assertEquals(0x11, frame[9]);
    }

    // --- buildBootloaderMode ---

    @Test
    public void testBuildBootloaderModeHasCommandCode0x1B() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2ConfigCommands.buildBootloaderMode());
        assertEquals(0x1B, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildBootloaderModeHasDlc0() {
        int[] frame = Cs2ConfigCommands.buildBootloaderMode();
        assertEquals(0, frame[4]);
    }

    @Test
    public void testCanBootAndBootloaderModeAreDifferent() {
        int[] boot       = Cs2ConfigCommands.buildCanBoot();
        int[] bootloader = Cs2ConfigCommands.buildBootloaderMode();
        assertNotEquals(boot[4], bootloader[4], "DLC must differ between the two BOOT_CAN variants");
    }

    // --- UpdateOfferReplyFrame ---

    private static int[] buildUpdateOfferReply(long uid, int version, int deviceType) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            (version >> 8) & 0xFF, version & 0xFF,
            (deviceType >> 8) & 0xFF, deviceType & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2ConfigCommands.COMMAND_UPDATE_OFFER,
                true, 0x0300, 8, data);
    }

    @Test
    public void testUpdateOfferReplyRoundtripsToUpdateOfferReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferReply(0x11223344L, 0x0201, 0x0030));
        assertInstanceOf(Cs2ConfigCommands.UpdateOfferReplyFrame.class, parsed);
    }

    @Test
    public void testUpdateOfferReplyFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferReply(0x11223344L, 0x0201, 0x0030));
        assertEquals(0x11223344L, ((Cs2ConfigCommands.UpdateOfferReplyFrame) parsed).uid());
    }

    @Test
    public void testUpdateOfferReplyFrameVersion() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferReply(0x11223344L, 0x0305, 0x0000));
        assertEquals(0x0305, ((Cs2ConfigCommands.UpdateOfferReplyFrame) parsed).version());
    }

    @Test
    public void testUpdateOfferReplyFrameDeviceType() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferReply(0x11223344L, 0x0100, 0xFFFF));
        assertEquals(0xFFFF, ((Cs2ConfigCommands.UpdateOfferReplyFrame) parsed).deviceType());
    }

    // --- BootRequestFrame ---

    private static int[] buildBootRequest(long uid, boolean response) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF)
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2ConfigCommands.COMMAND_BOOT_REQUEST,
                response, 0x0300, 4, data);
    }

    @Test
    public void testBootRequestRoundtripsToBootRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildBootRequest(0xDEADBEEFL, false));
        assertInstanceOf(Cs2ConfigCommands.BootRequestFrame.class, parsed);
    }

    @Test
    public void testBootRequestReplyRoundtripsToBootRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildBootRequest(0xDEADBEEFL, true));
        assertInstanceOf(Cs2ConfigCommands.BootRequestFrame.class, parsed);
    }

    @Test
    public void testBootRequestFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildBootRequest(0x12345678L, false));
        assertEquals(0x12345678L, ((Cs2ConfigCommands.BootRequestFrame) parsed).uid());
    }

    // --- ReadConfigDataReplyFrame ---

    private static int[] buildReadConfigDataReply(long uid, int channel, int sub, int packets) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            channel & 0xFF, sub & 0xFF, packets & 0xFF, 0x00
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2ConfigCommands.COMMAND_READ_CONFIG_DATA,
                true, 0x0300, 8, data);
    }

    @Test
    public void testReadConfigDataReplyRoundtripsToReadConfigDataReplyFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataReply(0x11223344L, 1, 0, 3));
        assertInstanceOf(Cs2ConfigCommands.ReadConfigDataReplyFrame.class, parsed);
    }

    @Test
    public void testReadConfigDataReplyFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataReply(0x11223344L, 1, 0, 3));
        assertEquals(0x11223344L, ((Cs2ConfigCommands.ReadConfigDataReplyFrame) parsed).uid());
    }

    @Test
    public void testReadConfigDataReplyFrameChannelIndex() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataReply(0x11223344L, 2, 5, 10));
        assertEquals(2, ((Cs2ConfigCommands.ReadConfigDataReplyFrame) parsed).channelIndex());
    }

    // --- BootCanRequestFrame ---

    @Test
    public void testBuildCanBootRoundtripsToBootCanRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2ConfigCommands.buildCanBoot());
        assertInstanceOf(Cs2ConfigCommands.BootCanRequestFrame.class, parsed);
    }

    @Test
    public void testBuildCanBootIsGleisboxActivation() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2ConfigCommands.buildCanBoot());
        assertTrue(((Cs2ConfigCommands.BootCanRequestFrame) parsed).isGleisboxActivation());
    }

    @Test
    public void testBuildBootloaderModeRoundtripsToBootCanRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2ConfigCommands.buildBootloaderMode());
        assertInstanceOf(Cs2ConfigCommands.BootCanRequestFrame.class, parsed);
    }

    @Test
    public void testBuildBootloaderModeIsNotGleisboxActivation() {
        Cs2ClassifiedFrame parsed = roundtrip(Cs2ConfigCommands.buildBootloaderMode());
        assertFalse(((Cs2ConfigCommands.BootCanRequestFrame) parsed).isGleisboxActivation());
    }

    // --- UpdateOfferRequestFrame ---

    private static int[] buildUpdateOfferRequest(long uid, int version) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            (version >> 8) & 0xFF, version & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2ConfigCommands.COMMAND_UPDATE_OFFER,
                false, 0x0300, 6, data);
    }

    @Test
    public void testUpdateOfferRequestRoundtripsToUpdateOfferRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferRequest(0x11223344L, 0x0163));
        assertInstanceOf(Cs2ConfigCommands.UpdateOfferRequestFrame.class, parsed);
    }

    @Test
    public void testUpdateOfferRequestFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferRequest(0x11223344L, 0x0163));
        assertEquals(0x11223344L, ((Cs2ConfigCommands.UpdateOfferRequestFrame) parsed).uid());
    }

    @Test
    public void testUpdateOfferRequestFrameVersion() {
        Cs2ClassifiedFrame parsed = roundtrip(buildUpdateOfferRequest(0x11223344L, 0x0163));
        assertEquals(0x0163, ((Cs2ConfigCommands.UpdateOfferRequestFrame) parsed).version());
    }

    // --- ReadConfigDataRequestFrame ---

    private static int[] buildReadConfigDataRequest(long uid, int channel, int sub) {
        int[] data = new int[]{
            (int)((uid >> 24) & 0xFF), (int)((uid >> 16) & 0xFF),
            (int)((uid >>  8) & 0xFF), (int)( uid        & 0xFF),
            channel & 0xFF, sub & 0xFF
        };
        return Cs2FrameBuilder.buildFrame(0, Cs2ConfigCommands.COMMAND_READ_CONFIG_DATA,
                false, 0x0300, 6, data);
    }

    @Test
    public void testReadConfigDataRequestRoundtripsToReadConfigDataRequestFrame() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataRequest(0x11223344L, 1, 0));
        assertInstanceOf(Cs2ConfigCommands.ReadConfigDataRequestFrame.class, parsed);
    }

    @Test
    public void testReadConfigDataRequestFrameUid() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataRequest(0xAABBCCDDL, 1, 0));
        assertEquals(0xAABBCCDDL, ((Cs2ConfigCommands.ReadConfigDataRequestFrame) parsed).uid());
    }

    @Test
    public void testReadConfigDataRequestFrameChannelIndex() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataRequest(0x11223344L, 3, 7));
        assertEquals(3, ((Cs2ConfigCommands.ReadConfigDataRequestFrame) parsed).channelIndex());
    }

    @Test
    public void testReadConfigDataRequestFrameSubIndex() {
        Cs2ClassifiedFrame parsed = roundtrip(buildReadConfigDataRequest(0x11223344L, 3, 7));
        assertEquals(7, ((Cs2ConfigCommands.ReadConfigDataRequestFrame) parsed).subIndex());
    }
}
