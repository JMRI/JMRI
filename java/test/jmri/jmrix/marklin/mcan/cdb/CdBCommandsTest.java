package jmri.jmrix.marklin.mcan.cdb;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.CdBFrameType;
import jmri.jmrix.marklin.mcan.protocol.CdBDiscoveryPlan;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the split CdB command classes:
 * {@link CdBConfigCommands}, {@link CdBWeichenChefCommands},
 * {@link CdBReporterCommands}, {@link CdBLampenChefCommands}.
 */
public class CdBCommandsTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // =================================================================
    // CdBConfigCommands
    // =================================================================

    @Test
    public void testDeselectFrame() {
        int[] frame = CdBConfigCommands.buildDeselect(0x27, 3);
        assertEquals(13, frame.length);
        assertEquals(0x83, frame[3] & 0xFF);
        assertEquals(2, frame[4] & 0xFF);
        assertEquals(0x00, frame[5] & 0xFF);
        assertEquals(0xFE, frame[6] & 0xFF);
    }

    @Test
    public void testSelectFrame() {
        int[] frame = CdBConfigCommands.buildSelect(0x27, 5);
        assertEquals(13, frame.length);
        assertEquals(0x85, frame[3] & 0xFF);
        assertEquals(4, frame[4] & 0xFF);
        assertEquals(0x00, frame[5] & 0xFF);
        assertEquals(0xF7, frame[6] & 0xFF);
        assertEquals(0x00, frame[7] & 0xFF);
        assertEquals(0x01, frame[8] & 0xFF);
    }

    @Test
    public void testPingCanIdIsPublic() {
        int id = CdBConfigCommands.pingCanId(0x27, 0);
        assertTrue(id > 0, "CAN ID should be positive");
    }

    // =================================================================
    // CdBWeichenChefCommands
    // =================================================================

    @Test
    public void testWriteChSwitchingTimeFrame() {
        int[] frame = CdBWeichenChefCommands.buildWriteChSwitchingTime(0x27, 3, 2, 500);
        assertEquals(13, frame.length);
        assertEquals(0x83, frame[3] & 0xFF);
        assertEquals(4, frame[4] & 0xFF);
        assertEquals(2, frame[5] & 0xFF);
        assertEquals(3, frame[6] & 0xFF);
        assertEquals(0x01, frame[7] & 0xFF);
        assertEquals(0xF4, frame[8] & 0xFF);
    }

    @Test
    public void testReadAllPropertiesWeichenChef() {
        int[][] frames = CdBWeichenChefCommands.buildReadAllProperties(0x27, 3);
        assertTrue(frames.length > 0);
        for (int[] f : frames) assertEquals(13, f.length);
    }

    // =================================================================
    // CdBReporterCommands
    // =================================================================

    @Test
    public void testReadAllPropertiesReporter() {
        int[][] frames = CdBReporterCommands.buildReadAllProperties(0x0D, 1);
        assertTrue(frames.length > 0);
        for (int[] f : frames) assertEquals(13, f.length);
    }

    @Test
    public void testWriteChEinSchaltzeit() {
        int[] frame = CdBReporterCommands.buildWriteChEinSchaltzeit(0x0D, 1, 4, 150);
        assertEquals(13, frame.length);
        assertEquals(4, frame[5] & 0xFF);
        assertEquals(2, frame[6] & 0xFF);
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(150, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChAusSchaltzeit() {
        int[] frame = CdBReporterCommands.buildWriteChAusSchaltzeit(0x0D, 1, 4, 75);
        assertEquals(3, frame[6] & 0xFF);
        assertEquals(75, frame[8] & 0xFF);
    }

    @Test
    public void testWriteStorungsRmk() {
        int[] frame = CdBReporterCommands.buildWriteStorungsRmk(0x0D, 2, 500);
        assertEquals(13, frame.length);
        assertEquals(4, frame[4] & 0xFF);
        assertEquals(0, frame[5] & 0xFF);
        assertEquals(10, frame[6] & 0xFF);
        assertEquals(1, frame[7] & 0xFF);
        assertEquals(0xF4, frame[8] & 0xFF);
    }

    @Test
    public void testWriteStorungsFlag() {
        int[] frame = CdBReporterCommands.buildWriteStorungsFlag(0x0D, 2, 1);
        assertEquals(11, frame[6] & 0xFF);
        assertEquals(1, frame[8] & 0xFF);
    }

    @Test
    public void testWriteSonderfunktion() {
        int[] frame = CdBReporterCommands.buildWriteSonderfunktion(0x0D, 2, 3);
        assertEquals(12, frame[6] & 0xFF);
        assertEquals(3, frame[8] & 0xFF);
    }

    @Test
    public void testWriteSlaveAddr() {
        int[] frame = CdBReporterCommands.buildWriteSlaveAddr(0x0B, 1, 16, 42);
        assertEquals(16, frame[6] & 0xFF);
        assertEquals(42, frame[8] & 0xFF);
    }

    @Test
    public void testWriteFreezeRmkAddr() {
        int[] frame = CdBReporterCommands.buildWriteFreezeRmkAddr(0x0B, 1, 200);
        assertEquals(18, frame[6] & 0xFF);
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(200, frame[8] & 0xFF);
    }

    // =================================================================
    // CdBLampenChefCommands
    // =================================================================

    @Test
    public void testReadAllPropertiesLampenChef() {
        int[][] frames = CdBLampenChefCommands.buildReadAllProperties(0x4A, 2);
        assertEquals(7 + 192, frames.length);
        for (int[] f : frames) assertEquals(13, f.length);
    }

    @Test
    public void testWriteChDimmwert() {
        int[] frame = CdBLampenChefCommands.buildWriteChDimmwert(0x4A, 3, 5, 127);
        assertEquals(13, frame.length);
        assertEquals(0x83, frame[3] & 0xFF);
        assertEquals(4, frame[4] & 0xFF);
        assertEquals(5, frame[5] & 0xFF);
        assertEquals(4, frame[6] & 0xFF);
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(127, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChZeit1() {
        int[] frame = CdBLampenChefCommands.buildWriteChZeit1(0x4A, 1, 3, 200);
        assertEquals(3, frame[5] & 0xFF);
        assertEquals(2, frame[6] & 0xFF);
    }

    @Test
    public void testWriteChZeit2() {
        int[] frame = CdBLampenChefCommands.buildWriteChZeit2(0x4A, 1, 3, 300);
        assertEquals(6, frame[6] & 0xFF);
    }

    @Test
    public void testWriteChStartzustand() {
        int[] frame = CdBLampenChefCommands.buildWriteChStartzustand(0x4A, 1, 3, 1);
        assertEquals(7, frame[6] & 0xFF);
    }

    @Test
    public void testWriteChAusgangType() {
        int[] frame = CdBLampenChefCommands.buildWriteChAusgangType(0x4A, 1, 3, 2);
        assertEquals(9, frame[6] & 0xFF);
    }

    @Test
    public void testWriteChMagnetadresse() {
        int[] frame = CdBLampenChefCommands.buildWriteChMagnetadresse(0x4A, 2, 7, 100);
        assertEquals(13, frame.length);
        assertEquals(7, frame[5] & 0xFF);
        assertEquals(3, frame[6] & 0xFF);
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(100, frame[8] & 0xFF);
    }

    // =================================================================
    // CdBWeichenChefCommands  -  coil-fire test
    // =================================================================

    @Test
    public void testBuildCoilFireTestGen001() {
        int[] frame = CdBWeichenChefCommands.buildCoilFireTest(0x22, 1, 2, 0x3800, 1, 1, true);
        assertEquals(13,   frame.length);
        assertEquals(0x00, frame[0] & 0xFF, "byte[0]");
        assertEquals(0x16, frame[1] & 0xFF, "byte[1] opcode");
        assertEquals(0xFF, frame[2] & 0xFF, "gen-001 header byte 0");
        assertEquals(0x3C, frame[3] & 0xFF, "gen-001 header byte 1 = 60");
        assertEquals(6,    frame[4] & 0x0F, "DLC=6");
        assertEquals(2,    frame[5] & 0xFF, "channel");
        assertEquals(0x00, frame[6] & 0xFF);
        assertEquals(0x38, frame[7] & 0xFF, "addr hi");
        assertEquals(0x00, frame[8] & 0xFF, "addr lo");
        assertEquals(1,    frame[9] & 0xFF, "direction");
        assertEquals(1,    frame[10] & 0xFF, "active");
    }

    @Test
    public void testBuildCoilFireTestGen002() {
        int[] frame = CdBWeichenChefCommands.buildCoilFireTest(0x22, 1, 2, 0x3800, 1, 1, false);
        assertEquals(0x03, frame[2] & 0xFF, "gen-002+ header byte 0");
        assertEquals(0x00, frame[3] & 0xFF, "gen-002+ header byte 1");
        assertEquals(6,    frame[4] & 0x0F, "DLC=6");
        assertEquals(2,    frame[5] & 0xFF, "channel");
        assertEquals(0x38, frame[7] & 0xFF, "addr hi");
        assertEquals(0x00, frame[8] & 0xFF, "addr lo");
        assertEquals(1,    frame[9] & 0xFF, "direction");
        assertEquals(1,    frame[10] & 0xFF, "active");
    }

    @Test
    public void testBuildCoilFireTestInactiveGen002() {
        int[] frame = CdBWeichenChefCommands.buildCoilFireTest(0x22, 1, 3, 0x3801, 0, 0, false);
        assertEquals(3,    frame[5] & 0xFF, "channel 3");
        assertEquals(0x38, frame[7] & 0xFF);
        assertEquals(0x01, frame[8] & 0xFF);
        assertEquals(0,    frame[9] & 0xFF, "direction=0");
        assertEquals(0,    frame[10] & 0xFF, "active=0");
    }

    // =================================================================
    // CdBWeichenChefCommands  -  gen-002+ register builders
    // =================================================================

    @Test
    public void testWriteChSlowMotion() {
        int[] frame = CdBWeichenChefCommands.buildWriteChSlowMotion(0x22, 1, 3, 400);
        assertEquals(3,    frame[5] & 0xFF, "slot=channel");
        assertEquals(2,    frame[6] & 0xFF, "reg=2 for slow-motion");
        assertEquals(1,    frame[7] & 0xFF, "value hi = 400>>8 = 1");
        assertEquals(0x90, frame[8] & 0xFF, "value lo = 400&0xFF = 0x90");
    }

    @Test
    public void testWriteChK84() {
        int[] frame = CdBWeichenChefCommands.buildWriteChK84(0x22, 1, 2, 1);
        assertEquals(2, frame[5] & 0xFF, "slot=channel");
        assertEquals(4, frame[6] & 0xFF, "reg=4 for K84");
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(1, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChSecondaryAddress() {
        int[] frame = CdBWeichenChefCommands.buildWriteChSecondaryAddress(0x22, 1, 4, 0x3801);
        assertEquals(4,    frame[5] & 0xFF, "slot=channel");
        assertEquals(8,    frame[6] & 0xFF, "reg=8");
        assertEquals(0x38, frame[7] & 0xFF, "value hi");
        assertEquals(0x01, frame[8] & 0xFF, "value lo");
    }

    @Test
    public void testWriteChSecondaryAddressEnable() {
        int[] frame = CdBWeichenChefCommands.buildWriteChSecondaryAddressEnable(0x22, 1, 4, 1);
        assertEquals(9, frame[6] & 0xFF, "reg=9");
        assertEquals(1, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChPeriode() {
        int[] frame = CdBWeichenChefCommands.buildWriteChPeriode(0x22, 1, 2, 100);
        assertEquals(2,   frame[5] & 0xFF, "slot=channel");
        assertEquals(16,  frame[6] & 0xFF, "reg=16");
        assertEquals(0,   frame[7] & 0xFF);
        assertEquals(100, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChPulsbreite() {
        int[] frame = CdBWeichenChefCommands.buildWriteChPulsbreite(0x22, 1, 2, 50);
        assertEquals(17, frame[6] & 0xFF, "reg=17");
        assertEquals(50, frame[8] & 0xFF);
    }

    @Test
    public void testWriteChFesteZeit() {
        int[] frame = CdBWeichenChefCommands.buildWriteChFesteZeit(0x22, 1, 2, 30);
        assertEquals(18, frame[6] & 0xFF, "reg=18");
        assertEquals(30, frame[8] & 0xFF);
    }

    @Test
    public void testWriteEepromCommit() {
        int[] frame = CdBWeichenChefCommands.buildWriteEepromCommit(0x22, 1);
        assertEquals(13,  frame.length);
        assertEquals(4,   frame[4] & 0x0F, "DLC=4 (write)");
        assertEquals(0,   frame[5] & 0xFF, "slot=0 (global)");
        assertEquals(254, frame[6] & 0xFF, "reg=254");
        assertEquals(0,   frame[7] & 0xFF);
        assertEquals(1,   frame[8] & 0xFF, "value=1 triggers commit");
    }

    // =================================================================
    // CdBLampenChefCommands  -  OLD-protocol ping
    // =================================================================

    @Test
    public void testOldProtocolPingLayout() {
        int[] frame = CdBLampenChefCommands.buildOldProtocolPing(0x4A, 5);
        assertEquals(13,   frame.length);
        assertEquals(0x00, frame[0] & 0xFF, "byte[0] must be 0x00");
        assertEquals(0x40, frame[1] & 0xFF, "byte[1] must be 0x40 (ping opcode)");
        assertEquals(0x4A, frame[2] & 0xFF, "byte[2] must be classId");
        assertEquals(0x85, frame[3] & 0xFF, "byte[3] must be 0x80|addr");
        assertEquals(0,    frame[4] & 0x0F, "DLC must be 0");
    }

    @Test
    public void testFrameEncoderOldProtocolPingLayout() {
        CdBDiscoveryPlan plan = CdBDiscoveryPlan.forClass(jmri.jmrix.marklin.mcan.device.CdBDeviceClass.LAMPEN_CHEF);
        int[] frame = plan.steps().get(5).frame();  // addr=5
        assertEquals(0x00, frame[0] & 0xFF, "byte[0] must be 0x00");
        assertEquals(0x40, frame[1] & 0xFF, "byte[1] must be 0x40");
        assertEquals(jmri.jmrix.marklin.mcan.device.CdBDeviceClass.LAMPEN_CHEF.getClassId(),
                frame[2] & 0xFF, "byte[2] must be classId");
        assertEquals(0x80 | 5, frame[3] & 0xFF, "byte[3] must be 0x80|addr");
        assertEquals(0, frame[4] & 0x0F, "DLC must be 0");
    }

    // =================================================================
    // CdBFrameParser
    // =================================================================

    @Test
    public void testParserRoundTripPing() {
        int[] raw = CdBConfigCommands.buildPing(0x27);
        jmri.jmrix.marklin.mcan.classify.ClassifiedFrame cf =
            jmri.jmrix.marklin.mcan.classify.ProtocolClassifier.classify(raw);
        CdBFrameParser.CdBFrame frame = CdBFrameParser.parse(cf);
        assertEquals(CdBFrameType.PING, frame.frameType());
        assertEquals(0x27, frame.classId());
        assertEquals(0, frame.moduleId());
    }

    @Test
    public void testParserRoundTripReadReply() {
        int[] raw = CdBConfigCommands.buildReadReply(0x0D, 3, 1, 4, 1000);
        jmri.jmrix.marklin.mcan.classify.ClassifiedFrame cf =
            jmri.jmrix.marklin.mcan.classify.ProtocolClassifier.classify(raw);
        CdBFrameParser.CdBFrame frame = CdBFrameParser.parse(cf);
        assertEquals(CdBFrameType.READ_REPLY, frame.frameType());
        CdBFrameParser.ReadReplyFrame rrf = (CdBFrameParser.ReadReplyFrame) frame;
        assertEquals(1, rrf.slot());
        assertEquals(4, rrf.reg());
        assertEquals(1000, rrf.value());
    }
}
