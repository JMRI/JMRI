package jmri.jmrix.marklin;

import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link CdBMessage} and {@link McanFrameSender}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
public class CdBMessageTest {

    @Test
    public void testCreatePingFrame() {
        int[] frame = CdBMessage.createPing(0x1234);
        assertNotNull(frame);
        assertEquals(13, frame.length);

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(frame);
        assertNotNull(decoded);
        assertEquals(CdBMessage.CMD_CONFIG, decoded.getCommand());
        assertFalse(decoded.isResponse());
        assertEquals(3, decoded.getDataLength());
        assertEquals(0x00, decoded.getDataByte(0)); // class 0
        assertEquals(0x00, decoded.getDataByte(1)); // module 0
        assertEquals(0x00, decoded.getDataByte(2)); // cdb cmd 0
    }

    @Test
    public void testCreateReadParamFrame() {
        int[] frame = CdBMessage.createReadParam(0x22, 0x03, 0x01, 0x5678);
        assertNotNull(frame);

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(frame);
        assertNotNull(decoded);
        assertEquals(CdBMessage.CMD_CONFIG, decoded.getCommand());
        assertEquals(4, decoded.getDataLength());
        assertEquals(0x22, decoded.getDataByte(0)); // class 0x22
        assertEquals(0x03, decoded.getDataByte(1)); // module 3
        assertEquals(0x01, decoded.getDataByte(2)); // cmd read
        assertEquals(0x01, decoded.getDataByte(3)); // param index 1
    }

    @Test
    public void testCreateWriteParamFrame() {
        int[] frame = CdBMessage.createWriteParam(0x22, 0x03, 0x01, 0xAB, 0x5678);
        assertNotNull(frame);

        MarklinCanCodec.DecodedMessage decoded = MarklinCanCodec.decode(frame);
        assertNotNull(decoded);
        assertEquals(4, decoded.getDataLength());
        assertEquals(0x22, decoded.getDataByte(0));
        assertEquals(0x03, decoded.getDataByte(1));
        assertEquals(0x02, decoded.getDataByte(2)); // cmd write
        assertEquals(0x01, decoded.getDataByte(3)); // param index
    }

    @Test
    public void testSendViaMcanFrameSender() {
        AtomicReference<int[]> sentFrame = new AtomicReference<>();
        McanFrameSender sender = sentFrame::set;

        int[] pingFrame = CdBMessage.createPing(0x0001);
        CdBMessage.send(sender, pingFrame);

        assertNotNull(sentFrame.get());
        assertArrayEquals(pingFrame, sentFrame.get());
    }

    @Test
    public void testDeselectModuleFrame() {
        int[] frame = CdBMessage.createDeselectModule(0x27, 3);
        assertEquals(13, frame.length);
        // byte[3] = 0x80 + 3 = 0x83 (write_id for class 0x27 with module 3)
        assertEquals(0x83, frame[3] & 0xFF);
        // DLC = 2 at byte[4]
        assertEquals(2, frame[4] & 0xFF);
        // data[0] = subcmd 0x00, data[1] = 0xFE (deselect)
        assertEquals(0x00, frame[5] & 0xFF);
        assertEquals(0xFE, frame[6] & 0xFF);
    }

    @Test
    public void testSelectModuleFrame() {
        int[] frame = CdBMessage.createSelectModule(0x27, 5);
        assertEquals(13, frame.length);
        assertEquals(0x85, frame[3] & 0xFF); // module 5
        assertEquals(4, frame[4] & 0xFF);    // DLC = 4
        assertEquals(0x00, frame[5] & 0xFF);
        assertEquals(0xF7, frame[6] & 0xFF); // 247 = select command
        assertEquals(0x00, frame[7] & 0xFF);
        assertEquals(0x01, frame[8] & 0xFF);
    }

    @Test
    public void testEncodeAddressType2_DCC() {
        assertEquals(14336 + 10 - 1, CdBMessage.encodeAddress("DCC", 10));
    }

    @Test
    public void testEncodeAddressType2_MM() {
        assertEquals(12288 + 5 - 1, CdBMessage.encodeAddress("MM", 5));
    }

