package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LncvDeviceTest {

    LncvDevice lncvd;

    @Test
    public void testCTor() {
        LncvDevice ld1 = new LncvDevice(5000, 1, 0, 1, "Test", "R_Test", 1);
        assertNotNull(ld1, "LncvDeviceManager exists");
    }

    @Test
    void testGetProductID() {
        assertEquals(1111, lncvd.getProductID(), "get productID");
    }

    @Test
    void testGetDestAddr() {
        assertEquals(8, lncvd.getDestAddr(), "get module address");
    }

    @Test
    void testSetDestAddr() {
        lncvd.setDestAddr(14);
        assertEquals(14, lncvd.getDestAddr(), "set module address");
    }

    @Test
    void testGetCvNum() {
        assertEquals(4, lncvd.getCvNum(), "get last cv num");
    }

    @Test
    void testSetCvNum() {
        lncvd.setCvNum(68);
        assertEquals(68, lncvd.getCvNum(), "set last cv num");
    }

    @Test
    void testGetCvValue() {
        assertEquals(16, lncvd.getCvValue(), "get cv value read");
    }

    @Test
    void testSetCvValue() {
        lncvd.setCvValue(33);
        assertEquals(33, lncvd.getCvValue(), "get module address");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        LocoNetSystemConnectionMemo memo = new jmri.jmrix.loconet.LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(jmri.jmrix.loconet.LocoNetSystemConnectionMemo.class, memo);
        lncvd = new LncvDevice(1111, 8, 4, 16, "LncvMod_8", "Decoder_8", 2);

    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
