package jmri.jmrix.roco.z21;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.roco.z21.Z21XNetReply class
 *
 * @author  Paul Bender Copyright (C) 2018 
 */
public class Z21XNetReplyTest {

    @Test
    public void testCtor() {
        Z21XNetReply m = new Z21XNetReply();
        Assert.assertNotNull(m);
    }

    // Test the string constructor.
    @Test
    public void testStringCtor() {
        Z21XNetReply m = new Z21XNetReply("12 34 AB 03 19 06 0B B1");
        Assert.assertEquals("length", 8, m.getNumDataElements());
        Assert.assertEquals("0th byte", 0x12, m.getElement(0) & 0xFF);
        Assert.assertEquals("1st byte", 0x34, m.getElement(1) & 0xFF);
        Assert.assertEquals("2nd byte", 0xAB, m.getElement(2) & 0xFF);
        Assert.assertEquals("3rd byte", 0x03, m.getElement(3) & 0xFF);
        Assert.assertEquals("4th byte", 0x19, m.getElement(4) & 0xFF);
        Assert.assertEquals("5th byte", 0x06, m.getElement(5) & 0xFF);
        Assert.assertEquals("6th byte", 0x0B, m.getElement(6) & 0xFF);
        Assert.assertEquals("7th byte", 0xB1, m.getElement(7) & 0xFF);
    }

    // Test the string constructor with an empty string paramter.
    @Test
    public void testStringCtorEmptyString() {
        Z21XNetReply m = new Z21XNetReply("");
        Assert.assertEquals("length", 0, m.getNumDataElements());
        Assert.assertTrue("empty reply",m.toString().equals(""));
    }

    // Test the copy constructor.
    @Test
    public void testCopyCtor() {
        Z21XNetReply x = new Z21XNetReply("12 34 AB 03 19 06 0B B1");
        Z21XNetReply m = new Z21XNetReply(x);
        Assert.assertEquals("length", x.getNumDataElements(), m.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0), m.getElement(0));
        Assert.assertEquals("1st byte", x.getElement(1), m.getElement(1));
        Assert.assertEquals("2nd byte", x.getElement(2), m.getElement(2));
        Assert.assertEquals("3rd byte", x.getElement(3), m.getElement(3));
        Assert.assertEquals("4th byte", x.getElement(4), m.getElement(4));
        Assert.assertEquals("5th byte", x.getElement(5), m.getElement(5));
        Assert.assertEquals("6th byte", x.getElement(6), m.getElement(6));
        Assert.assertEquals("7th byte", x.getElement(7), m.getElement(7));
    }

    // Test the XNetMessage constructor.
    @Test
    public void testXNetMessageCtor() {
        Z21XNetMessage x = new Z21XNetMessage("12 34 AB 03 19 06 0B B1");
        Z21XNetReply m = new Z21XNetReply(x);
        Assert.assertEquals("length", x.getNumDataElements(), m.getNumDataElements());
        Assert.assertEquals("0th byte", x.getElement(0)& 0xFF, m.getElement(0)& 0xFF);
        Assert.assertEquals("1st byte", x.getElement(1)& 0xFF, m.getElement(1)& 0xFF);
        Assert.assertEquals("2nd byte", x.getElement(2)& 0xFF, m.getElement(2)& 0xFF);
        Assert.assertEquals("3rd byte", x.getElement(3)& 0xFF, m.getElement(3)& 0xFF);
        Assert.assertEquals("4th byte", x.getElement(4)& 0xFF, m.getElement(4)& 0xFF);
        Assert.assertEquals("5th byte", x.getElement(5)& 0xFF, m.getElement(5)& 0xFF);
        Assert.assertEquals("6th byte", x.getElement(6)& 0xFF, m.getElement(6)& 0xFF);
        Assert.assertEquals("7th byte", x.getElement(7)& 0xFF, m.getElement(7)& 0xFF);
    }

    // get information from specific types of messages.

    // check is service mode response
    @Test
    public void testIsServiceModeResponse() {
        // CV 1 in direct mode.
        Z21XNetReply r = new Z21XNetReply("64 14 00 14 05 61");
        Assert.assertTrue(r.isServiceModeResponse());
    }
 
   @Test
    public void testToMonitorStringServiceModeDirectResponse(){
        Z21XNetReply r = new Z21XNetReply("64 14 00 14 05 61");
        Assert.assertEquals("Monitor String",Bundle.getMessage("Z21LAN_X_CV_RESULT",21,5),r.toMonitorString());
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

}
