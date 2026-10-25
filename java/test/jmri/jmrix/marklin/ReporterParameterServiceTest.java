package jmri.jmrix.marklin;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link ReporterParameterService}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class ReporterParameterServiceTest {

    private static McanDevice makeDev(int canClass, int moduleId) {
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(canClass, moduleId);
        return new McanDevice("M", id);
    }

    @Test
    void testSwitchingTimeValidation_einschaltzeit_outOfRange() {
        assertEquals(30, ReporterParameterService.clampEinschaltzeit(0));
        assertEquals(30, ReporterParameterService.clampEinschaltzeit(7000));
    }

    @Test
    void testSwitchingTimeValidation_einschaltzeit_inRange() {
        assertEquals(500, ReporterParameterService.clampEinschaltzeit(500));
        assertEquals(1, ReporterParameterService.clampEinschaltzeit(1));
        assertEquals(6000, ReporterParameterService.clampEinschaltzeit(6000));
    }

    @Test
    void testSwitchingTimeValidation_ausschaltzeit_outOfRange() {
        assertEquals(120, ReporterParameterService.clampAusschaltzeit(0));
        assertEquals(120, ReporterParameterService.clampAusschaltzeit(9999));
    }

    @Test
    void testSwitchingTimeValidation_ausschaltzeit_inRange() {
        assertEquals(1000, ReporterParameterService.clampAusschaltzeit(1000));
        assertEquals(1, ReporterParameterService.clampAusschaltzeit(1));
        assertEquals(6000, ReporterParameterService.clampAusschaltzeit(6000));
    }

    @Test
    void testFirmwareVersionGatesSonderfunktion() {
        assertTrue(ReporterParameterService.showSonderfunktion(8, 0));
        assertTrue(ReporterParameterService.showSonderfunktion(9, 0));
        assertFalse(ReporterParameterService.showSonderfunktion(7, 99));
        assertFalse(ReporterParameterService.showSonderfunktion(0, 0));
    }

    @Test
    void testFirmwareVersionGatesSlaveRow() {
        assertTrue(ReporterParameterService.showSlaveRow(6, 0));
        assertTrue(ReporterParameterService.showSlaveRow(8, 1));
        assertFalse(ReporterParameterService.showSlaveRow(7, 1)); // fw 7.1 hidden
        assertTrue(ReporterParameterService.showSlaveRow(7, 2));
    }

    @Test
    void testRmkBaseAddressComputedOnSelect() {
        List<String> inputNumbers = new ArrayList<>();
        McanDevice dev = makeDev(0x0D, 5);
        ReporterParameterService svc = new ReporterParameterService(dev, f -> {});
        svc.addListener(new ReporterParameterService.ReporterListener() {
            @Override
            public void inputNumbersUpdated(int channel, String rmkAddr) {
                inputNumbers.add(channel + ":" + rmkAddr);
            }
        });
        svc.selectModule(5);
        // rmk = 5*8 - 8 = 32; input 0 → addr 33, input 7 → addr 40
        assertTrue(inputNumbers.contains("0:33"), "channel 0 should be 33, got: " + inputNumbers);
        assertTrue(inputNumbers.contains("7:40"), "channel 7 should be 40, got: " + inputNumbers);
    }

    @Test
    void testSelectSendsDeselectOfPrevious() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x0D, 2);
        ReporterParameterService svc = new ReporterParameterService(dev, sent::add);

        svc.selectModule(2);
        sent.clear();
        svc.selectModule(4);

        assertTrue(sent.size() >= 2);
        // First frame must deselect module 2
        assertEquals(0x82, sent.get(0)[3] & 0xFF, "deselect must target module 2");
        assertEquals(0xFE, sent.get(0)[6] & 0xFF, "deselect must carry 0xFE");
        // Second frame must select module 4
        assertEquals(0x84, sent.get(1)[3] & 0xFF, "select must target module 4");
        assertEquals(0xF7, sent.get(1)[6] & 0xFF, "select must carry 0xF7");
    }

    @Test
    void testDeselectWhenNoneSelected_sendsNothing() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x0D, 1);
        ReporterParameterService svc = new ReporterParameterService(dev, sent::add);
        svc.deselectModule();
        assertTrue(sent.isEmpty());
    }

    @Test
    void testOccupancyFrameDispatch() {
        // Module raw ID = 3: rmk = 3*8-8 = 16; channel 0 → absolute RMK = 17
        // Status frame pattern: byte[0]=0, byte[1]=35,
        // bytes[2-3] such that (b23 - 2816) == rawId - 128 → b23 = rawId - 128 + 2816 = 3 - 128 + 2816 = 2691
        // byte[7]=0, byte[8]=17 (rmk+1), byte[9]=0 = occupied
        List<Boolean> occupancy = new ArrayList<>();
        McanDevice dev = makeDev(0x0D, 3);
        ReporterParameterService svc = new ReporterParameterService(dev, f -> {});
        svc.addListener(new ReporterParameterService.ReporterListener() {
            @Override public void occupancyReceived(int ch, Boolean occ) { occupancy.add(occ); }
        });
        svc.selectModule(3);

        int b23 = 2691;
        int[] data = new int[13];
        data[0] = 0;
        data[1] = 35;
        data[2] = (b23 >> 8) & 0xFF;
        data[3] = b23 & 0xFF;
        data[4] = 4;
        data[7] = 0;
        data[8] = 17; // rmk base = 16, channel 0 → addr 17
        data[9] = 0;  // occupied
        svc.handleStatusFrame(new MarklinReply(data));

        assertFalse(occupancy.isEmpty(), "occupancyReceived should have been called");
        assertTrue(occupancy.get(0), "channel should be occupied (status=0 → occupied=true)");
    }
}
