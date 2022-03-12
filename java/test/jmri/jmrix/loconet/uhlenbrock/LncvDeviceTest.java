package jmri.jmrix.loconet.uhlenbrock;

import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locobufferusb.Bundle;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LncvDeviceTest {

    LncvDevice lncvd;

    @Test
    public void testCTor() {
        LncvDevice ld1 = new LncvDevice(5000, 1, 0, 1, "Test", "R_Test", 1);
        Assert.assertNotNull("LncvDeviceManager exists", ld1);
    }

    @Test
    void testGetProductID() {
        Assert.assertEquals("get productID", 1111, lncvd.getProductID());
    }

    @Test
    void testGetDestAddr() {
        Assert.assertEquals("get module address", 8, lncvd.getDestAddr());
    }

    @Test
    void testSetDestAddr() {
        lncvd.setDestAddr(14);
        Assert.assertEquals("set module address", 14, lncvd.getDestAddr());
    }

    @Test
    void testGetCvNum() {
        Assert.assertEquals("get last cv num", 4, lncvd.getCvNum());
    }

    @Test
    void testSetCvNum() {
        lncvd.setCvNum(68);
        Assert.assertEquals("set last cv num", 68, lncvd.getCvNum());
    }

    @Test
    void testGetCvValue() {
        Assert.assertEquals("get cv value read", 16, lncvd.getCvValue());
    }

    @Test
    void testSetCvValue() {
        lncvd.setCvValue(33);
        Assert.assertEquals("get module address", 33, lncvd.getCvValue());
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
