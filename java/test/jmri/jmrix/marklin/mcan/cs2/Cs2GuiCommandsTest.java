package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.FrameProtocol;
import jmri.jmrix.marklin.mcan.McanUid;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2GuiCommands}.
 */
public class Cs2GuiCommandsTest {

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

    // --- buildStatusDataConfig ---

    @Test
    public void testBuildStatusDataConfigProduces13Bytes() {
        assertEquals(13, Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 0).length);
    }

    @Test
    public void testBuildStatusDataConfigHasCommandCode0x1D() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 0));
        assertEquals(0x1D, cf.header().commandCode());
    }

    @Test
    public void testBuildStatusDataConfigHasDlc5() {
        int[] frame = Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 2);
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildStatusDataConfigEncodesUidAndIndex() {
        int[] frame = Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 7);
        assertEquals(0x11, frame[5]);
        assertEquals(0x22, frame[6]);
        assertEquals(0x33, frame[7]);
        assertEquals(0x44, frame[8]);
        assertEquals(7,    frame[9]);
    }

    @Test
    public void testBuildStatusDataConfigIsNotResponse() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 0));
        assertFalse(cf.header().response());
    }

    // --- buildRequestConfigData ---

    @Test
    public void testBuildRequestConfigDataProduces13Bytes() {
        assertEquals(13, Cs2GuiCommands.buildRequestConfigData("loks").length);
    }

    @Test
    public void testBuildRequestConfigDataHasCommandCode0x20() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2GuiCommands.buildRequestConfigData("loks"));
        assertEquals(0x20, cf.header().commandCode());
    }

    @Test
    public void testBuildRequestConfigDataHasDlc8() {
        int[] frame = Cs2GuiCommands.buildRequestConfigData("loks");
        assertEquals(8, frame[4]);
    }

    @Test
    public void testBuildRequestConfigDataEncodesFilename() {
        int[] frame = Cs2GuiCommands.buildRequestConfigData("loks");
        assertEquals('l', frame[5]);
        assertEquals('o', frame[6]);
        assertEquals('k', frame[7]);
        assertEquals('s', frame[8]);
        assertEquals(0,   frame[9]);
    }

    @Test
    public void testBuildRequestConfigDataRejectsLongName() {
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildRequestConfigData("toolongname"));
    }

    // --- buildConfigDataStreamHeader ---

    @Test
    public void testBuildConfigDataStreamHeaderProduces13Bytes() {
        assertEquals(13, Cs2GuiCommands.buildConfigDataStreamHeader(1024L, 0xABCD).length);
    }

    @Test
    public void testBuildConfigDataStreamHeaderHasCommandCode0x21() {
        Cs2Frame cf = (Cs2Frame) ProtocolClassifier.classify(
                Cs2GuiCommands.buildConfigDataStreamHeader(1024L, 0xABCD));
        assertEquals(0x21, cf.header().commandCode());
    }

    @Test
    public void testBuildConfigDataStreamHeaderHasDlc6() {
        int[] frame = Cs2GuiCommands.buildConfigDataStreamHeader(1024L, 0xABCD);
        assertEquals(6, frame[4]);
    }

    @Test
    public void testBuildConfigDataStreamHeaderEncodesLengthAndCrc() {
        int[] frame = Cs2GuiCommands.buildConfigDataStreamHeader(0x00000400L, 0xABCD);
        assertEquals(0x00, frame[5]);
        assertEquals(0x00, frame[6]);
        assertEquals(0x04, frame[7]);
        assertEquals(0x00, frame[8]);
        assertEquals(0xAB, frame[9]);
        assertEquals(0xCD, frame[10]);
    }

    @Test
    public void testBuildConfigDataStreamHeaderRoundtripsToHeaderFrame() {
        Cs2Frame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamHeader(4096L, 0x1234));
        assertInstanceOf(Cs2GuiCommands.ConfigDataStreamHeaderFrame.class, parsed);
    }

    @Test
    public void testConfigDataStreamHeaderFrameFileLength() {
        Cs2Frame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamHeader(4096L, 0x1234));
        assertEquals(4096L,
                ((Cs2GuiCommands.ConfigDataStreamHeaderFrame) parsed).fileLength());
    }

    @Test
    public void testConfigDataStreamHeaderFrameCrc() {
        Cs2Frame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamHeader(4096L, 0x5678));
        assertEquals(0x5678,
                ((Cs2GuiCommands.ConfigDataStreamHeaderFrame) parsed).crc());
    }

    // --- buildConfigDataStreamPacket ---

    @Test
    public void testBuildConfigDataStreamPacketProduces13Bytes() {
        int[] data = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        assertEquals(13, Cs2GuiCommands.buildConfigDataStreamPacket(1, data).length);
    }

    @Test
    public void testBuildConfigDataStreamPacketHasDlc8() {
        int[] data = new int[]{0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF, 0x11, 0x22};
        int[] frame = Cs2GuiCommands.buildConfigDataStreamPacket(5, data);
        assertEquals(8, frame[4]);
    }

    @Test
    public void testBuildConfigDataStreamPacketEncodesData() {
        int[] data = new int[]{0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88};
        int[] frame = Cs2GuiCommands.buildConfigDataStreamPacket(0, data);
        for (int i = 0; i < 8; i++) {
            assertEquals(data[i], frame[5 + i], "data byte " + i);
        }
    }

    @Test
    public void testBuildConfigDataStreamPacketPreservesTransactionHash() {
        int[] data = new int[8];
        int transactionHash = 0xFB21;
        int[] frame = Cs2GuiCommands.buildConfigDataStreamPacket(transactionHash, data);
        int hash = ((frame[2] & 0xFF) << 8) | (frame[3] & 0xFF);
        assertEquals(transactionHash, hash);
    }

    @Test
    public void testBuildConfigDataStreamPacketRejectsWrongArraySize() {
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildConfigDataStreamPacket(0, new int[7]));
    }

    @Test
    public void testBuildConfigDataStreamPacketRoundtripsToPacketFrame() {
        int[] data = new int[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        Cs2Frame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamPacket(0xFB21, data));
        assertInstanceOf(Cs2GuiCommands.ConfigDataStreamPacketFrame.class, parsed);
    }

    @Test
    public void testConfigDataStreamPacketFrameTransactionHash() {
        int[] data = new int[8];
        Cs2Frame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamPacket(0xFB21, data));
        assertEquals(0xFB21,
                ((Cs2GuiCommands.ConfigDataStreamPacketFrame) parsed).transactionHash());
    }

    // --- StatusDataConfigRequestFrame ---

    @Test
    public void testBuildStatusDataConfigRoundtripsToRequestFrame() {
        Cs2Frame parsed = roundtrip(Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 2));
        assertInstanceOf(Cs2GuiCommands.StatusDataConfigRequestFrame.class, parsed);
    }

    @Test
    public void testStatusDataConfigRequestFrameDeviceUid() {
        Cs2Frame parsed = roundtrip(Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0xAABBCCDDL), 0));
        assertEquals(McanUid.of(0xAABBCCDDL),
                ((Cs2GuiCommands.StatusDataConfigRequestFrame) parsed).deviceUid());
    }

    @Test
    public void testStatusDataConfigRequestFrameIndex() {
        Cs2Frame parsed = roundtrip(Cs2GuiCommands.buildStatusDataConfig(McanUid.of(0x11223344L), 5));
        assertEquals(5,
                ((Cs2GuiCommands.StatusDataConfigRequestFrame) parsed).index());
    }

    // --- RequestConfigDataRequestFrame ---

    @Test
    public void testBuildRequestConfigDataRoundtripsToRequestFrame() {
        Cs2Frame parsed = roundtrip(Cs2GuiCommands.buildRequestConfigData("loks"));
        assertInstanceOf(Cs2GuiCommands.RequestConfigDataRequestFrame.class, parsed);
    }

    @Test
    public void testRequestConfigDataRequestFrameFilename() {
        Cs2Frame parsed = roundtrip(Cs2GuiCommands.buildRequestConfigData("lokinfo"));
        assertEquals("lokinfo",
                ((Cs2GuiCommands.RequestConfigDataRequestFrame) parsed).filename());
    }

    @Test
    public void testMs2RequestsMatchWireFrames() {
        assertArrayEquals(new int[]{0, 0x40, 0x47, 0x12, 8,
            0x6C, 0x6F, 0x6B, 0x6C, 0x69, 0x73, 0x74, 0x65},
                Cs2GuiCommands.buildRequestConfigData("lokliste", 0x4712));
        java.util.List<int[]> frames = Cs2GuiCommands.buildRequestLocoInfo("K\u00d6F II EMMA", 0x4712);
        assertEquals(3, frames.size());
        assertArrayEquals(new int[]{0, 0x40, 0x47, 0x12, 8,
            0x6C, 0x6F, 0x6B, 0x69, 0x6E, 0x66, 0x6F, 0}, frames.get(0));
        assertArrayEquals(new int[]{0, 0x40, 0x47, 0x12, 8,
            0x4B, 0xC3, 0x96, 0x46, 0x20, 0x49, 0x49, 0x20}, frames.get(1));
        assertArrayEquals(new int[]{0, 0x40, 0x47, 0x12, 8,
            0x45, 0x4D, 0x4D, 0x41, 0, 0, 0, 0}, frames.get(2));
        frames = Cs2GuiCommands.buildRequestLocoInfo("1234567\u00d6", 0x4712);
        assertEquals(0xC3, frames.get(1)[12]);
        assertEquals(0x96, frames.get(2)[5]);
    }

    @Test
    public void testLocoInfoPaddingAndNameLimits() {
        java.util.List<int[]> frames = Cs2GuiCommands.buildRequestLocoInfo("1234567890123456", 0x4712);
        assertEquals('6', frames.get(2)[12]);
        frames = Cs2GuiCommands.buildRequestLocoInfo("BR 89", 0x4712);
        assertEquals(0, frames.get(1)[10]);
        assertArrayEquals(new int[]{0, 0x40, 0x47, 0x12, 8, 0, 0, 0, 0, 0, 0, 0, 0}, frames.get(2));
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildRequestLocoInfo("\u00d6".repeat(9), 0x4712));
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildRequestLocoInfo("A\0B", 0x4712));
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildRequestConfigData("l\u00f6ks", 0x4712));
    }

    @Test
    public void testCapturedBroadcastHeaderParses() {
        int[] header = {0, 0x42, 0xFB, 0x21, 7, 0, 0, 0, 32, 0xCE, 0x2C, 0, 0};
        Cs2GuiCommands.ConfigDataStreamHeaderFrame parsed =
                (Cs2GuiCommands.ConfigDataStreamHeaderFrame) roundtrip(header);
        assertEquals(32, parsed.fileLength());
        assertEquals(0xCE2C, parsed.crc());
        assertEquals(Cs2Command.CONFIG_DATA_STREAM, parsed.command());
    }
}
