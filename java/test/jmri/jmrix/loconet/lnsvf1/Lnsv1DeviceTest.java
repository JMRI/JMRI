package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrit.roster.RosterEntry;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class Lnsv1DeviceTest {

    Lnsv1Device lnsv1d;

    @Test
    public void testCTor() {
        Lnsv1Device ld1 = new Lnsv1Device(5000, 1, 0, 1, "Test", "R_Test", 1);
        assertNotNull(ld1, "Lnsv1DeviceManager exists");
    }

    @Test
    void testGetRosterName() {
        assertEquals("Decoder_81", lnsv1d.getRosterName(), "get RosterName");
    }

    @Test
    void testGetVersion() {
        assertEquals(12, lnsv1d.getSwVersion(), "get Version");
    }

    @Test
    void testSetRosterEntry() {
        lnsv1d.setRosterEntry(new RosterEntry("someFile.ext"));
        assertEquals("someFile.ext", lnsv1d.getRosterEntry().getFileName(), "set RosterEntry");
    }

    @Test
    void testGetDestAddr() {
        assertEquals(1873, lnsv1d.getDestAddr(), "get module compositeaddress");
    }

    @Test
    void testSetDestAddrLow() {
        lnsv1d.setDestAddrLow(14);
        assertEquals(14, lnsv1d.getDestAddrLow(), "set module address");
    }

    @Test
    void testGetCvNum() {
        assertEquals(2, lnsv1d.getCvNum(), "get last cv num");
    }

    @Test
    void testSetCvNum() {
        lnsv1d.setCvNum(68);
        assertEquals(68, lnsv1d.getCvNum(), "set last cv num");
    }

    @Test
    void testGetCvValue() {
        assertEquals(8, lnsv1d.getCvValue(), "get cv value read");
    }

    @Test
    void testSetCvValue() {
        lnsv1d.setCvValue(33);
        assertEquals(33, lnsv1d.getCvValue(), "get sv value 1");
        lnsv1d.setCvValue(900);
        assertEquals(900, lnsv1d.getCvValue(), "get sv value 2");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo);
        lnsv1d = new Lnsv1Device(81, 8, 2, 8, "Lnsv1Mod_81_8", "Decoder_81", 12);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