    @Test
    public void testEncodeAddressType2_CAN() {
        assertEquals(100, CdBMessage.encodeAddress("CAN", 100));
    }

    @Test
    public void testDecodeAddressRoundTrip_DCC() {
        int raw = CdBMessage.encodeAddress("DCC", 42);
        CdBMessage.AddressEncoding enc = CdBMessage.decodeAddress(raw);
        assertEquals("DCC", enc.format);
        assertEquals(42, enc.address);
    }

    @Test
    public void testDecodeAddressRoundTrip_MM() {
        int raw = CdBMessage.encodeAddress("MM", 15);
        CdBMessage.AddressEncoding enc = CdBMessage.decodeAddress(raw);
        assertEquals("MM", enc.format);
        assertEquals(15, enc.address);
    }

    @Test
    public void testDecodeAddressRoundTrip_CAN() {
        int raw = CdBMessage.encodeAddress("CAN", 200);
        CdBMessage.AddressEncoding enc = CdBMessage.decodeAddress(raw);
        assertEquals("CAN", enc.format);
        assertEquals(200, enc.address);
    }

    @Test
    public void testEncodeAddressType3_DCC() {
        int raw = CdBMessage.encodeAddressType3("DCC", 1);
        CdBMessage.AddressEncoding enc = CdBMessage.decodeAddressType3(raw);
        assertEquals("DCC", enc.format);
        assertEquals(1, enc.address);
    }

    @Test
    public void testEncodeAddressType3_DCCext() {
        int raw = CdBMessage.encodeAddressType3("DCC ext.", 10);
        CdBMessage.AddressEncoding enc = CdBMessage.decodeAddressType3(raw);
        assertEquals("DCC ext.", enc.format);
        assertEquals(10, enc.address);
    }

    @Test
    public void testPingIdRoundTrip_class27() {
        int pingId = CdBMessage.CdBDeviceClass.WEICHEN_CHEF_KOMBI.getPingId();
        // The class value 0x27 should be recoverable from the pingId
        int classHigh = (pingId >> 21) & 0x0F;
        int classLow  = (pingId >> 12) & 0x0F;
        int recovered = (classHigh << 4) | classLow;
        assertEquals(0x27, recovered);
    }

    @Test
    public void testCreateWriteChSwitchingTimeFrame() {
        int[] frame = CdBMessage.createWriteChSwitchingTime(0x27, 3, 2, 500);
        assertEquals(13, frame.length);
        assertEquals(0x83, frame[3] & 0xFF); // module 3
        assertEquals(4, frame[4] & 0xFF);    // DLC = 4
        assertEquals(2, frame[5] & 0xFF);    // channel
        assertEquals(3, frame[6] & 0xFF);    // reg 3 = switching time
        // 500 = 0x01F4: hi=0x01 at frame[7], lo=0xF4 at frame[8]
        assertEquals(0x01, frame[7] & 0xFF);
        assertEquals(0xF4, frame[8] & 0xFF);
    }

    @Test
    public void testCreateReadAllPropertiesWeichenChef_notEmpty() {
        int[][] frames = CdBMessage.createReadAllPropertiesWeichenChef(0x27, 3);
        assertTrue(frames.length > 0, "should return at least one frame");
        for (int[] f : frames) {
            assertEquals(13, f.length);
        }
    }

    @Test
    public void testCreateReadAllPropertiesReporter_notEmpty() {
        int[][] frames = CdBMessage.createReadAllPropertiesReporter(0x0D, 1);
        assertTrue(frames.length > 0, "should return at least one frame");
        for (int[] f : frames) {
            assertEquals(13, f.length);
        }
    }

    @Test
    public void testForClassId_LampenChef() {
        CdBMessage.CdBDeviceClass cls = CdBMessage.CdBDeviceClass.forClassId(0x4A);
        assertNotNull(cls, "LAMPENCHEF (0x4A) must be resolvable");
        assertEquals(0x4A, cls.getClassId());
    }

