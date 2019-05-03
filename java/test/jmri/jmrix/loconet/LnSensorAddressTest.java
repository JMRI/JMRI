package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnSensorAddress class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnSensorAddressTest {

    @Test
    public void testLnSensorAddressCreate() {
        LnSensorAddress a1 = new LnSensorAddress("LS001", "L");
        LnSensorAddress a2 = new LnSensorAddress("LS001A", "L");
        LnSensorAddress a3 = new LnSensorAddress("LS001C3", "L");
        LnSensorAddress a4 = new LnSensorAddress(0x15, 0x60, "L"); // LS043
        Assert.assertNotNull("exists", a1);
        Assert.assertNotNull("exists", a2);
        Assert.assertNotNull("exists", a3);
        Assert.assertNotNull("exists", a4);
    }

    @Test
    public void testLnSensorInvalid() {
        LnSensorAddress a;
        a = new LnSensorAddress("foo", "L") {
            @Override
            void reportParseError(String s) {
            }
        };
        Assert.assertTrue(!a.isValid());
    }

    @Test
    public void testLnSensorAddressASmode() {
        LnSensorAddress a;

        a = new LnSensorAddress("LS130A", "L");
        Assert.assertTrue(a.getLowBits() == 2);
        Assert.assertTrue(a.getHighBits() == 1);
        Assert.assertEquals("AS bit from LS130A", 0x20, a.getASBit());
        Assert.assertTrue(a.isValid());

        a = new LnSensorAddress("LS257S", "L");
        Assert.assertTrue(a.getLowBits() == 1);
        Assert.assertTrue(a.getHighBits() == 2);
        Assert.assertTrue(a.getASBit() == 0x00);
        Assert.assertTrue(a.isValid());
    }

    @Test
    public void testLnSensorAddressNumericMode() {
        LnSensorAddress a;

        a = new LnSensorAddress("LS130A2", "L"); // 0x0822
        Assert.assertTrue(a.getLowBits() == 17);
        Assert.assertTrue(a.getHighBits() == 16);
        Assert.assertTrue(a.getASBit() == 0x00);
        Assert.assertTrue(a.isValid());

        a = new LnSensorAddress("LS257D3", "L");  // 0x101F
        Assert.assertTrue(a.getLowBits() == 15);
        Assert.assertTrue(a.getHighBits() == 32);
        Assert.assertEquals("AS bit from LS257D3", 0x20, a.getASBit());
        Assert.assertTrue(a.isValid());
    }

    @Test
    public void testLnSensorAddressBDL16Mode() {
        LnSensorAddress a;

        a = new LnSensorAddress("LS131", "L");
        Assert.assertTrue(a.getLowBits() == 65);
        Assert.assertTrue(a.getHighBits() == 0);
        Assert.assertTrue(a.getASBit() == 0x00);
        Assert.assertTrue(a.isValid());

        a = new LnSensorAddress("LS258", "L");
        Assert.assertTrue(a.getLowBits() == 0);
        Assert.assertTrue(a.getHighBits() == 1);
        Assert.assertEquals("AS bit from LS258", 0x20, a.getASBit());
        Assert.assertTrue(a.isValid());
    }

    @Test
    public void testLnSensorAddressFromPacket() {
        LnSensorAddress a;

        a = new LnSensorAddress(0x15, 0x60, "L"); // LS044
        log.debug("0x15, 0x60 shows as " + a.getNumericAddress() + " "
                + a.getDS54Address() + " " + a.getBDL16Address());
        Assert.assertTrue(a.getNumericAddress().equals("LS44"));
        Assert.assertTrue(a.getDS54Address().equals("LS21A"));
        Assert.assertTrue(a.getBDL16Address().equals("LS2C3"));

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorAddressTest.class);

}
