package jmri.jmrix.marklin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import jmri.util.JUnitUtil;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WeichenChefKombiParameterService}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class WeichenChefKombiParameterServiceTest {

    // Disposed in tearDown to avoid leaking pending delayed property-query/retry tasks into later tests.
    private WeichenChefKombiParameterService svcUnderTest;

    private static McanDevice makeDev(int canClass, int moduleId) {
        McanDeviceIdentity.CdB id = McanDeviceIdentity.forCdB(canClass, moduleId);
        return new McanDevice("M", id);
    }

    @Test
    void testVoltageCalc_defaultChip() {
        // raw = 0x0500 = 1280; volts = 1280 * 68 / 1000 = 87 (int div) → 8.7 V → low warning
        WeichenChefKombiParameterService.VoltageResult r =
            WeichenChefKombiParameterService.calcVoltage(0x0500, null);
        assertTrue(r.lowVoltageWarning, "expected low voltage warning for 8.7V");
    }

    @Test
    void testVoltageCalc_chip61() {
        // raw = 0x0500 = 1280; volts = 1280 * 33 / 100 = 422 (unit: 0.01V) → 4.22 V → warning
        WeichenChefKombiParameterService.VoltageResult r =
            WeichenChefKombiParameterService.calcVoltage(0x0500, "61");
        assertTrue(r.lowVoltageWarning, "expected low voltage warning for chip61 result");
    }

    @Test
    void testVoltageCalc_noWarning() {
        // raw = 14000; volts = 14000 * 68 / 1000 = 952 → 9.52 V → no warning
        WeichenChefKombiParameterService.VoltageResult r =
            WeichenChefKombiParameterService.calcVoltage(14000, null);
        assertFalse(r.lowVoltageWarning, "expected no low voltage warning for 9.52V");
    }

    @Test
    void testModuleSelectFrameOrder() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x27, 3);
        WeichenChefKombiParameterService svc =
            new WeichenChefKombiParameterService(dev, sent::add);
        svcUnderTest = svc;

        // First select sets module to 3
        svc.selectModule(3);
        sent.clear();

        // Second select: must deselect old (addr 3) first, then select new (addr 5)
        svc.selectModule(5);

        assertTrue(sent.size() >= 2, "must send at least deselect + select frames");
        // First frame: deselect module 3 → byte[3]=0x83, byte[6]=0xFE
        assertEquals(0x83, sent.get(0)[3] & 0xFF, "deselect frame must target module 3");
        assertEquals(0xFE, sent.get(0)[6] & 0xFF, "deselect frame must carry 0xFE");
        // Second frame: select module 5 → byte[3]=0x85, byte[6]=0xF7
        assertEquals(0x85, sent.get(1)[3] & 0xFF, "select frame must target module 5");
        assertEquals(0xF7, sent.get(1)[6] & 0xFF, "select frame must carry 0xF7");
    }

    @Test
    void testSelectSendsPropertyQuery() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x27, 2);
        WeichenChefKombiParameterService svc =
            new WeichenChefKombiParameterService(dev, sent::add);
        svcUnderTest = svc;

        svc.selectModule(2);

        // Must send at least select frame + all property query frames
        int[][] queryFrames = jmri.jmrix.marklin.mcan.cdb.CdBWeichenChefCommands.buildReadAllProperties(0x27, 2);
        jmri.util.JUnitUtil.waitFor(
            () -> sent.size() >= 1 + queryFrames.length,
            "all property query frames sent");
        assertTrue(sent.size() >= 1 + queryFrames.length,
            "must send select + all property read frames");
    }

    @Test
    void testDeselectModule() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x27, 4);
        WeichenChefKombiParameterService svc =
            new WeichenChefKombiParameterService(dev, sent::add);
        svcUnderTest = svc;

        svc.selectModule(4);
        sent.clear();
        svc.deselectModule();

        assertEquals(1, sent.size(), "deselect must send exactly one frame");
        assertEquals(0x84, sent.get(0)[3] & 0xFF, "deselect frame must target module 4");
        assertEquals(0xFE, sent.get(0)[6] & 0xFF, "deselect frame must carry 0xFE");
    }

    @Test
    void testDeselectWhenNoneSelected_sendsNothing() {
        List<int[]> sent = new ArrayList<>();
        McanDevice dev = makeDev(0x27, 1);
        WeichenChefKombiParameterService svc =
            new WeichenChefKombiParameterService(dev, sent::add);

        svc.deselectModule(); // no module selected yet

        assertTrue(sent.isEmpty(), "deselect with no selected module must not send");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        // Cancels services on GUI thread before teardown
        if (svcUnderTest != null) {
            svcUnderTest.dispose();
            svcUnderTest = null;
        }
        JUnitUtil.tearDown();
    }
}