    @Test
    public void testForClassId_GleisReporterJumbo() {
        CdBMessage.CdBDeviceClass cls = CdBMessage.CdBDeviceClass.forClassId(0x03);
        assertNotNull(cls, "GLEISREPORTER_JUMBO (0x03) must be resolvable");
        assertEquals(0x03, cls.getClassId());
    }

    @Test
    public void testForClassId_WeichenChefMotor() {
        CdBMessage.CdBDeviceClass cls = CdBMessage.CdBDeviceClass.forClassId(0x2A);
        assertNotNull(cls, "WEICHEN_CHEF_MOTOR (0x2A) must be resolvable");
        assertEquals(0x2A, cls.getClassId());
    }

    @Test
    public void testPingIdRoundTrip_LampenChef() {
        CdBMessage.CdBDeviceClass cls = CdBMessage.CdBDeviceClass.forClassId(0x4A);
        assertNotNull(cls);
        int pingId = cls.getPingId();
        int classHigh = (pingId >> 21) & 0x0F;
        int classLow  = (pingId >> 12) & 0x0F;
        assertEquals(0x4A, (classHigh << 4) | classLow);
    }

    @Test
    public void testProtocolGeneration_OldProtocolClasses() {
        assertEquals(CdBMessage.CdBDeviceClass.ProtocolGeneration.OLD,
                CdBMessage.CdBDeviceClass.LAMPEN_CHEF.getProtocolGeneration(),
                "LAMPEN_CHEF must use OLD protocol");
        assertEquals(CdBMessage.CdBDeviceClass.ProtocolGeneration.OLD,
                CdBMessage.CdBDeviceClass.WEICHEN_CHEF_MOTOR.getProtocolGeneration(),
                "WEICHEN_CHEF_MOTOR must use OLD protocol");
    }

    @Test
    public void testProtocolGeneration_NewProtocolClasses() {
        for (CdBMessage.CdBDeviceClass cls : CdBMessage.CdBDeviceClass.values()) {
            if (cls == CdBMessage.CdBDeviceClass.LAMPEN_CHEF
                    || cls == CdBMessage.CdBDeviceClass.WEICHEN_CHEF_MOTOR) {
                continue;
            }
            assertEquals(CdBMessage.CdBDeviceClass.ProtocolGeneration.NEW,
                    cls.getProtocolGeneration(),
                    cls.name() + " must use NEW protocol");
        }
    }

    @Test
    public void testCreateReadAllPropertiesLampenChef_frameCount() {
        // global regs: {1,244,245,246,250,252,48} = 7
        // per-channel regs {2,3,4,6,7,9} over 32 channels = 6*32 = 192
        int[][] frames = CdBMessage.createReadAllPropertiesLampenChef(0x4A, 2);
        assertEquals(7 + 192, frames.length, "LampenChef read-all must return 199 frames");
        for (int[] f : frames) {
            assertEquals(13, f.length);
        }
    }

    @Test
    public void testCreateWriteChDimmwert_frame() {
        int[] frame = CdBMessage.createWriteChDimmwert(0x4A, 3, 5, 127);
        assertEquals(13, frame.length);
        assertEquals(0x83, frame[3] & 0xFF); // module 3 + 0x80
        assertEquals(4, frame[4] & 0xFF);    // DLC=4
        assertEquals(5, frame[5] & 0xFF);    // channel
        assertEquals(4, frame[6] & 0xFF);    // reg 4 = Dimmwert
        assertEquals(0, frame[7] & 0xFF);    // value hi
        assertEquals(127, frame[8] & 0xFF);  // value lo
    }

    @Test
    public void testCreateWriteChZeit1_frame() {
        int[] frame = CdBMessage.createWriteChZeit1(0x4A, 1, 3, 200);
        assertEquals(13, frame.length);
        assertEquals(3, frame[5] & 0xFF);  // channel
        assertEquals(2, frame[6] & 0xFF);  // reg 2 = Zeit_1
    }

