package jmri.jmrix.marklin.mcan.cs2;

import jmri.jmrix.marklin.mcan.classify.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Roundtrip tests for {@link Cs2GuiCommands}.
 */
public class Cs2GuiCommandsTest {

    private static Cs2ClassifiedFrame roundtrip(int[] frame) {
        ClassifiedFrame cf = ProtocolClassifier.classify(frame);
        assertEquals(FrameProtocol.CS2_NATIVE, cf.protocol(),
                "Encoded frame must classify as CS2_NATIVE");
        return Cs2FrameParser.parse(cf);
    }

    // --- buildStatusDataConfig ---

    @Test
    public void testBuildStatusDataConfigProduces13Bytes() {
        assertEquals(13, Cs2GuiCommands.buildStatusDataConfig(0x11223344L, 0).length);
    }

    @Test
    public void testBuildStatusDataConfigHasCommandCode0x20() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2GuiCommands.buildStatusDataConfig(0x11223344L, 0));
        assertEquals(0x20, cf.cs2Header().commandCode());
    }

    @Test
    public void testBuildStatusDataConfigHasDlc5() {
        int[] frame = Cs2GuiCommands.buildStatusDataConfig(0x11223344L, 2);
        assertEquals(5, frame[4]);
    }

    @Test
    public void testBuildStatusDataConfigEncodesUidAndIndex() {
        int[] frame = Cs2GuiCommands.buildStatusDataConfig(0x11223344L, 7);
        assertEquals(0x11, frame[5]);
        assertEquals(0x22, frame[6]);
        assertEquals(0x33, frame[7]);
        assertEquals(0x44, frame[8]);
        assertEquals(7,    frame[9]);
    }

    @Test
    public void testBuildStatusDataConfigIsNotResponse() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2GuiCommands.buildStatusDataConfig(0x11223344L, 0));
        assertFalse(cf.cs2Header().response());
    }

    // --- buildRequestConfigData ---

    @Test
    public void testBuildRequestConfigDataProduces13Bytes() {
        assertEquals(13, Cs2GuiCommands.buildRequestConfigData("loks").length);
    }

    @Test
    public void testBuildRequestConfigDataHasCommandCode0x21() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2GuiCommands.buildRequestConfigData("loks"));
        assertEquals(0x21, cf.cs2Header().commandCode());
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
    public void testBuildConfigDataStreamHeaderHasCommandCode0x22() {
        ClassifiedFrame cf = ProtocolClassifier.classify(
                Cs2GuiCommands.buildConfigDataStreamHeader(1024L, 0xABCD));
        assertEquals(0x22, cf.cs2Header().commandCode());
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
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamHeader(4096L, 0x1234));
        assertInstanceOf(Cs2GuiCommands.ConfigDataStreamHeaderFrame.class, parsed);
    }

    @Test
    public void testConfigDataStreamHeaderFrameFileLength() {
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamHeader(4096L, 0x1234));
        assertEquals(4096L,
                ((Cs2GuiCommands.ConfigDataStreamHeaderFrame) parsed).fileLength());
    }

    @Test
    public void testConfigDataStreamHeaderFrameCrc() {
        Cs2ClassifiedFrame parsed = roundtrip(
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
    public void testBuildConfigDataStreamPacketMasksHashBits() {
        int[] data = new int[8];
        // Packet number with bits 7-9 set; they should be cleared in hash
        int packetNumber = 0x1FFF;
        int[] frame = Cs2GuiCommands.buildConfigDataStreamPacket(packetNumber, data);
        int hash = ((frame[2] & 0xFF) << 8) | (frame[3] & 0xFF);
        assertEquals(packetNumber & 0x1F8F, hash & 0x1F8F);
    }

    @Test
    public void testBuildConfigDataStreamPacketRejectsWrongArraySize() {
        assertThrows(IllegalArgumentException.class,
                () -> Cs2GuiCommands.buildConfigDataStreamPacket(0, new int[7]));
    }

    @Test
    public void testBuildConfigDataStreamPacketRoundtripsToPacketFrame() {
        int[] data = new int[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamPacket(3, data));
        assertInstanceOf(Cs2GuiCommands.ConfigDataStreamPacketFrame.class, parsed);
    }

    @Test
    public void testConfigDataStreamPacketFramePacketNumber() {
        int[] data = new int[8];
        Cs2ClassifiedFrame parsed = roundtrip(
                Cs2GuiCommands.buildConfigDataStreamPacket(42, data));
        assertEquals(42 & 0x1F8F,
                ((Cs2GuiCommands.ConfigDataStreamPacketFrame) parsed).packetNumber());
    }
}
