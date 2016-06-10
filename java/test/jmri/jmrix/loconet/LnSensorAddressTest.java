package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnSensorAddress class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 */
public class LnSensorAddressTest extends TestCase {

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

    public void testLnSensorInvalid() {
        LnSensorAddress a;
        a = new LnSensorAddress("foo", "L") {
            void reportParseError(String s) {
            }
        };
        assertTrue(!a.isValid());
    }

    public void testLnSensorAddressASmode() {
        LnSensorAddress a;

        a = new LnSensorAddress("LS130A", "L");
        assertTrue(a.getLowBits() == 2);
        assertTrue(a.getHighBits() == 1);
        assertEquals("AS bit from LS130A", 0x20, a.getASBit());
        assertTrue(a.isValid());

        a = new LnSensorAddress("LS257S", "L");
        assertTrue(a.getLowBits() == 1);
        assertTrue(a.getHighBits() == 2);
        assertTrue(a.getASBit() == 0x00);
        assertTrue(a.isValid());

    }

    public void testLnSensorAddressNumericMode() {
        LnSensorAddress a;

        a = new LnSensorAddress("LS130A2", "L"); // 0x0822
        assertTrue(a.getLowBits() == 17);
        assertTrue(a.getHighBits() == 16);
        assertTrue(a.getASBit() == 0x00);
        assertTrue(a.isValid());

        a = new LnSensorAddress("LS257D3", "L");  // 0x101F
        Assert.assertTrue(a.getLowBits() == 15);
        Assert.assertTrue(a.getHighBits() == 32);
        assertEquals("AS bit from LS257D3", 0x20, a.getASBit());
        Assert.assertTrue(a.isValid());

    }

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
        assertEquals("AS bit from LS258", 0x20, a.getASBit());
        Assert.assertTrue(a.isValid());

    }

    public void testLnSensorAddressFromPacket() {
        LnSensorAddress a;

        a = new LnSensorAddress(0x15, 0x60, "L"); // LS044
        log.debug("0x15, 0x60 shows as " + a.getNumericAddress() + " "
                + a.getDS54Address() + " " + a.getBDL16Address());
        Assert.assertTrue(a.getNumericAddress().equals("LS44"));
        Assert.assertTrue(a.getDS54Address().equals("LS21A"));
        Assert.assertTrue(a.getBDL16Address().equals("LS2C3"));

    }

    // from here down is testing infrastructure
    public LnSensorAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnSensorAddressTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LnSensorAddressTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorAddressTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