    @Test
    public void testCreateWriteChZeit2_frame() {
        int[] frame = CdBMessage.createWriteChZeit2(0x4A, 1, 3, 300);
        assertEquals(6, frame[6] & 0xFF);  // reg 6 = Zeit_2
    }

    @Test
    public void testCreateWriteChStartzustand_frame() {
        int[] frame = CdBMessage.createWriteChStartzustand(0x4A, 1, 3, 1);
        assertEquals(7, frame[6] & 0xFF);  // reg 7 = Startzustand
    }

    @Test
    public void testCreateWriteChAusgangType_frame() {
        int[] frame = CdBMessage.createWriteChAusgangType(0x4A, 1, 3, 2);
        assertEquals(9, frame[6] & 0xFF);  // reg 9 = Ausgang type
    }

    @Test
    public void testCreateWriteChMagnetadresse_lampen_frame() {
        int[] frame = CdBMessage.createWriteChMagnetadresse(0x4A, 2, 7, 100);
        assertEquals(13, frame.length);
        assertEquals(7, frame[5] & 0xFF);   // channel
        assertEquals(3, frame[6] & 0xFF);   // reg 3 = Magnetadresse
        assertEquals(0, frame[7] & 0xFF);   // addr hi (100 < 256)
        assertEquals(100, frame[8] & 0xFF); // addr lo
    }

    @Test
    public void testCreateWriteChEinSchaltzeit_frame() {
        int[] frame = CdBMessage.createWriteChEinSchaltzeit(0x0D, 1, 4, 150);
        assertEquals(13, frame.length);
        assertEquals(4, frame[5] & 0xFF);    // channel
        assertEquals(2, frame[6] & 0xFF);    // reg 2 = EinSchaltzeit
        assertEquals(0, frame[7] & 0xFF);
        assertEquals(150, frame[8] & 0xFF);
    }

    @Test
    public void testCreateWriteChAusSchaltzeit_frame() {
        int[] frame = CdBMessage.createWriteChAusSchaltzeit(0x0D, 1, 4, 75);
        assertEquals(3, frame[6] & 0xFF);    // reg 3 = AusSchaltzeit
        assertEquals(75, frame[8] & 0xFF);
    }

    @Test
    public void testCreateWriteStorungsRmk_frame() {
        int[] frame = CdBMessage.createWriteStorungsRmk(0x0D, 2, 500);
        assertEquals(13, frame.length);
        assertEquals(4, frame[4] & 0xFF);    // DLC=4
        assertEquals(0, frame[5] & 0xFF);    // subcmd 0
        assertEquals(10, frame[6] & 0xFF);   // reg 10
        assertEquals(1, frame[7] & 0xFF);    // 500 = 0x01F4, hi=1
        assertEquals(0xF4, frame[8] & 0xFF); // lo=0xF4
    }

    @Test
    public void testCreateWriteStorungsFlag_frame() {
        int[] frame = CdBMessage.createWriteStorungsFlag(0x0D, 2, 1);
        assertEquals(11, frame[6] & 0xFF);   // reg 11
        assertEquals(1, frame[8] & 0xFF);
    }

    @Test
    public void testCreateWriteSonderfunktion_frame() {
        int[] frame = CdBMessage.createWriteSonderfunktion(0x0D, 2, 3);
        assertEquals(12, frame[6] & 0xFF);   // reg 12
        assertEquals(3, frame[8] & 0xFF);
    }

    @Test
    public void testCreateWriteSlaveAddr_frame() {
        int[] frame = CdBMessage.createWriteSlaveAddr(0x0B, 1, 16, 42);
        assertEquals(16, frame[6] & 0xFF);   // reg 16
        assertEquals(42, frame[8] & 0xFF);
    }

    @Test
    public void testCreateWriteFreezeRmkAddr_frame() {
        int[] frame = CdBMessage.createWriteFreezeRmkAddr(0x0B, 1, 200);
        assertEquals(18, frame[6] & 0xFF);   // reg 18 = Freeze RMK addr (StromSniffer only)
        assertEquals(0, frame[7] & 0xFF);    // 200 fits in lo byte
        assertEquals(200, frame[8] & 0xFF);
    }
}
