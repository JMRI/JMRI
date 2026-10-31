package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.classify.ClassifiedFrame;
import jmri.jmrix.marklin.mcan.classify.FrameProtocol;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.device.CdBDeviceClass;
import jmri.jmrix.marklin.mcan.protocol.NativeDeviceInfo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McanDeviceManager}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class McanDeviceManagerTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;
    private McanDeviceManager manager;

    private static long cdBPingId(int canClass) {
        return ((long)(canClass >> 4) << 21)
             | ((long)(canClass & 0x0F) << 12)
             | 0x0A80L
             | 0x80L;   // broadcast module base (moduleId=0 → 0+0x80)
    }

    private static MarklinReply frameFromCanId(long canId, int dlc, int... data) {
        int[] raw = new int[13];
        raw[0] = (int)((canId >> 24) & 0xFF);
        raw[1] = (int)((canId >> 16) & 0xFF);
        raw[2] = (int)((canId >> 8)  & 0xFF);
        raw[3] = (int)( canId        & 0xFF);
        raw[4] = dlc;
        for (int i = 0; i < data.length && i < 8; i++) {
            raw[5 + i] = data[i] & 0xFF;
        }
        return new MarklinReply(raw);
    }

    /** Builds a CS2 ping reply frame (command=0x18, response=true, DLC=8, hash=0x0300). */
    private static MarklinReply cs2PingReply(long uid, int swVersion, int deviceType) {
        // CAN ID: priority=0, command=0x18, response=1, hash=0x0300
        long canId = (0x18L << 17) | (1L << 16) | 0x0300L;
        int[] raw = new int[13];
        raw[0] = (int)((canId >> 24) & 0xFF);
        raw[1] = (int)((canId >> 16) & 0xFF);
        raw[2] = (int)((canId >>  8) & 0xFF);
        raw[3] = (int)( canId        & 0xFF);
        raw[4] = 8; // DLC
        raw[5] = (int)((uid >> 24) & 0xFF);
        raw[6] = (int)((uid >> 16) & 0xFF);
        raw[7] = (int)((uid >>  8) & 0xFF);
        raw[8] = (int)( uid        & 0xFF);
        raw[9]  = (swVersion >> 8) & 0xFF;
        raw[10] = swVersion & 0xFF;
        raw[11] = (deviceType >> 8) & 0xFF;
        raw[12] = deviceType & 0xFF;
        return new MarklinReply(raw);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
        manager = memo.getMcanDeviceManager();
    }

    @AfterEach
    public void tearDown() {
        tc.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    @Test
    public void testProvideAndGetDevice() {
        assertNotNull(manager);
        McanDeviceIdentity.Native nativeIdentity = McanDeviceIdentity.forNative(0x00112233L, NativeDeviceInfo.TYPE_MS2);

        McanDevice device = manager.provideDevice(nativeIdentity);
        assertNotNull(device);
        assertEquals("M:dev:uid-00112233", device.getSystemName());

        McanDevice retrieved = manager.getDevice(nativeIdentity);
        assertSame(device, retrieved);
    }

    @Test
    public void testPingResponseDiscovery() {
        McanDeviceIdentity.Native nativeIdentity = McanDeviceIdentity.forNative(0x00112233L, NativeDeviceInfo.TYPE_MS2);
        assertNull(manager.getDevice(nativeIdentity));

        MarklinReply pingReply = cs2PingReply(0x00112233L, 0x0102, NativeDeviceInfo.TYPE_MS2);
        manager.reply(pingReply);

        McanDevice discovered = manager.getDevice(nativeIdentity);
        assertNotNull(discovered);
        assertEquals(McanDeviceState.ONLINE, discovered.getDeviceState());
        assertEquals(0x0102, discovered.getSoftwareVersion());
    }

    @Test
    public void testCdBConfigReplyDiscovery() {
        McanDeviceIdentity.CdB cdbIdentity = McanDeviceIdentity.forCdB(0x22, 0x03);
        assertNull(manager.getDevice(cdbIdentity));

        long pingReplyId = cdBPingId(0x22) + 0x10000;
        long withModule = (pingReplyId & 0xFFFFFF00L) | 0x83;

        MarklinReply reply = frameFromCanId(withModule, 2);

        // Classify and parse via mcan layer, then call manager directly
        int[] raw13 = new int[13];
        for (int i = 0; i < 13; i++) raw13[i] = reply.getElement(i) & 0xFF;
        ClassifiedFrame cf = ProtocolClassifier.classify(raw13);
        assertEquals(FrameProtocol.CDB_CONFIG, cf.protocol());
        CdBFrameParser.CdBFrame frame = CdBFrameParser.parse(cf);

        manager.cdBConfigReply(frame, reply);

        McanDevice device = manager.getDevice(cdbIdentity);
        assertNotNull(device);
        assertEquals(McanDeviceState.ONLINE, device.getDeviceState());
    }

    @Test
    public void testProgrammaticMarklinDiscovery() {
        int initialSent = tc.getSentMessages().size();

        manager.discoverMarklinDevices();

        assertEquals(initialSent + 1, tc.getSentMessages().size());
        MarklinMessage sent = tc.getLastMessageSent();
        assertNotNull(sent);

        // Ping request: command=0x18 encoded in bytes 0-1
        int command = ((sent.getElement(0) & 0x0F) << 4) | ((sent.getElement(1) & 0xFE) >> 1);
        assertEquals(Cs2ConfigCommands.COMMAND_PING, command, "Must send a PING command");
        assertEquals(0, sent.getElement(1) & 0x01, "Must not have response flag set");
        assertEquals(0, sent.getElement(4), "PING DLC must be 0");
    }

    @Test
    public void testProgrammaticCdBDiscoverySingle() {
        int initialSent = tc.getSentMessages().size();

        manager.discoverCdBDevice(CdBDeviceClass.WEICHEN_CHEF_KOMBI);

        assertEquals(initialSent + 1, tc.getSentMessages().size());
        MarklinMessage sent = tc.getLastMessageSent();
        assertNotNull(sent);

        int expectedPingId = CdBDeviceClass.WEICHEN_CHEF_KOMBI.getPingId();
        int actualId = ((sent.getElement(0) & 0xFF) << 24) |
                      ((sent.getElement(1) & 0xFF) << 16) |
                      ((sent.getElement(2) & 0xFF) << 8) |
                      (sent.getElement(3) & 0xFF);
        assertEquals(expectedPingId, actualId);
        assertEquals(0, sent.getElement(4)); // DLC should be 0
    }

    @Test
    public void testProgrammaticCdBDiscoveryAllNewProtocolClassesSendOnePingEach() {
        int initialSent = tc.getSentMessages().size();

        manager.discoverAllCdBDevices();

        long newCount = java.util.Arrays.stream(CdBDeviceClass.values())
                .filter(c -> c.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.NEW)
                .count();
        long oldCount = java.util.Arrays.stream(CdBDeviceClass.values())
                .filter(c -> c.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.OLD)
                .count();
        long expectedTotal = newCount + oldCount * 128;
        JUnitUtil.waitFor(
                () -> tc.getSentMessages().size() >= initialSent + expectedTotal,
                "Expected " + expectedTotal + " pings to be sent");
        assertEquals(initialSent + expectedTotal, tc.getSentMessages().size());
    }

    @Test
    public void testDiscoverOldProtocolClassSends128AddressedPings() {
        int initialSent = tc.getSentMessages().size();

        manager.discoverCdBDevice(CdBDeviceClass.LAMPEN_CHEF);

        JUnitUtil.waitFor(
                () -> tc.getSentMessages().size() >= initialSent + 128,
                "Expected 128 addressed pings for LAMPEN_CHEF");
        assertEquals(initialSent + 128, tc.getSentMessages().size(),
                "Old-protocol discovery must iterate all 128 address slots");

        for (int i = initialSent; i < tc.getSentMessages().size(); i++) {
            MarklinMessage msg = tc.getSentMessages().get(i);
            assertEquals(0x4A, msg.getElement(2) & 0xFF,
                    "Frame " + i + " must carry LampenChef class byte 0x4A at position 2");
        }
    }

    @Test
    public void testDiscoverOldProtocolClassDoesNotSendAll128Synchronously() {
        int initialSent = tc.getSentMessages().size();

        manager.discoverCdBDevice(CdBDeviceClass.LAMPEN_CHEF);

        int sentNow = tc.getSentMessages().size() - initialSent;
        assertTrue(sentNow < 128,
                "OLD-protocol pings must be staggered; got " + sentNow + " synchronously");
    }

    @Test
    public void testReplyProcessingIndependentOfSender() {
        McanDeviceIdentity.CdB cdbIdentity = McanDeviceIdentity.forCdB(0x0D, 0x01);
        assertNull(manager.getDevice(cdbIdentity));

        long pingReplyId = cdBPingId(0x0D) + 0x10000;
        long withModule = (pingReplyId & 0xFFFFFF00L) | 0x81;

        MarklinReply reply = frameFromCanId(withModule, 2);
        memo.getTrafficController().getCdBFilter().offer(reply);

        McanDevice device = manager.getDevice(cdbIdentity);
        assertNotNull(device);
        assertEquals(McanDeviceState.ONLINE, device.getDeviceState());
    }
}
